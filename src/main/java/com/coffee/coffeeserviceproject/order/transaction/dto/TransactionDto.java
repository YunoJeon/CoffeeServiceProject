package com.coffee.coffeeserviceproject.order.transaction.dto;

import com.coffee.coffeeserviceproject.order.transaction.entity.Item;
import com.coffee.coffeeserviceproject.order.transaction.type.PaymentMethodType;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransactionDto {

  private Long totalPrice;

  private String merchant_uid;

  private PaymentMethodType paymentMethod;

  private List<Item> items;
}
