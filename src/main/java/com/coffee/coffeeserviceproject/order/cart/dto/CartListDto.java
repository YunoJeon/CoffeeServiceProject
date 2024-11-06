package com.coffee.coffeeserviceproject.order.cart.dto;

import com.coffee.coffeeserviceproject.order.cart.entity.Cart;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CartListDto {

  private String roasterName;

  private String beanName;

  private int quantity;

  private Long priceAtAdded;

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
