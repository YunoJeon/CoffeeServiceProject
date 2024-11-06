package com.coffee.coffeeserviceproject.order.cart.repository;

import com.coffee.coffeeserviceproject.bean.entity.Bean;
import com.coffee.coffeeserviceproject.member.entity.Member;
import com.coffee.coffeeserviceproject.order.cart.entity.Cart;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Long> {

  Cart findByMemberAndBean(Member member, Bean bean);

  List<Cart> findAllByMemberId(Long id);

  void deleteByBeanIdAndMemberId(Long beanId, Long memberId);
}
