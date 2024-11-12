package com.coffee.coffeeserviceproject.order.transaction.dto;

import com.coffee.coffeeserviceproject.order.transaction.entity.Item;
import com.coffee.coffeeserviceproject.order.transaction.entity.Transaction;
import com.coffee.coffeeserviceproject.order.transaction.type.PaymentMethodType;
import com.coffee.coffeeserviceproject.order.transaction.type.PaymentStatusType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "판매 목록 조회 DTO")
public class TransactionSellerListDto {

  @Schema(description = "주문서 ID")
  private Long transactionId;

  @Schema(description = "구매 회원 ID")
  private Long buyerId;

  @Schema(description = "구매 회원 주소")
  private String buyerAddress;

  @Schema(description = "상품 목록")
  private List<Item> items;

  @Schema(description = "총 가격")
  private Long totalPrice;

  @Schema(description = "주문 상태")
  private PaymentStatusType paymentStatus;

  @Schema(description = "결제 수단")
  private PaymentMethodType paymentMethod;

  @Schema(description = "주문서 생성일")
  private LocalDateTime createdAt;

  @Schema(description = "주문서 수정일")
  private LocalDateTime updatedAt;

  public static TransactionSellerListDto fromEntity(Transaction transaction, List<Item> items) {

    return TransactionSellerListDto.builder()
        .transactionId(transaction.getTransactionId())
        .buyerId(transaction.getMember().getId())
        .buyerAddress(transaction.getMember().getAddress())
        .items(items)
        .totalPrice(transaction.getTotalPrice())
        .paymentStatus(transaction.getPaymentStatus())
        .paymentMethod(transaction.getPaymentMethod())
        .createdAt(transaction.getCreatedAt())
        .updatedAt(transaction.getUpdatedAt())
        .build();
  }
}
