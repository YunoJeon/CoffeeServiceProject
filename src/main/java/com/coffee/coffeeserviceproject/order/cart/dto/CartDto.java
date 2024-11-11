package com.coffee.coffeeserviceproject.order.cart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "장바구니 추가 DTO")
public class CartDto {

  @Positive(message = "최소 1개 이상 장바구니에 담을 수 있습니다.")
  @Max(value = 100, message = "최대 수량은 100개 까지 입니다.")
  @Schema(description = "수량")
  private int quantity;

  @Schema(description = "요청사항", example = "홀빈 or 필터 분쇄로 주세요", nullable = true)
  private String requestNote;
}
