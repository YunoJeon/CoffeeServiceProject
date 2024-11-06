package com.coffee.coffeeserviceproject.order.transaction.dto;

import com.coffee.coffeeserviceproject.order.transaction.entity.Item;
import com.coffee.coffeeserviceproject.order.transaction.entity.Transaction;
import com.coffee.coffeeserviceproject.order.transaction.type.PaymentMethodType;
import com.coffee.coffeeserviceproject.order.transaction.type.PaymentStatusType;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransactionBuyerListDto {

  private Long transactionId;

  private List<Item> items;

  private Long totalPrice;

  private PaymentStatusType paymentStatus;

  private PaymentMethodType paymentMethod;

  private LocalDateTime createdAt;

  private LocalDateTime updatedAt;

  public static TransactionBuyerListDto fromEntity(Transaction transaction, List<Item> items) {

    return TransactionBuyerListDto.builder()
        .transactionId(transaction.getTransactionId())
        .items(items)
        .totalPrice(transaction.getTotalPrice())
        .paymentStatus(transaction.getPaymentStatus())
        .paymentMethod(transaction.getPaymentMethod())
        .createdAt(transaction.getCreatedAt())
        .updatedAt(transaction.getUpdatedAt())
        .build();
  }
}
