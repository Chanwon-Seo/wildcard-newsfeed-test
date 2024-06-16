package com.sparta.wildcard_newsfeed.domain.comment.service;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BuilderArbitraryIntrospector;
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin;
import com.sparta.wildcard_newsfeed.domain.comment.dto.CommentRequestDto;
import com.sparta.wildcard_newsfeed.domain.comment.dto.CommentResponseDto;
import com.sparta.wildcard_newsfeed.domain.comment.entity.Comment;
import com.sparta.wildcard_newsfeed.domain.comment.repository.CommentRepository;
import com.sparta.wildcard_newsfeed.domain.post.entity.Post;
import com.sparta.wildcard_newsfeed.domain.post.repository.PostRepository;
import com.sparta.wildcard_newsfeed.domain.user.entity.User;
import com.sparta.wildcard_newsfeed.domain.user.entity.UserRoleEnum;
import com.sparta.wildcard_newsfeed.domain.user.repository.UserRepository;
import com.sparta.wildcard_newsfeed.security.AuthenticationUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {
    @Mock
    private PostRepository postRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CommentService commentService;

    private static final FixtureMonkey sut = FixtureMonkey.builder()
            .defaultNotNull(Boolean.FALSE)
            .objectIntrospector(BuilderArbitraryIntrospector.INSTANCE)
            .plugin(new JakartaValidationPlugin())
            .build();

    @Test
    @DisplayName("댓글 추가_성공")
    public void addComment_Ok() {
        // Given
        long postId = 1L;
        User mockUser = User.builder()
                .usercode("testUser1234")
                .name("testUsername")
                .password("currentPWD12@@")
                .email("email")
                .userRoleEnum(UserRoleEnum.USER)
                .build();
        AuthenticationUser loginUser = new AuthenticationUser(mockUser.getUsercode(), mockUser.getPassword());

        CommentRequestDto requestDto = sut.giveMeBuilder(CommentRequestDto.class)
                .setNotNull("content")
                .set("user", mockUser)
                .sample();

        Post post = sut.giveMeOne(Post.class);

        Comment domainComment = requestDto.toDomainComment(mockUser, post);

        when(userRepository.findByUsercode(mockUser.getUsercode())).thenReturn(Optional.of(mockUser));
        when(postRepository.findById(anyLong())).thenReturn(Optional.of(new Post()));
        when(commentRepository.save(any(Comment.class))).thenReturn(domainComment);

        // When
        CommentResponseDto responseDto = commentService.addComment(postId, requestDto, loginUser);

        // Then
        assertNotNull(responseDto);
        assertEquals(responseDto.getContent(), requestDto.getContent());
    }

    @Test
    @DisplayName("댓글 추가_실패 - 찾을 수 없는 사용자")
    public void should_ThrowException_when_NotFoundUser() {
        // Given
        long postId = 1L;
        User mockUser = User.builder()
                .usercode("testUser1234")
                .name("testUsername")
                .password("currentPWD12@@")
                .email("email")
                .userRoleEnum(UserRoleEnum.USER)
                .build();
        AuthenticationUser loginUser = new AuthenticationUser(mockUser.getUsercode(), mockUser.getPassword());

        CommentRequestDto requestDto = sut.giveMeBuilder(CommentRequestDto.class)
                .setNotNull("content")
                .set("user", mockUser)
                .sample();

        Post post = sut.giveMeOne(Post.class);

        // When
        when(userRepository.findByUsercode(mockUser.getUsercode())).thenReturn(Optional.empty());


        // Then
        assertThrows(IllegalArgumentException.class, () -> commentService.addComment(postId, requestDto, loginUser));
    }

    @Test
    @DisplayName("댓글 추가_실패 - 존재하지 않는 게시물")
    public void should_ThrowException_when_NotFoundPost() {
        // Given
        long postId = 1L;
        User mockUser = User.builder()
                .usercode("testUser1234")
                .name("testUsername")
                .password("currentPWD12@@")
                .email("email")
                .userRoleEnum(UserRoleEnum.USER)
                .build();
        AuthenticationUser loginUser = new AuthenticationUser(mockUser.getUsercode(), mockUser.getPassword());

        CommentRequestDto requestDto = sut.giveMeBuilder(CommentRequestDto.class)
                .setNotNull("content")
                .set("user", mockUser)
                .sample();
        // When
        when(userRepository.findByUsercode(mockUser.getUsercode())).thenReturn(Optional.of(mockUser));
        when(postRepository.findById(anyLong())).thenReturn(Optional.empty());


        // Then
        assertThrows(IllegalArgumentException.class, () -> commentService.addComment(postId, requestDto, loginUser));
    }


    @Test
    @DisplayName("댓글 수정_성공")
    public void updateComment_Ok() {
        // Given
        long postId = 1L;
        long commentId = 1L;
        User mockUser = User.builder()
                .usercode("testUser1234")
                .name("testUsername")
                .password("currentPWD12@@")
                .email("email")
                .userRoleEnum(UserRoleEnum.USER)
                .build();
        AuthenticationUser loginUser = new AuthenticationUser(mockUser.getUsercode(), mockUser.getPassword());

        CommentRequestDto requestDto = sut.giveMeBuilder(CommentRequestDto.class)
                .setNotNull("content")
                .sample();

        Comment existingComment = new Comment("기존 댓글 내용", mockUser, new Post());

        when(postRepository.findById(anyLong())).thenReturn(Optional.of(new Post()));
        when(commentRepository.findById(anyLong())).thenReturn(Optional.of(existingComment));
        when(commentRepository.save(any(Comment.class))).thenReturn(existingComment);

        // When
        CommentResponseDto responseDto = commentService.updateComment(postId, commentId, requestDto, loginUser);
        // Then
        assertNotNull(responseDto);
        assertEquals(requestDto.getContent(), responseDto.getContent());
    }

    @Test
    @DisplayName("댓글 수정_실패 - 존재하지 않는 게시물")
    public void should_ThrowException_when_updateComment_NotFoundPost() {
        // Given
        long postId = 1L;
        long commentId = 1L;
        User mockUser = User.builder()
                .usercode("testUser1234")
                .name("testUsername")
                .password("currentPWD12@@")
                .email("email")
                .userRoleEnum(UserRoleEnum.USER)
                .build();
        AuthenticationUser loginUser = new AuthenticationUser(mockUser.getUsercode(), mockUser.getPassword());

        CommentRequestDto requestDto = sut.giveMeBuilder(CommentRequestDto.class)
                .setNotNull("content")
                .set("user", mockUser)
                .sample();
        // When
        when(postRepository.findById(anyLong())).thenReturn(Optional.empty());


        // Then
        assertThrows(IllegalArgumentException.class, () -> commentService.updateComment(postId, commentId, requestDto, loginUser));
    }

    @Test
    @DisplayName("댓글 수정_실패 - 존재하지 않는 댓글")
    public void should_ThrowException_when_updateComment_NotFoundComment() {
        // Given
        long postId = 1L;
        long commentId = 1L;
        User mockUser = User.builder()
                .usercode("testUser1234")
                .name("testUsername")
                .password("currentPWD12@@")
                .email("email")
                .userRoleEnum(UserRoleEnum.USER)
                .build();
        AuthenticationUser loginUser = new AuthenticationUser(mockUser.getUsercode(), mockUser.getPassword());

        CommentRequestDto requestDto = sut.giveMeBuilder(CommentRequestDto.class)
                .setNotNull("content")
                .set("user", mockUser)
                .sample();
        // When
        when(postRepository.findById(anyLong())).thenReturn(Optional.of(new Post()));
        when(commentRepository.findById(anyLong())).thenReturn(Optional.empty());


        // Then
        assertThrows(IllegalArgumentException.class, () -> commentService.updateComment(postId, commentId, requestDto, loginUser));
    }

    @Test
    @DisplayName("댓글 삭제_성공")
    public void deleteComment_OK() {
        // Given
        long postId = 1L;
        long commentId = 1L;
        User mockUser = User.builder()
                .usercode("testUser1234")
                .name("testUsername")
                .password("currentPWD12@@")
                .email("email")
                .userRoleEnum(UserRoleEnum.USER)
                .build();

        Comment existingComment = new Comment("기존 댓글 내용", mockUser, new Post());

        when(postRepository.findById(anyLong())).thenReturn(Optional.of(new Post()));
        when(commentRepository.findById(anyLong())).thenReturn(Optional.of(existingComment));

        // When
        commentService.deleteComment(postId, commentId, mockUser.getUsercode());

        // Then
    }

    @Test
    @DisplayName("댓글 삭제_실패 - 존재하지 않는 게시물")
    public void should_ThrowException_when_deleteComment_NotFoundPost() {
        // Given
        long postId = 1L;
        long commentId = 1L;
        User mockUser = User.builder()
                .usercode("testUser1234")
                .name("testUsername")
                .password("currentPWD12@@")
                .email("email")
                .userRoleEnum(UserRoleEnum.USER)
                .build();

        // When
        when(postRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Then
        assertThrows(IllegalArgumentException.class, () -> commentService.deleteComment(postId, commentId, mockUser.getUsercode()));
    }

    @Test
    @DisplayName("댓글 삭제_실패 - 존재하지 않는 댓글")
    public void should_ThrowException_when_deleteComment_NotFoundComment() {
        // Given
        long postId = 1L;
        long commentId = 1L;
        User mockUser = User.builder()
                .usercode("testUser1234")
                .name("testUsername")
                .password("currentPWD12@@")
                .email("email")
                .userRoleEnum(UserRoleEnum.USER)
                .build();

        // When
        when(postRepository.findById(anyLong())).thenReturn(Optional.of(new Post()));
        when(commentRepository.findById(anyLong())).thenReturn(Optional.empty());


        // Then
        assertThrows(IllegalArgumentException.class, () -> commentService.deleteComment(postId, commentId, mockUser.getUsercode()));
    }
}