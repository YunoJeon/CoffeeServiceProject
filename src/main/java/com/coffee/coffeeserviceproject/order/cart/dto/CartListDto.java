package com.coffee.coffeeserviceproject.order.cart.dto;

import com.coffee.coffeeserviceproject.order.cart.entity.Cart;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "장바구니 조회 DTO")
public class CartListDto {

  @Schema(description = "로스터 이름")
  private String roasterName;

  @Schema(description = "원두 이름")
  private String beanName;

  @Schema(description = "수량")
  private int quantity;

  @Schema(description = "장바구니 담을 당시 가격")
  private Long priceAtAdded;

  @Schema(description = "요청사항")
  private String requestNote;

  public static CartListDto fromEntity(Cart cart) {

    return CartListDto.builder()
            .roasterName(cart.getBean().getRoasterName())
            .beanName(cart.getBean().getBeanName())
            .quantity(cart.getQuantity())
            .priceAtAdded(cart.getPriceAtAdded())
            .requestNote(cart.getRequestNote())
            .build();
  }
}
