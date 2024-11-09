package com.coffee.coffeeserviceproject.review.entity;

import com.coffee.coffeeserviceproject.bean.entity.Bean;
import com.coffee.coffeeserviceproject.member.entity.Member;
import com.coffee.coffeeserviceproject.review.dto.ReviewRequestDto;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @ManyToOne
  @JoinColumn(name = "bean_id", nullable = false)
  private Bean bean;

  private Double score;

  private String comment;

  private LocalDateTime createdAt;

  private LocalDateTime updatedAt;

  @PrePersist
  public void onCreate() {
    this.createdAt = LocalDateTime.now();
  }

  @PreUpdate
  public void onUpdate() {
    this.updatedAt = LocalDateTime.now();
  }

  public static Review fromDto(Member member, Bean bean, ReviewRequestDto reviewRequestDto) {

    return Review.builder()
        .member(member)
        .bean(bean)
        .score(reviewRequestDto.getScore())
        .comment(reviewRequestDto.getComment())
        .build();
  }
}
