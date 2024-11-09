package com.coffee.coffeeserviceproject.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReviewRequestDto {

  @NotNull(message = "별점을 등록해 주세요.")
  @Min(value = 0, message = "최소 점수는 0.0 입니다.")
  @Max(value = 5, message = "최대 점수는 5.0 입니다.")
  private Double score;

  @NotBlank(message = "리뷰를 작성해 주세요.")
  @Size(min = 10, message = "최소 10글자 이상 입력해 주세요.")
  private String comment;

  public boolean isValidScore() {
    return score % 0.5 == 0;
  }
}
