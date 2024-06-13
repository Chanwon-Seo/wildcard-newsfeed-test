package com.sparta.wildcard_newsfeed.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.wildcard_newsfeed.domain.user.dto.UserSignupRequestDto;
import com.sparta.wildcard_newsfeed.domain.user.dto.UserSignupResponseDto;
import com.sparta.wildcard_newsfeed.domain.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("User Controller Test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("회원가입_성공")
    public void test1() throws Exception {
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

    /**
     * 아이디 검증
     * test1_1 아이디 미입력
     * test1_2 아이디 입력 길이 조건 미달
     * test1_3 아이디 입력 조건 미달
     */
    @Test
    @DisplayName("회원가입_실패 - 아이디_미입력")
    public void test1_1() throws Exception {

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
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    @DisplayName("회원가입_실패 - 비밀번호 미입력")
    public void test1_2() throws Exception {

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
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    @DisplayName("회원가입_실패 - 이메일 미입력")
    public void test1_3() throws Exception {

        UserSignupRequestDto requestDto = UserSignupRequestDto.builder()
                .usercode("testUser1234")
                .password("currentPWD12@@")
//                .email("test@gmail.com")
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
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.BAD_REQUEST.value()));
    }

}