package com.coffee.coffeeserviceproject.review.dto;

import com.coffee.coffeeserviceproject.review.entity.Review;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReviewResponseDto {

  private String memberName;

  private String beanName;

  private Double score;

  private String comment;

  private LocalDateTime createdAt;

  private LocalDateTime updatedAt;

  public static ReviewResponseDto myReviewList(Review review) {

    return ReviewResponseDto.builder()
            .beanName(review.getBean().getBeanName())
            .score(review.getScore())
            .comment(review.getComment())
            .createdAt(review.getCreatedAt())
            .updatedAt(review.getUpdatedAt())
            .build();
  }

  public static ReviewResponseDto getBeanReviewList(Review review) {

    return ReviewResponseDto.builder()
        .memberName(review.getMember().getMemberName())
        .beanName(review.getBean().getBeanName())
        .score(review.getScore())
        .comment(review.getComment())
        .createdAt(review.getCreatedAt())
        .updatedAt(review.getUpdatedAt())
        .build();
  }
}
