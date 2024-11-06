package com.coffee.coffeeserviceproject.favorite.dto;

import com.coffee.coffeeserviceproject.bean.entity.Bean;
import com.coffee.coffeeserviceproject.favorite.entity.Favorite;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FavoriteDto {

  private String beanName;

  private String roasterName;

  public static FavoriteDto fromEntity(Favorite favorite) {

    Bean bean = favorite.getBean();

    return FavoriteDto.builder()
        .beanName(bean.getBeanName())
        .roasterName(bean.getRoasterName() != null ? bean.getRoasterName() : null)
        .build();
  }
}
