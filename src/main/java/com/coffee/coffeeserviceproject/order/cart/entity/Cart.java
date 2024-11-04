package com.coffee.coffeeserviceproject.order.cart.entity;

import com.coffee.coffeeserviceproject.bean.entity.Bean;
import com.coffee.coffeeserviceproject.member.entity.Member;
import com.coffee.coffeeserviceproject.order.cart.dto.CartDto;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long cartId;

  @ManyToOne
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @ManyToOne
  @JoinColumn(name = "bean_id", nullable = false)
  private Bean bean;

  private int quantity;

  private Long priceAtAdded;

  private String requestNote;

  public static Cart fromDto(CartDto cartDto, Member member, Bean bean) {

    return Cart.builder()
        .member(member)
        .bean(bean)
        .quantity(cartDto.getQuantity())
        .priceAtAdded(bean.getPrice())
        .requestNote(cartDto.getRequestNote())
        .build();
  }
}
