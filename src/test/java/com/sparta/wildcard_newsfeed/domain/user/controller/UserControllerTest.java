package com.sparta.wildcard_newsfeed.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.sparta.wildcard_newsfeed.config.WebSecurityConfig;
import com.sparta.wildcard_newsfeed.domain.user.dto.UserResponseDto;
import com.sparta.wildcard_newsfeed.domain.user.dto.UserSignupRequestDto;
import com.sparta.wildcard_newsfeed.domain.user.dto.UserSignupResponseDto;
import com.sparta.wildcard_newsfeed.domain.user.entity.User;
import com.sparta.wildcard_newsfeed.domain.user.entity.UserRoleEnum;
import com.sparta.wildcard_newsfeed.domain.user.service.UserService;
import com.sparta.wildcard_newsfeed.security.AuthenticationUser;
import com.sparta.wildcard_newsfeed.security.MockSpringSecurityFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {UserController.class},
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = WebSecurityConfig.class
                )
        })
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("User Controller Test")
class UserControllerTest {

    private MockMvc mockMvc;
    private Principal mockPrincipal;

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final FixtureMonkey sut = FixtureMonkey.builder()
            .defaultNotNull(Boolean.FALSE)
            .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
            .build();

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity(new MockSpringSecurityFilter()))
                .build();
    }

    private void mockUserDto(User user) {
        User testUser = User.builder()
                .usercode(user.getUsercode())
                .name(user.getName())
                .password(user.getPassword())
                .email(user.getEmail())
                .userRoleEnum(UserRoleEnum.USER)
                .build();
        AuthenticationUser authenticationUser = new AuthenticationUser(testUser.getUsercode(), testUser.getPassword(), testUser.getUserRoleEnum());
        mockPrincipal = new UsernamePasswordAuthenticationToken(authenticationUser, "", authenticationUser.getAuthorities());
    }


    @Test
    @DisplayName("회원가입_성공")
    public void signup_OK() throws Exception {
        // given
        UserSignupRequestDto requestDto = UserSignupRequestDto.builder()
                .usercode("testUser1234")
                .password("currentPWD12@@")
                .email("test@gmail.com")
                .build();
        when(userService.signup(any(UserSignupRequestDto.class))).thenReturn(UserSignupResponseDto.of(requestDto.toUserDomain()));

        // when
        ResultActions resultActions = mockMvc.perform(post("/api/v1/user/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)));

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.message").value("회원가입 성공"))
                .andExpect(jsonPath("$.data.usercode").value(requestDto.getUsercode()))
                .andExpect(jsonPath("$.data.email").value(requestDto.getEmail()));
    }

    @Test
    @DisplayName("회원가입_실패 - 아이디_미입력")
    public void should_ThrowException_when_usercodeBlank() throws Exception {

        UserSignupRequestDto requestDto = UserSignupRequestDto.builder()
//                .usercode("testUser1234")
                .password("currentPWD12@@")
                .email("test@gmail.com")
                .build();
        // given
        when(userService.signup(any(UserSignupRequestDto.class))).thenReturn(UserSignupResponseDto.of(requestDto.toUserDomain()));

        // when
        ResultActions resultActions = mockMvc.perform(post("/api/v1/user/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)));

        // then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.message").value("아이디를 작성해주세요"));
    }

    @Test
    @DisplayName("회원가입_실패 - 비밀번호 미입력")
    public void should_ThrowException_when_passwordBlank() throws Exception {

        UserSignupRequestDto requestDto = UserSignupRequestDto.builder()
                .usercode("testUser1234")
//                .password("currentPWD12@@")
                .email("test@gmail.com")
                .build();
        // given
        when(userService.signup(any(UserSignupRequestDto.class))).thenReturn(UserSignupResponseDto.of(requestDto.toUserDomain()));

        // when
        ResultActions resultActions = mockMvc.perform(post("/api/v1/user/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)));

        // then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.message").value("비밀번호를 작성해주세요"));
        ;
    }

    @Test
    @DisplayName("회원가입_실패 - 이메일 미입력")
    public void should_ThrowException_when_emailBlank() throws Exception {
        // given
        UserSignupRequestDto requestDto = UserSignupRequestDto.builder()
                .usercode("testUser1234")
                .password("currentPWD12@@")
//                .email("test@gmail.com")
                .build();
        when(userService.signup(any(UserSignupRequestDto.class))).thenReturn(UserSignupResponseDto.of(requestDto.toUserDomain()));

        // when
        ResultActions resultActions = mockMvc.perform(post("/api/v1/user/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)));

        // then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.message").value("이메일을 입력해주세요."));
    }

    @Test
    @DisplayName("회원탈퇴_성공")
    void resign_OK() throws Exception {
        // given
        User user = User.builder()
                .usercode("testUser1234")
                .name("updateUser1")
                .email("update@gmail.com")
                .introduce("수정 해야할 말")
                .password("currentPWD12@@")
                .build();
        this.mockUserDto(user);

        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("password", "currentPWD12@@");

        doNothing().when(userService).resign(any(), anyString());

        // when
        ResultActions resultActions = mockMvc.perform(delete("/api/v1/user/resign")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestMap))
                .principal(mockPrincipal)
        );

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.message").value("회원탈퇴 성공"));
    }

    @Test
    @DisplayName("회원탈퇴_실패 - 찾을 수 없는 회원")
    void should_ThrowException_when_NotFoundUser() throws Exception {
        // given
        User user = User.builder()
                .usercode("testUser1234")
                .name("updateUser1")
                .email("update@gmail.com")
                .introduce("수정 해야할 말")
                .password("currentPWD12@@")
                .build();
        this.mockUserDto(user);

        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("password", "wrongPWD");

        doThrow(new NullPointerException("해당하는 회원이 없습니다!!")).when(userService).resign(any(), anyString());
        // when
        ResultActions resultActions = mockMvc.perform(delete("/api/v1/user/resign")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestMap))
                .principal(mockPrincipal)
        );

        // then
        resultActions
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.message").value("해당하는 회원이 없습니다!!"))
        ;
    }

    @Test
    @DisplayName("프로필 조회 - 성공")
    void findById_OK() throws Exception {
        // given
        Long userId = 1L;
        UserResponseDto userResponseDto = sut.giveMeOne(UserResponseDto.class);

        when(userService.findById(userId)).thenReturn(userResponseDto);

        // when
        ResultActions resultActions = mockMvc.perform(get("/api/v1/user/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.message").value("프로필 조회 성공"))
                .andExpect(jsonPath("$.data.usercode").value(userResponseDto.getUsercode()))
                .andExpect(jsonPath("$.data.name").value(userResponseDto.getName()))
                .andExpect(jsonPath("$.data.email").value(userResponseDto.getEmail()));

        verify(userService, times(1)).findById(userId);
    }

    @Test
    @DisplayName("프로필 조회 - 찾을 수 없는 사용자")
    void should_ThrowException_when_IllegalArgumentException() throws Exception {
        // given
        Long userId = 2L;

        doThrow(new IllegalArgumentException("사용자를 찾을 수 없습니다.")).when(userService).findById(any());

        // when
        ResultActions resultActions = mockMvc.perform(get("/api/v1/user/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.message").value("사용자를 찾을 수 없습니다."))
        ;
    }


    @Test
    @DisplayName("프로필 사진 업로드 - 성공")
    void uploadProfileImage_OK() throws Exception {
        // given
        Long userId = 1L;
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "test image".getBytes());
        String savedS3Url = "https://example.com/test.jpg";

        when(userService.uploadProfileImage(any(), any(Long.class), any(MultipartFile.class))).thenReturn(savedS3Url);

        // when
        ResultActions resultActions = mockMvc.perform(multipart("/api/v1/user/{userId}/profile-image", userId)
                .file(file)
                .contentType(MediaType.MULTIPART_FORM_DATA));

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.message").value("프로필 사진 업로드 성공"))
                .andExpect(jsonPath("$.data").value(savedS3Url));
    }

}