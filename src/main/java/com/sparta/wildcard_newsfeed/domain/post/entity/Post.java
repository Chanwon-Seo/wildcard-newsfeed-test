package com.sparta.wildcard_newsfeed.domain.post.entity;

import com.sparta.wildcard_newsfeed.domain.comment.entity.Comment;
import com.sparta.wildcard_newsfeed.domain.common.TimeStampEntity;
import com.sparta.wildcard_newsfeed.domain.post.dto.PostRequestDto;
import com.sparta.wildcard_newsfeed.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class Post extends TimeStampEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Setter
    @Column(nullable = false)
    private Long likeCount;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostMedia> postMedias = new ArrayList<>();

    public Post(Long id, User user, String title, String content, Long likeCount, List<Comment> comments, List<PostMedia> postMedias) {
        this.user = user;
        this.title = title;
        this.content = content;
        this.likeCount = likeCount;
        this.comments = comments;
        this.postMedias = postMedias;
    }

    @Builder
    public Post(User user, String title, String content, Long likeCount, List<Comment> comments, List<PostMedia> postMedias) {
        this.user = user;
        this.title = title;
        this.content = content;
        this.likeCount = likeCount;
        this.comments = comments;
        this.postMedias = postMedias;
    }

    public Post(PostRequestDto postRequestDto, User user) {
        this.user = user;
        this.title = postRequestDto.getTitle();
        this.content = postRequestDto.getContent();
        this.likeCount = 0L;
    }

    public void update(PostRequestDto postRequestDto) {
        this.title = postRequestDto.getTitle();
        this.content = postRequestDto.getContent();
    }

    public void setTestDateTime() {
        super.setDateTimeInit();
    }
}