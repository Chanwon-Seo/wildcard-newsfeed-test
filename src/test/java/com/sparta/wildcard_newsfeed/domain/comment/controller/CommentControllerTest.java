package com.sparta.wildcard_newsfeed.domain.comment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BuilderArbitraryIntrospector;
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin;
import com.sparta.wildcard_newsfeed.config.WebSecurityConfig;
import com.sparta.wildcard_newsfeed.domain.comment.dto.CommentRequestDto;
import com.sparta.wildcard_newsfeed.domain.comment.dto.CommentResponseDto;
import com.sparta.wildcard_newsfeed.domain.comment.service.CommentService;
import com.sparta.wildcard_newsfeed.domain.user.entity.User;
import com.sparta.wildcard_newsfeed.domain.user.entity.UserRoleEnum;
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
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.security.Principal;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {CommentController.class},
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = WebSecurityConfig.class
                )
        })
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Comment Controller Test")
class CommentControllerTest {
    private MockMvc mockMvc;
    private Principal mockPrincipal;

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private CommentService commentService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final FixtureMonkey sut = FixtureMonkey.builder()
            .defaultNotNull(Boolean.FALSE)
            .objectIntrospector(BuilderArbitraryIntrospector.INSTANCE)
            .plugin(new JakartaValidationPlugin())
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
    @DisplayName("댓글 등록_성공")
    public void addComment_OK() throws Exception {
        // given
        User mockUser = User.builder()
                .usercode("testUser1234")
                .name("testUsername")
                .password("currentPWD12@@")
                .email("email")
                .userRoleEnum(UserRoleEnum.USER)
                .build();
        this.mockUserDto(mockUser);

        Long postId = 1L;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        CommentRequestDto requestDto = CommentRequestDto.builder()
                .content("content")
                .build();

        CommentResponseDto commentResponseDto = sut.giveMeBuilder(CommentResponseDto.class)
                .setNotNull("id")
                .setNotNull("postId")
                .set("content", requestDto.getContent())
                .setNotNull("username")
                .setNotNull("createdAt")
                .setNotNull("updatedAt")
                .setNotNull("likeCount")
                .sample();

        when(commentService.addComment(postId, requestDto, (AuthenticationUser) authentication))
                .thenReturn(commentResponseDto);

        // when
        ResultActions resultActions = mockMvc.perform(post("/api/v1/post/{postId}/comment", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
                .principal(mockPrincipal));

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("댓글 등록 성공"))
                .andDo(print())
        ;
    }

    @Test
    @DisplayName("댓글 등록_실패 - 내용 미입력")
    public void should_ThrowException_when_contentBlank() throws Exception {
        // given
        User mockUser = User.builder()
                .usercode("testUser1234")
                .name("testUsername")
                .password("currentPWD12@@")
                .email("email")
                .userRoleEnum(UserRoleEnum.USER)
                .build();
        this.mockUserDto(mockUser);

        Long postId = 1L;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        CommentRequestDto requestDto = CommentRequestDto.builder()
//                .content("content")
                .build();

        CommentResponseDto commentResponseDto = sut.giveMeBuilder(CommentResponseDto.class)
                .setNotNull("id")
                .setNotNull("postId")
                .set("content", requestDto.getContent())
                .setNotNull("username")
                .setNotNull("createdAt")
                .setNotNull("updatedAt")
                .setNotNull("likeCount")
                .sample();

        when(commentService.addComment(postId, requestDto, (AuthenticationUser) authentication))
                .thenReturn(commentResponseDto);

        // when
        ResultActions resultActions = mockMvc.perform(post("/api/v1/post/{postId}/comment", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
                .principal(mockPrincipal));

        // then
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("댓글 내용은 필수 입력 값입니다."))
                .andDo(print())
        ;
    }


    @Test
    @DisplayName("댓글 수정_성공")
    public void updateComment_OK() throws Exception {
        // given
        User mockUser = User.builder()
                .usercode("testUser1234")
                .name("testUsername")
                .password("currentPWD12@@")
                .email("email")
                .userRoleEnum(UserRoleEnum.USER)
                .build();
        this.mockUserDto(mockUser);

        Long postId = 1L;
        Long commentId = 1L;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        CommentRequestDto requestDto = CommentRequestDto.builder()
                .content("content")
                .build();

        CommentResponseDto commentResponseDto = sut.giveMeBuilder(CommentResponseDto.class)
                .setNotNull("id")
                .setNotNull("postId")
                .set("content", requestDto.getContent())
                .setNotNull("username")
                .setNotNull("createdAt")
                .setNotNull("updatedAt")
                .setNotNull("likeCount")
                .sample();

        when(commentService.updateComment(postId, commentId, requestDto, (AuthenticationUser) authentication))
                .thenReturn(commentResponseDto);

        // when
        ResultActions resultActions = mockMvc.perform(put("/api/v1/post/{postId}/comment/{commentId}", 1L, 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
                .principal(mockPrincipal));

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("댓글 수정 성공"))
                .andDo(print())
        ;
    }

    @Test
    @DisplayName("댓글 수정_실패 - 내용 미입력")
    public void should_ThrowException_when_updateComment_contentBlank() throws Exception {
        // given
        User mockUser = User.builder()
                .usercode("testUser1234")
                .name("testUsername")
                .password("currentPWD12@@")
                .email("email")
                .userRoleEnum(UserRoleEnum.USER)
                .build();
        this.mockUserDto(mockUser);

        Long postId = 1L;
        Long commentId = 1L;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        CommentRequestDto requestDto = CommentRequestDto.builder()
//                .content("content")
                .build();

        CommentResponseDto commentResponseDto = sut.giveMeBuilder(CommentResponseDto.class)
                .setNotNull("id")
                .setNotNull("postId")
                .set("content", requestDto.getContent())
                .setNotNull("username")
                .setNotNull("createdAt")
                .setNotNull("updatedAt")
                .setNotNull("likeCount")
                .sample();

        when(commentService.updateComment(postId, commentId, requestDto, (AuthenticationUser) authentication))
                .thenReturn(commentResponseDto);

        // when
        ResultActions resultActions = mockMvc.perform(put("/api/v1/post/{postId}/comment/{commentId}", 1L, 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
                .principal(mockPrincipal));

        // then
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("댓글 내용은 필수 입력 값입니다."))
                .andDo(print())
        ;
    }

    @Test
    @DisplayName("댓글 삭제_성공")
    public void testDeleteComment_Success() throws Exception {
        // Given
        User mockUser = User.builder()
                .usercode("testUser1234")
                .name("testUsername")
                .password("currentPWD12@@")
                .email("email")
                .userRoleEnum(UserRoleEnum.USER)
                .build();
        this.mockUserDto(mockUser);
        Long postId = 1L;
        Long commentId = 1L;

        doNothing().when(commentService).deleteComment(postId, commentId, mockUser.getUsercode());

        // When
        ResultActions resultActions = mockMvc.perform(delete("/api/v1/post/{postId}/comment/{commentId}", postId, commentId)
                .contentType(MediaType.APPLICATION_JSON)
                .principal(mockPrincipal));

        // Then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("댓글 삭제 성공"))
                .andDo(print());
    }

}