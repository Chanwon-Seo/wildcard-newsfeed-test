package com.sparta.wildcard_newsfeed.domain.user.service;


import com.sparta.wildcard_newsfeed.domain.file.service.FileService;
import com.sparta.wildcard_newsfeed.domain.user.dto.EmailSendEvent;
import com.sparta.wildcard_newsfeed.domain.user.dto.UserSignupRequestDto;
import com.sparta.wildcard_newsfeed.domain.user.dto.UserSignupResponseDto;
import com.sparta.wildcard_newsfeed.domain.user.entity.AuthCodeHistory;
import com.sparta.wildcard_newsfeed.domain.user.entity.User;
import com.sparta.wildcard_newsfeed.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("User Service Test")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthCodeService authCodeService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private FileService fileService;

    @InjectMocks
    private UserService userService;


    /**
     * 성공
     */
    @Test
    @DisplayName("회원가입_성공")
    void 회원가입_성공() {
        // given
        UserSignupRequestDto requestDto = UserSignupRequestDto.builder()
                .usercode("testUser1234")
                .password("currentPWD12@@")
                .email("test@gmail.com")
                .build();

        User userDomain = requestDto.toUserDomain();

        UserSignupResponseDto originResponseDto = UserSignupResponseDto.of(userDomain);

        given(userRepository.save(any(User.class))).willReturn(userDomain);

        // when
        UserSignupResponseDto newResponseDto = userService.signup(requestDto);

        // then
        Assertions.assertEquals(originResponseDto.getUsercode(), newResponseDto.getUsercode());
        Assertions.assertEquals(originResponseDto.getEmail(), newResponseDto.getEmail());
    }

    @Test
    public void testSignup_Success() {
        // given
        UserSignupRequestDto requestDto = new UserSignupRequestDto("usercode", "password", "email@example.com");

        // when
        when(userRepository.findByUsercodeOrEmail("usercode", "email@example.com"))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
//        when(authCodeService.addAuthCode(any(User.class))).thenReturn(new AuthCodeHistory());

        // then
        userService.signup(requestDto);

        verify(userRepository, times(1)).findByUsercodeOrEmail("usercode", "email@example.com");
        verify(passwordEncoder, times(1)).encode("password");
        verify(userRepository, times(1)).save(any(User.class));
        verify(authCodeService, times(1)).addAuthCode(any(User.class));
//        verify(eventPublisher, times(1)).publishEvent(any(EmailSendEvent.class));
    }
}