package com.coffee.coffeeserviceproject.order.cart.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CartUpdateDto {

  @Positive(message = "최소 1개 이상 장바구니에 담을 수 있습니다.")
  @Max(value = 100, message = "최대 수량은 100개 까지 입니다.")
  private Integer quantity;

  private String requestNote;
}
