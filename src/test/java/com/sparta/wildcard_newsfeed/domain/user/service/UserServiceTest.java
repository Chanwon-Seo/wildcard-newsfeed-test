package com.sparta.wildcard_newsfeed.domain.user.service;

import com.sparta.wildcard_newsfeed.domain.user.dto.EmailSendEvent;
import com.sparta.wildcard_newsfeed.domain.user.dto.UserSignupRequestDto;
import com.sparta.wildcard_newsfeed.domain.user.dto.UserSignupResponseDto;
import com.sparta.wildcard_newsfeed.domain.user.entity.AuthCodeHistory;
import com.sparta.wildcard_newsfeed.domain.user.entity.User;
import com.sparta.wildcard_newsfeed.domain.user.repository.AuthCodeRepository;
import com.sparta.wildcard_newsfeed.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
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
    public void test1() {
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
    public void test1_1_throw() {
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
}