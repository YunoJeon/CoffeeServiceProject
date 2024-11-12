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
import com.coffee.coffeeserviceproject.common.model.ListWrapper;
import com.coffee.coffeeserviceproject.configuration.JwtProvider;
import com.coffee.coffeeserviceproject.member.entity.Member;
import com.coffee.coffeeserviceproject.order.cart.dto.CartDto;
import com.coffee.coffeeserviceproject.order.cart.dto.CartListDto;
import com.coffee.coffeeserviceproject.order.cart.dto.CartUpdateDto;
import com.coffee.coffeeserviceproject.order.cart.entity.Cart;
import com.coffee.coffeeserviceproject.order.cart.repository.CartRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartService {

  private final CartRepository cartRepository;

  private final JwtProvider jwtProvider;

  private final BeanRepository beanRepository;

  private final RedisTemplate<String, ListWrapper<CartListDto>> redisTemplateForCart;

  private static final int MAX_QUANTITY_LIMIT = 100;

  private static final int CACHE_TIMEOUT_DAY = 1;

  private static final String CART_CACHE_KEY_PREFIX = "cart:";

  @Transactional
  public void addCart(Long beanId, CartDto cartDto, String token) {

    Member member = getMemberFromToken(token);

    Bean bean = findByBeanIdFromBeanRepository(beanId);

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
    setCartCache(member.getId());
  }

  @Transactional(readOnly = true)
  public List<CartListDto> getCart(String token) {

    Member member = getMemberFromToken(token);

    String cartCacheKey = CART_CACHE_KEY_PREFIX + member.getId();

    ValueOperations<String, ListWrapper<CartListDto>> valueOperations = redisTemplateForCart.opsForValue();

    ListWrapper<CartListDto> cartWrapper = valueOperations.get(cartCacheKey);

    List<CartListDto> cartList;
    if (cartWrapper == null) {

      cartList = cartRepository.findAllByMemberId(member.getId()).stream()
          .map(CartListDto::fromEntity)
          .toList();

      valueOperations.set(cartCacheKey, new ListWrapper<>(cartList), CACHE_TIMEOUT_DAY, TimeUnit.DAYS);
    } else {

      cartList = cartWrapper.getList();
    }

    return cartList;
  }

  @Transactional
  public void updateCart(Long id, CartUpdateDto cartUpdateDto, String token) {

    Member member = getMemberFromToken(token);

    Cart cart = findByCartIdFromCartRepository(id);

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

    setCartCache(member.getId());
  }

  @Transactional
  public void deleteCart(Long id, String token) {

    Member member = getMemberFromToken(token);

    Cart cart = findByCartIdFromCartRepository(id);

    if (!member.equals(cart.getMember())) {
      throw new CustomException(NOT_PERMISSION);
    }

    cartRepository.delete(cart);

    deleteCartCache(member.getId());
  }

  private void setCartCache(Long memberId) {

    String cartCacheKey = CART_CACHE_KEY_PREFIX + memberId;

    ListWrapper<CartListDto> cacheCartWrapper = redisTemplateForCart.opsForValue().get(cartCacheKey);

    List<CartListDto> cartList = cartRepository.findAllByMemberId(memberId)
        .stream().map(CartListDto::fromEntity).toList();

    if (cacheCartWrapper != null) {

      Set<CartListDto> plusCartList = new HashSet<>(cacheCartWrapper.getList());

      plusCartList.addAll(cartList);

      cartList = new ArrayList<>(plusCartList);
    }

    ListWrapper<CartListDto> wrapper = new ListWrapper<>(cartList);

    redisTemplateForCart.opsForValue().set(cartCacheKey, wrapper, CACHE_TIMEOUT_DAY, TimeUnit.DAYS);
  }

  private void deleteCartCache(Long memberId) {
    String cartCacheKey = CART_CACHE_KEY_PREFIX + memberId;
    redisTemplateForCart.delete(cartCacheKey);
  }

  private Member getMemberFromToken(String token) {

    return jwtProvider.getMemberFromEmail(token);
  }

  private Bean findByBeanIdFromBeanRepository(Long id) {

    return beanRepository.findById(id).orElseThrow(() -> new CustomException(NOT_FOUND_BEAN));
  }

  private Cart findByCartIdFromCartRepository(Long id) {

    return cartRepository.findById(id).orElseThrow(() -> new CustomException(NOT_FOUND_CART));
  }
}