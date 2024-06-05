package com.sparta.wildcard_newsfeed.domain.post.service;

import com.sparta.wildcard_newsfeed.domain.post.dto.PostRequestDto;
import com.sparta.wildcard_newsfeed.domain.post.dto.PostResponseDto;
import com.sparta.wildcard_newsfeed.domain.post.entity.Post;
import com.sparta.wildcard_newsfeed.domain.post.repository.PostRepository;
import com.sparta.wildcard_newsfeed.domain.user.entity.User;
import com.sparta.wildcard_newsfeed.domain.user.repository.UserRepository;
import com.sparta.wildcard_newsfeed.security.AuthenticationUser;
import com.sparta.wildcard_newsfeed.security.jwt.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Transactional
    public PostResponseDto addPost(PostRequestDto postRequestDto, AuthenticationUser user) {
        User byUsercode = userRepository.findByUsercode(user.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Post post = new Post(postRequestDto, byUsercode);
        postRepository.save(post);
        return new PostResponseDto(post);
    }

    public PostResponseDto findById(long id) {
        Post post = findPostById(id);
        return new PostResponseDto(post);
    }

    public List<PostResponseDto> findAll() {
        List<Post> postlist = postRepository.findAll();
        return postlist.stream()
                .sorted(Comparator.comparing(Post::getCreatedAt).reversed())
                .map(PostResponseDto::new)
                .toList();
    }


    @Transactional
    public PostResponseDto updatePost(PostRequestDto postRequestDto, Long postId, HttpServletRequest request) {
        Post post = findPostById(postId);
        User user = validateTokenAndGetUser(request);

        validateUser(post, user.getId());

        post.update(postRequestDto);
        postRepository.save(post);
        return new PostResponseDto(post);
    }

    @Transactional
    public void deletePost(Long postId, HttpServletRequest request) {
        Post post = findPostById(postId);
        User user = validateTokenAndGetUser(request);

        validateUser(post, user.getId());

        postRepository.delete(post);
    }

    private Post findPostById(long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시물이 존재하지 않습니다."));
    }

    private void validateUser(Post post, Long userId) {
        if (!post.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("작성자만 할 수 있습니다.");
        }
    }

    private User validateTokenAndGetUser(HttpServletRequest request) {
        String token = jwtUtil.getAccessTokenFromHeader(request);
        if (token == null || !jwtUtil.validateToken(token)) {
            throw new SecurityException("유효한 토큰이 아닙니다.");
        }

        Claims claims = jwtUtil.getUserInfoFromToken(token);
        String usercode = claims.getSubject();

        return userRepository.findByUsercode(usercode)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 사용자입니다."));
    }

}