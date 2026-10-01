package com.example.forum.domain;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Follow {

    @EmbeddedId
    private FollowId followId;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("followerId")
    @JoinColumn(name = "follower_id")
    private UserEntity follower;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("followingId")
    @JoinColumn(name = "following_id")
    private UserEntity following;


    @Column(name = "followed_at", nullable = false)
    private LocalDateTime followedAt;

    @PrePersist
    protected void onFollow() {
        this.followedAt = LocalDateTime.now();
    }

}
