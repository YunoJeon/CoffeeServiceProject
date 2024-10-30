package com.coffee.coffeeserviceproject.bean.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BeanListDto {

  private Long beanId;

  private String beanName;

  private Double averageScore;

  private String roasterName;
}
