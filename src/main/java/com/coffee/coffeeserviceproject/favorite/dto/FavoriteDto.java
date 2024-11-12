package com.coffee.coffeeserviceproject.favorite.dto;

import com.coffee.coffeeserviceproject.bean.entity.Bean;
import com.coffee.coffeeserviceproject.favorite.entity.Favorite;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "즐겨찾기 조회 DTO")
public class FavoriteDto {

  @Schema(description = "원두 이름(즐겨찾기 조회 시 자동입력)")
  private String beanName;

  @Schema(description = "로스터 이름(즐겨찾기 조회 시 자동입력)")
  private String roasterName;

  public static FavoriteDto fromEntity(Favorite favorite) {

    Bean bean = favorite.getBean();

    return FavoriteDto.builder()
        .beanName(bean.getBeanName())
        .roasterName(bean.getRoasterName() != null ? bean.getRoasterName() : null)
        .build();
  }
}
