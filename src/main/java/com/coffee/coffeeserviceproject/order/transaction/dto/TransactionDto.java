package com.coffee.coffeeserviceproject.order.transaction.dto;

import com.coffee.coffeeserviceproject.order.transaction.entity.Item;
import com.coffee.coffeeserviceproject.order.transaction.type.PaymentMethodType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "주문서 생성 DTO")
public class TransactionDto {

  @Schema(description = "가맹점 자체 주문번호")
  private String merchantUid;

  @Schema(description = "결제 수단")
  private PaymentMethodType paymentMethod;

  @Schema(description = "상품 목록")
  private List<Item> items;
}
