package com.sparta.wildcard_newsfeed.domain.user.service;

import com.sparta.wildcard_newsfeed.domain.file.service.FileService;
import com.sparta.wildcard_newsfeed.domain.user.dto.EmailSendEvent;
import com.sparta.wildcard_newsfeed.domain.user.dto.UserResponseDto;
import com.sparta.wildcard_newsfeed.domain.user.dto.UserSignupRequestDto;
import com.sparta.wildcard_newsfeed.domain.user.dto.UserSignupResponseDto;
import com.sparta.wildcard_newsfeed.domain.user.entity.AuthCodeHistory;
import com.sparta.wildcard_newsfeed.domain.user.entity.User;
import com.sparta.wildcard_newsfeed.domain.user.entity.UserStatusEnum;
import com.sparta.wildcard_newsfeed.domain.user.repository.AuthCodeRepository;
import com.sparta.wildcard_newsfeed.domain.user.repository.UserRepository;
import com.sparta.wildcard_newsfeed.security.AuthenticationUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthCodeRepository authCodeRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private FileService fileService;

    @InjectMocks
    private UserService userService;

    private UserSignupRequestDto requestDto;

    @BeforeEach
    public void setUp() {
        requestDto = UserSignupRequestDto.builder()
                .usercode("testUser1234")
                .password("currentPWD12@@")
                .email("test@gmail.com")
                .build();
    }

    @Test
    @DisplayName("회원가입_성공")
    public void should_RegisterUser_when_SignupSuccess() {
        // given
        when(userRepository.findByUsercodeOrEmail(requestDto.getUsercode(), requestDto.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(requestDto.getPassword())).thenReturn("encodedPassword");
        User user = new User(requestDto.getUsercode(), "encodedPassword", requestDto.getEmail());
        when(userRepository.save(any(User.class))).thenReturn(user);
        AuthCodeHistory authCodeHistory = AuthCodeHistory.builder()
                .user(user)
                .autoCode("authCode")
                .expireDate(LocalDateTime.now().plusSeconds(180L))
                .build();
        when(authCodeRepository.save(any(AuthCodeHistory.class))).thenReturn(authCodeHistory);

        // when
        UserSignupResponseDto responseDto = userService.signup(requestDto);

        // then
        verify(userRepository, times(1)).findByUsercodeOrEmail(requestDto.getUsercode(), requestDto.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
        verify(authCodeRepository, times(1)).save(any(AuthCodeHistory.class));
        verify(eventPublisher, times(1)).publishEvent(any(EmailSendEvent.class));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals("encodedPassword", savedUser.getPassword());

        assertNotNull(responseDto);
        assertEquals(user.getUsercode(), responseDto.getUsercode());
        assertEquals(user.getEmail(), responseDto.getEmail());
    }

    @Test
    @DisplayName("회원가입 - (예외) 이미 가입한 아이디 또는 이메일")
    public void should_ThrowException_when_DuplicateUserOrEmail() {
        // given
        when(userRepository.findByUsercodeOrEmail(requestDto.getUsercode(), requestDto.getEmail())).thenReturn(Optional.of(requestDto.toUserDomain()));

        // when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.signup(requestDto);
        });

        // then
        assertEquals("이미 가입한 아이디 또는 이메일이 있습니다.", exception.getMessage());
        verify(userRepository, times(1)).findByUsercodeOrEmail(requestDto.getUsercode(), requestDto.getEmail());
        verify(userRepository, times(0)).save(any(User.class));
        verify(authCodeRepository, times(0)).save(any(AuthCodeHistory.class));
        verify(eventPublisher, times(0)).publishEvent(any(EmailSendEvent.class));
    }

    @Test
    @DisplayName("회원 탈퇴 - 성공")
    void should_DisableUser_when_ResignSuccess() {
        // given
        AuthenticationUser loginUser = new AuthenticationUser("testUser", "encodedPassword");
        String password = "currentPassword";

        User user = new User("testUser", "encodedPassword", "test@gmail.com");

        when(userRepository.findByUsercode(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        // when
        userService.resign(loginUser, password);

        // then
        assertEquals(UserStatusEnum.DISABLED, user.getUserStatus());
        verify(userRepository, times(1)).findByUsercode(anyString());
        verify(passwordEncoder, times(1)).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("회원 탈퇴 - 실패: 사용자가 없는 경우")
    void should_ThrowException_when_UserNotFoundOnResign() {
        // given
        AuthenticationUser loginUser = new AuthenticationUser("testUser", "encodedPassword");
        String password = "currentPassword";

        when(userRepository.findByUsercode(anyString())).thenReturn(Optional.empty());

        // when / then
        assertThrows(NullPointerException.class, () -> userService.resign(loginUser, password));
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("회원 조회 - 성공")
    void should_ReturnUser_when_FindByIdSuccess() {
        // given
        Long userId = 1L;
        User user = new User("testUser", "encodedPassword", "test@gmail.com");

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        // when
        UserResponseDto responseDto = userService.findById(userId);

        // then
        assertNotNull(responseDto);
        assertEquals(user.getUsercode(), responseDto.getUsercode());
        assertEquals(user.getEmail(), responseDto.getEmail());
        verify(userRepository, times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("회원 조회 - 실패: 사용자를 찾을 수 없음")
    void should_ThrowException_when_UserNotFoundOnFindById() {
        // given
        Long userId = 1L;

        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when / then
        assertThrows(IllegalArgumentException.class, () -> userService.findById(userId));
        verify(userRepository, times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("프로필 사진 업로드 - 성공")
    void should_UploadProfileImage_when_Success() {
        // given
        AuthenticationUser loginUser = new AuthenticationUser("testUser", "encodedPassword");
        Long userId = 1L;
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test image".getBytes());
        String s3Url = "https://s3.bucket.url/profiles/test.jpg";

        User user = User.builder()
                .id(1L)
                .usercode("testUser")
                .password("encodedPassword")
                .email("test@gmail.com")
                .build();

        when(userRepository.findByUsercode(anyString())).thenReturn(Optional.of(user));
        when(fileService.uploadFileToS3(any(MockMultipartFile.class))).thenReturn(s3Url);

        // when
        String savedS3Url = userService.uploadProfileImage(loginUser, userId, file);

        // then
        assertEquals(s3Url, savedS3Url);
        assertEquals(s3Url, user.getProfileImageUrl());
        verify(userRepository, times(1)).findByUsercode(anyString());
        verify(fileService, times(1)).uploadFileToS3(any(MockMultipartFile.class));
    }

    @Test
    @DisplayName("프로필 사진 업로드 - 실패: 사용자가 일치하지 않음")
    void should_ThrowException_when_UserMismatchOnUploadProfileImage() {
        // given
        AuthenticationUser loginUser = new AuthenticationUser("testUser", "encodedPassword");
        Long userId = 1L;
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test image".getBytes());

        User user = new User("anotherUser", "encodedPassword", "another@gmail.com");

        when(userRepository.findByUsercode(anyString())).thenReturn(Optional.of(user));

        // when / then
        assertThrows(IllegalArgumentException.class, () -> userService.uploadProfileImage(loginUser, userId, file));
        verify(userRepository, times(1)).findByUsercode(anyString());
        verify(fileService, never()).uploadFileToS3(any(MockMultipartFile.class));
    }

}