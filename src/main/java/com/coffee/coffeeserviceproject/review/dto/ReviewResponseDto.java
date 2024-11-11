package com.coffee.coffeeserviceproject.review.dto;

import com.coffee.coffeeserviceproject.review.entity.Review;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "리뷰 조회 DTO")
public class ReviewResponseDto {

  @Schema(description = "회원 이름")
  private String memberName;

  @Schema(description = "원두 이름")
  private String beanName;

  @Schema(description = "별점")
  private Double score;

  @Schema(description = "코멘트")
  private String comment;

  @Schema(description = "생성일")
  private LocalDateTime createdAt;

  @Schema(description = "수정일")
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
