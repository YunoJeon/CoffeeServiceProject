package com.coffee.coffeeserviceproject.order.cart.service;

import static com.coffee.coffeeserviceproject.bean.type.PurchaseStatus.IMPOSSIBLE;
import static com.coffee.coffeeserviceproject.common.type.ErrorCode.MAX_QUANTITY;
import static com.coffee.coffeeserviceproject.common.type.ErrorCode.NOT_AVAILABLE_PURCHASE;
import static com.coffee.coffeeserviceproject.common.type.ErrorCode.NOT_FOUND_BEAN;
import static com.coffee.coffeeserviceproject.common.type.ErrorCode.NOT_FOUND_CART;
import static com.coffee.coffeeserviceproject.common.type.ErrorCode.NOT_PERMISSION;

import com.coffee.coffeeserviceproject.bean.entity.Bean;
import com.coffee.coffeeserviceproject.bean.repository.BeanRepository;
import com.coffee.coffeeserviceproject.common.exception.CustomException;
import com.coffee.coffeeserviceproject.configuration.JwtProvider;
import com.coffee.coffeeserviceproject.member.entity.Member;
import com.coffee.coffeeserviceproject.order.cart.dto.CartDto;
import com.coffee.coffeeserviceproject.order.cart.dto.CartListDto;
import com.coffee.coffeeserviceproject.order.cart.dto.CartUpdateDto;
import com.coffee.coffeeserviceproject.order.cart.entity.Cart;
import com.coffee.coffeeserviceproject.order.cart.repository.CartRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartService {

  private final CartRepository cartRepository;

  private final JwtProvider jwtProvider;

  private final BeanRepository beanRepository;

  private static final int MAX_QUANTITY_LIMIT = 100;

  @Transactional
  public void addCart(Long beanId, CartDto cartDto, String token) {

    Member member = jwtProvider.getMemberFromEmail(token);

    Bean bean = beanRepository.findById(beanId)
        .orElseThrow(() -> new CustomException(NOT_FOUND_BEAN));

    if (bean.getPurchaseStatus() == IMPOSSIBLE || bean.getPurchaseStatus() == null) {
      throw new CustomException(NOT_AVAILABLE_PURCHASE);
    }

    Cart existingCart = cartRepository.findByMemberAndBean(member, bean);

    int totalQuantity =
        (existingCart != null ? existingCart.getQuantity() : 0) + cartDto.getQuantity();

    if (totalQuantity > MAX_QUANTITY_LIMIT) {
      throw new CustomException(MAX_QUANTITY);
    }

    if (existingCart != null) {

      existingCart.setQuantity(totalQuantity);

      cartRepository.save(existingCart);

    } else {

      Cart cart = Cart.fromDto(cartDto, member, bean);

      cartRepository.save(cart);
    }
  }

  @Transactional(readOnly = true)
  public List<CartListDto> getCart(String token) {

    Member member = jwtProvider.getMemberFromEmail(token);

    List<Cart> cartList = cartRepository.findAllByMemberId(member.getId());

    return cartList.stream()
        .map(CartListDto::fromEntity)
        .toList();
  }

  @Transactional
  public void updateCart(Long id, CartUpdateDto cartUpdateDto, String token) {

    Member member = jwtProvider.getMemberFromEmail(token);

    Cart cart = cartRepository.findById(id).orElseThrow(() -> new CustomException(NOT_FOUND_CART));

    Bean bean = cart.getBean();

    if (bean.getPurchaseStatus() == IMPOSSIBLE || bean.getPurchaseStatus() == null) {
      throw new CustomException(NOT_AVAILABLE_PURCHASE);
    }

    if (!member.equals(cart.getMember())) {
      throw new CustomException(NOT_PERMISSION);
    }

    if (cartUpdateDto.getQuantity() != null) {
      cart.setQuantity(cartUpdateDto.getQuantity());
    }

    if (cartUpdateDto.getRequestNote() != null) {
      cart.setRequestNote(cartUpdateDto.getRequestNote());
    }
    cartRepository.save(cart);
  }

  @Transactional
  public void deleteCart(Long id, String token) {

    Member member = jwtProvider.getMemberFromEmail(token);

    Cart cart = cartRepository.findById(id).orElseThrow(() -> new CustomException(NOT_FOUND_CART));

    if (!member.equals(cart.getMember())) {
      throw new CustomException(NOT_PERMISSION);
    }

    cartRepository.delete(cart);
  }
}
