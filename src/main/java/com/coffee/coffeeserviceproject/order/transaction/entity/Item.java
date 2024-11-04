package com.coffee.coffeeserviceproject.order.transaction.entity;

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
public class Item {

  private Long beanId;

  private int quantity;

  private Long price;
}
