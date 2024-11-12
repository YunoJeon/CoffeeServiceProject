package com.coffee.coffeeserviceproject.order.cart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
@Schema(description = "장바구니 수정 DTO")
public class CartUpdateDto {

  @Positive(message = "최소 1개 이상 장바구니에 담을 수 있습니다.")
  @Max(value = 100, message = "최대 수량은 100개 까지 입니다.")
  @Schema(description = "수량", nullable = true)
  private Integer quantity;

  @Schema(description = "요청사항", example = "홀빈 or 필터 분쇄로 주세요", nullable = true)
  private String requestNote;
}
