package com.sparta.wildcard_newsfeed.domain.post.service;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BuilderArbitraryIntrospector;
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin;
import com.sparta.wildcard_newsfeed.domain.file.service.FileService;
import com.sparta.wildcard_newsfeed.domain.post.dto.PostRequestDto;
import com.sparta.wildcard_newsfeed.domain.post.dto.PostResponseDto;
import com.sparta.wildcard_newsfeed.domain.post.entity.Post;
import com.sparta.wildcard_newsfeed.domain.post.entity.PostMedia;
import com.sparta.wildcard_newsfeed.domain.post.repository.PostMediaRepository;
import com.sparta.wildcard_newsfeed.domain.post.repository.PostRepository;
import com.sparta.wildcard_newsfeed.domain.user.dto.UserSignupRequestDto;
import com.sparta.wildcard_newsfeed.domain.user.entity.User;
import com.sparta.wildcard_newsfeed.domain.user.entity.UserRoleEnum;
import com.sparta.wildcard_newsfeed.domain.user.repository.UserRepository;
import com.sparta.wildcard_newsfeed.security.AuthenticationUser;
import com.sparta.wildcard_newsfeed.util.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PostRepository postRepository;

    @Mock
    private PostMediaRepository postMediaRepository;

    @Mock
    private FileService fileService;

    @Mock
    private FileUtils fileUtils;

    @InjectMocks
    private PostService postService;

    private static final FixtureMonkey sut = FixtureMonkey.builder()
            .defaultNotNull(Boolean.FALSE)
            .objectIntrospector(BuilderArbitraryIntrospector.INSTANCE)
            .plugin(new JakartaValidationPlugin())
            .build();

    @Test
    @DisplayName("게시글 생성_성공")
    public void addPost_OK() {
        // given
        User mockUser = User.builder()
                .usercode("testUser1234")
                .name("testUsername")
                .password("currentPWD12@@")
                .email("email")
                .userRoleEnum(UserRoleEnum.USER)
                .build();
        AuthenticationUser loginUser = new AuthenticationUser(mockUser.getUsercode(), mockUser.getPassword());
        PostRequestDto requestDto = sut.giveMeBuilder(PostRequestDto.class)
                .setNotNull("title")
                .setNotNull("content")
                .setNotNull("files")
                .sample();
        Post domainPost = requestDto.toDomainPost(mockUser);

        when(userRepository.findByUsercode(mockUser.getUsercode())).thenReturn(Optional.of(mockUser));
        when(postRepository.save(any(Post.class))).thenReturn(domainPost);

        // when
        PostResponseDto result = postService.addPost(requestDto, loginUser);

        // then
        assertEquals(domainPost.getTitle(), result.getTitle());
        assertEquals(domainPost.getContent(), result.getContent());
        assertEquals(domainPost.getCreatedAt(), result.getCreatedAt());
    }

    @Test
    @DisplayName("게시글 생성_실패 - 찾을 수 없는 사용자.")
    public void should_ThrowException_when_NotFoundUser() {
        // given
        User mockUser = User.builder()
                .usercode("testUser1234")
                .name("testUsername")
                .password("currentPWD12@@")
                .email("email")
                .userRoleEnum(UserRoleEnum.USER)
                .build();
        AuthenticationUser loginUser = new AuthenticationUser(mockUser.getUsercode(), mockUser.getPassword());
        PostRequestDto requestDto = sut.giveMeBuilder(PostRequestDto.class)
                .setNotNull("title")
                .setNotNull("content")
                .setNotNull("files")
                .sample();
        Post domainPost = requestDto.toDomainPost(mockUser);

        List<PostMedia> postMediaList = List.of(new PostMedia(/* 필요한 필드 설정 */));

        when(userRepository.findByUsercode(mockUser.getUsercode())).thenReturn(Optional.empty());

        // when

        // then
        assertThrows(IllegalArgumentException.class, () -> postService.addPost(requestDto, loginUser));
    }

    @Test
    @DisplayName("게시글 단건 조회_성공")
    public void findById_success() {
        // given
        long postId = 1L;
        User mockUser = User.builder()
                .usercode("testUser1234")
                .name("testUsername")
                .password("currentPWD12@@")
                .email("email")
                .userRoleEnum(UserRoleEnum.USER)
                .build();
        PostRequestDto requestDto = sut.giveMeBuilder(PostRequestDto.class)
                .setNotNull("title")
                .setNotNull("content")
                .setNotNull("files")
                .sample();
        Post domainPost = requestDto.toDomainPost(mockUser);

        when(postRepository.findById(postId)).thenReturn(Optional.of(domainPost));

        // when
        PostResponseDto result = postService.findById(postId);

        // then
        assertEquals(domainPost.getTitle(), result.getTitle());
        assertEquals(domainPost.getContent(), result.getContent());
        assertEquals(domainPost.getCreatedAt(), result.getCreatedAt());
    }

    @Test
    @DisplayName("게시글 단건 조회_실패 - 찾을 수 없는 게시글")
    public void should_ThrowException_when_NotFoundPost() {
        // given
        long postId = 1L;
        // when
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        //then
        assertThrows(IllegalArgumentException.class, () -> {
            postService.findById(postId);
        });
    }

    @Test
    @DisplayName("게시글 다건 조회_성공")
    public void findAll_success() {
        // given
        List<Post> postList = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            Post post = sut.giveMeBuilder(Post.class)
                    .setNotNull("user")
                    .setNotNull("title")
                    .setNotNull("content")
                    .setNotNull("likeCount")
                    .setNotNull("comments")
                    .setNotNull("postMedias")
                    .setNotNull("createAt")
                    .setNotNull("updatedAt")
                    .sample();
            post.setTestDateTime();
            postList.add(post);
        }

        when(postRepository.findAll()).thenReturn(postList);

        // when
        List<PostResponseDto> result = postService.findAll();

        // then
        List<PostResponseDto> expected = postList.stream()
                .sorted(Comparator.comparing(Post::getCreatedAt).reversed())
                .map(PostResponseDto::new)
                .toList();

        assertEquals(expected.get(0).getContent(), result.get(0).getContent());
    }


    @Test
    @DisplayName("게시글 삭제_성공")
    public void deletePost_success() {
        // given
        Long postId = 1L;
        User mockUser = User.builder()
                .usercode("testUser1234")
                .name("testUsername")
                .password("currentPWD12@@")
                .email("email")
                .userRoleEnum(UserRoleEnum.USER)
                .build();
        AuthenticationUser loginUser = new AuthenticationUser(mockUser.getUsercode(), mockUser.getPassword());

        Post post = sut.giveMeBuilder(Post.class)
                .set("user", mockUser)
                .sample();
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        doNothing().when(postRepository).delete(post);

        // when
        postService.deletePost(postId, loginUser);

        // then
        verify(postRepository).findById(postId);
        verify(postRepository).delete(post);
    }

    @Test
    @DisplayName("게시글 삭제_게시물 없음")
    public void deletePost_postNotFound() {
        // given
        Long postId = 1L;
        User mockUser = User.builder()
                .usercode("testUser1234")
                .name("testUsername")
                .password("currentPWD12@@")
                .email("email")
                .userRoleEnum(UserRoleEnum.USER)
                .build();
        AuthenticationUser loginUser = new AuthenticationUser(mockUser.getUsercode(), mockUser.getPassword());

        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            postService.deletePost(postId, loginUser);
        });

        verify(postRepository).findById(postId);
    }

}