package com.coffee.coffeeserviceproject.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ListResponseDto<T> {

  private T data;
  private long totalElements;
  private int totalPages;
}
