package com.coffee.coffeeserviceproject.bean.dto;

import com.coffee.coffeeserviceproject.bean.entity.Bean;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "원두 목록 조회 DTO")
public class BeanListDto {

  @Schema(description = "원두 PK")
  private Long beanId;

  @Schema(description = "원두 이름")
  private String beanName;

  @Schema(description = "평균 별점")
  private Double averageScore;

  @Schema(description = "로스터 이름")
  private String roasterName;

  @Schema(description = "조회수")
  private Long viewCount;

  public static BeanListDto fromEntity(Bean bean) {

    return BeanListDto.builder()
        .beanId(bean.getId())
        .beanName(bean.getBeanName())
        .averageScore(bean.getAverageScore())
        .roasterName(bean.getRoasterName())
        .viewCount(bean.getViewCount())
        .build();
  }
}
