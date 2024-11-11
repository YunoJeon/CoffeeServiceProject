package com.coffee.coffeeserviceproject.order.transaction.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Embeddable
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "장바구니 상품 Item List")
public class Item {

  @Schema(description = "원두 ID")
  private Long beanId;

  @Schema(description = "수량")
  private int quantity;

  @Schema(description = "가격")
  private Long price;

  public static Item of(Item item) {

    return Item.builder()
        .beanId(item.getBeanId())
        .quantity(item.getQuantity())
        .price(item.getPrice())
        .build();
  }
}
