package com.sparta.wildcard_newsfeed.domain.user.service;

import com.sparta.wildcard_newsfeed.domain.file.service.FileService;
import com.sparta.wildcard_newsfeed.domain.user.dto.*;
import com.sparta.wildcard_newsfeed.domain.user.entity.AuthCodeHistory;
import com.sparta.wildcard_newsfeed.domain.user.entity.User;
import com.sparta.wildcard_newsfeed.domain.user.entity.UserStatusEnum;
import com.sparta.wildcard_newsfeed.domain.user.repository.AuthCodeRepository;
import com.sparta.wildcard_newsfeed.domain.user.repository.UserRepository;
import com.sparta.wildcard_newsfeed.exception.customexception.UserNotFoundException;
import com.sparta.wildcard_newsfeed.security.AuthenticationUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import static com.sparta.wildcard_newsfeed.domain.user.dto.emailtemplate.EmailTemplate.AUTH_EMAIL;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final AuthCodeRepository authCodeRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;
    private final FileService fileService;

    private static final Long MAX_EXPIRE_TIME = 180L;

    @Transactional
    public UserSignupResponseDto signup(UserSignupRequestDto requestDto) {
        String usercode = requestDto.getUsercode();
        String email = requestDto.getEmail();

        userRepository.findByUsercodeOrEmail(usercode, email).ifPresent(u -> {
            throw new IllegalArgumentException("이미 가입한 아이디 또는 이메일이 있습니다.");
        });

        //Bcrypt 암호화
        String pwd = passwordEncoder.encode(requestDto.getPassword());
        User user = userRepository.save(new User(usercode, pwd, email));

        //autocode 생성 및 등록
        AuthCodeHistory authCodeHistory = AuthCodeHistory.builder()
                .user(user)
                .autoCode(createAuthCode())
                .expireDate(LocalDateTime.now().plusSeconds(MAX_EXPIRE_TIME))
                .build();
        authCodeRepository.save(authCodeHistory);

        //메일 생성 후 전송
        eventPublisher.publishEvent(EmailSendEvent.of(AUTH_EMAIL.getSub(), AUTH_EMAIL.formatBody(authCodeHistory.getAutoCode()), user.getEmail()));

        return new UserSignupResponseDto(user);
    }

    @Transactional
    public void resign(AuthenticationUser user, String password) {
        String usercode = user.getUsername();

        User findUser = userRepository.findByUsercode(usercode)
                .orElseThrow(() -> new NullPointerException("해당하는 회원이 없습니다!!"));

        if (findUser.getUserStatus() == UserStatusEnum.DISABLED) {
            throw new IllegalArgumentException("이미 탈퇴한 사용자입니다.");
        }

        if (!passwordEncoder.matches(password, findUser.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다!!");
        }

        findUser.setUserStatus(UserStatusEnum.DISABLED);
    }

    @Transactional(readOnly = true)
    public UserResponseDto findById(Long userId) {
        User findUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (findUser.getUserStatus() == UserStatusEnum.DISABLED) {
            throw new IllegalArgumentException("이미 탈퇴한 사용자입니다.");
        }

        return UserResponseDto.of(findUser);
    }

    @Transactional
    public UserResponseDto updateUser(AuthenticationUser loginUser, Long userId, UserRequestDto requestDto) {
        User findUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (findUser.getUserStatus() == UserStatusEnum.DISABLED) {
            throw new IllegalArgumentException("이미 탈퇴한 사용자입니다.");
        }

        if (!Objects.equals(findUser.getUsercode(), loginUser.getUsername())) {
            throw new IllegalArgumentException("사용자가 다릅니다.");
        }

        if (requestDto.getPassword() != null) {
            if (requestDto.getChangePassword() == null) {
                throw new IllegalArgumentException("변경할 비밀번호을 입력해 주세요.");
            }
        }
        if (requestDto.getChangePassword() != null) {
            if (requestDto.getPassword() == null) {
                throw new IllegalArgumentException("현재 비밀번호를 입력해 주세요.");
            }
        }
        if (requestDto.getPassword() != null && requestDto.getChangePassword() != null) {
            if (!passwordEncoder.matches(requestDto.getPassword(), loginUser.getPassword())
                    || !passwordEncoder.matches(requestDto.getPassword(), findUser.getPassword())) {
                throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
            }
            if (requestDto.getPassword().equals(requestDto.getChangePassword())) {
                throw new IllegalArgumentException("변경하려는 비밀번호와 현재 비밀번호가 같습니다.");
            }
        }

        requestDto.encryptPassword(passwordEncoder.encode(requestDto.getChangePassword()));
        findUser.update(requestDto);

        User savedUser = userRepository.save(findUser);
        return UserResponseDto.of(savedUser);
    }

    @Transactional(readOnly = true)
    public UserResponseFromTokenDto findByUsercode(String usercode) {
        User user = userRepository.findByUsercode(usercode).orElseThrow(UserNotFoundException::new);
        return UserResponseFromTokenDto.of(user);
    }

    @Transactional
    public void updateRefreshToken(String usercode, String refreshToken) {
        User user = userRepository.findByUsercode(usercode).orElseThrow(UserNotFoundException::new);
        user.setRefreshToken(refreshToken);
    }

    @Transactional
    public String uploadProfileImage(AuthenticationUser loginUser, Long userId, MultipartFile file) {
        User findUser = userRepository.findByUsercode(loginUser.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (!Objects.equals(findUser.getId(), userId)) {
            throw new IllegalArgumentException("사용자가 일치하지 않습니다.");
        }

        String s3Url = fileService.uploadFileToS3(file);
        log.info("S3에 저장한 파일 주소: {}", s3Url);
        findUser.setProfileImageUrl(s3Url);

        return s3Url;
    }

    /**
     * Auto 생성
     */
    private String createAuthCode() {
        return UUID.randomUUID().toString();
    }
}