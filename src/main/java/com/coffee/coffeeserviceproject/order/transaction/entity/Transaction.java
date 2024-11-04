package com.coffee.coffeeserviceproject.order.transaction.entity;

import static com.coffee.coffeeserviceproject.order.transaction.type.PaymentStatusType.READY;

import com.coffee.coffeeserviceproject.member.entity.Member;
import com.coffee.coffeeserviceproject.member.entity.Roaster;
import com.coffee.coffeeserviceproject.order.transaction.dto.TransactionDto;
import com.coffee.coffeeserviceproject.order.transaction.type.PaymentMethodType;
import com.coffee.coffeeserviceproject.order.transaction.type.PaymentStatusType;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long transactionId;

  @ManyToOne
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @ManyToOne
  @JoinColumn(name = "roaster_id", nullable = false)
  private Roaster roaster;

  private String merchantUid;

  @ElementCollection
  private List<Item> items;

  private Long totalPrice;

  @Enumerated(EnumType.STRING)
  private PaymentStatusType paymentStatus;

  @Enumerated(EnumType.STRING)
  private PaymentMethodType paymentMethod;

  private LocalDateTime createdAt;

  private LocalDateTime updatedAt;

  @PrePersist
  public void onCreate() {
    this.createdAt = LocalDateTime.now();
  }

  @PreUpdate
  public void onUpdate() {
    this.updatedAt = LocalDateTime.now();
  }

  public static Transaction fromDto(TransactionDto transactionDto, Member member, Roaster roaster, List<Item> items) {

    return Transaction.builder()
        .member(member)
        .roaster(roaster)
        .paymentStatus(READY)
        .items(items)
        .merchantUid(transactionDto.getMerchant_uid())
        .paymentMethod(transactionDto.getPaymentMethod())
        .build();
  }
}

