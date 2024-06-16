package com.sparta.wildcard_newsfeed.domain.post.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BuilderArbitraryIntrospector;
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin;
import com.sparta.wildcard_newsfeed.config.WebSecurityConfig;
import com.sparta.wildcard_newsfeed.domain.comment.dto.CommentResponseDto;
import com.sparta.wildcard_newsfeed.domain.comment.service.CommentService;
import com.sparta.wildcard_newsfeed.domain.post.dto.PostRequestDto;
import com.sparta.wildcard_newsfeed.domain.post.dto.PostResponseDto;
import com.sparta.wildcard_newsfeed.domain.post.service.PostService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.security.Principal;
import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {PostController.class},
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = WebSecurityConfig.class
                )
        })
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Post Controller Test")
class PostControllerTest {
    private MockMvc mockMvc;
    private Principal mockPrincipal;

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private PostService postService;

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
    @DisplayName("게시물 등록_성공")
    void addPost_OK() throws Exception {
        // given
        User mockUser = User.builder()
                .usercode("testUser1234")
                .name("testUsername")
                .password("currentPWD12@@")
                .email("email")
                .userRoleEnum(UserRoleEnum.USER)
                .build();
        this.mockUserDto(mockUser);

        PostRequestDto requestDto = PostRequestDto.builder()
                .title("title")
                .content("content")
                .build();

        // when
        ResultActions resultActions = mockMvc.perform(post("/api/v1/post")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .flashAttr("postRequestDto", requestDto)
                .principal(mockPrincipal));

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("게시물 등록 성공"))
        ;
    }


    @Test
    @DisplayName("게시글 등록_실패 - 제목 미입력")
    public void should_ThrowException_when_titleBlank() throws Exception {
        // given
        User mockUser = User.builder()
                .usercode("testUser1234")
                .name("testUsername")
                .password("currentPWD12@@")
                .email("email")
                .userRoleEnum(UserRoleEnum.USER)
                .build();
        this.mockUserDto(mockUser);

        PostRequestDto requestDto = PostRequestDto.builder()
//                .title("title")
                .content("content")
                .build();
        // when
        ResultActions resultActions = mockMvc.perform(post("/api/v1/post")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .flashAttr("postRequestDto", requestDto)
                .principal(mockPrincipal));

        // then
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("제목은 필수 입력 값입니다."))
        ;
    }

    @Test
    @DisplayName("게시글 등록_실패 - 내용 미입력")
    public void testAddPost() throws Exception {
        // given
        User mockUser = User.builder()
                .usercode("testUser1234")
                .name("testUsername")
                .password("currentPWD12@@")
                .email("email")
                .userRoleEnum(UserRoleEnum.USER)
                .build();
        this.mockUserDto(mockUser);

        PostRequestDto requestDto = PostRequestDto.builder()
                .title("title")
//                .content("content")
                .build();
        // when
        ResultActions resultActions = mockMvc.perform(post("/api/v1/post")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .flashAttr("postRequestDto", requestDto)
                .principal(mockPrincipal));

        // then
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("내용은 필수 입력 값입니다."));
    }

    @Test
    @DisplayName("게시글 단일 조회_성공")
    void findById_OK() throws Exception {
        // given
        Long postId = 1L;
        PostResponseDto postResponseDto = sut.giveMeOne(PostResponseDto.class);
        List<CommentResponseDto> commentResponseDtoList = sut.giveMe(CommentResponseDto.class, 2);

        when(postService.findById(postId)).thenReturn(postResponseDto);
        when(commentService.findAllCommentsByPostId(postId)).thenReturn(commentResponseDtoList);

        // when
        ResultActions resultActions = mockMvc.perform(get("/api/v1/post/{postId}", postId)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("게시글 수정_성공")
    void updatePost_OK() throws Exception {
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
        PostRequestDto requestDto = sut.giveMeBuilder(PostRequestDto.class)
                .setNotNull("title")
                .setNotNull("content")
                .sample();

        PostResponseDto postResponseDto = PostResponseDto.of(requestDto.toDomainPost(mockUser));
        when(postService.updatePost(requestDto, postId, (AuthenticationUser) authentication))
                .thenReturn(postResponseDto);

        // when
        ResultActions resultActions = mockMvc.perform(put("/api/v1/post/{postId}", postId)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .flashAttr("postRequestDto", requestDto)
                .principal(mockPrincipal));

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("게시물 수정 성공"))
        ;
    }

    @Test
    @DisplayName("게시글 수정_실패 - 제목 미입력")
    void should_ThrowException_when_updateTitleBlank() throws Exception {
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

        PostRequestDto requestDto = PostRequestDto.builder()
//                .title("title")
                .content("content")
                .build();

        // when
        ResultActions resultActions = mockMvc.perform(put("/api/v1/post/{postId}", postId)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .flashAttr("postRequestDto", requestDto)
                .principal(mockPrincipal));

        // then
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("제목은 필수 입력 값입니다."))
        ;
    }

    @Test
    @DisplayName("게시글 수정_실패 - 내용 미입력")
    void should_ThrowException_when_updateContentBlank() throws Exception {
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

        PostRequestDto requestDto = PostRequestDto.builder()
                .title("title")
//                .content("content")
                .build();

        // when
        ResultActions resultActions = mockMvc.perform(put("/api/v1/post/{postId}", postId)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .flashAttr("postRequestDto", requestDto)
                .principal(mockPrincipal));

        // then
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("내용은 필수 입력 값입니다."))
                .andDo(print())
        ;
    }

    @Test
    @DisplayName("게시글 삭제_성공")
    void deletePost_OK() throws Exception {
        // given
        Long postId = 1L;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        doNothing().when(postService).deletePost(postId, (AuthenticationUser) authentication);
        // when
        ResultActions resultActions = mockMvc.perform(delete("/api/v1/post/{postId}", postId));

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.message").value("게시물 삭제 성공"))
        ;
    }

}
