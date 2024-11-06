package com.coffee.coffeeserviceproject.order.cart.service;

import static com.coffee.coffeeserviceproject.bean.type.PurchaseStatus.IMPOSSIBLE;
import static com.coffee.coffeeserviceproject.bean.type.PurchaseStatus.POSSIBLE;
import static com.coffee.coffeeserviceproject.common.type.ErrorCode.MAX_QUANTITY;
import static com.coffee.coffeeserviceproject.common.type.ErrorCode.NOT_AVAILABLE_PURCHASE;
import static com.coffee.coffeeserviceproject.common.type.ErrorCode.NOT_FOUND_BEAN;
import static com.coffee.coffeeserviceproject.common.type.ErrorCode.NOT_FOUND_CART;
import static com.coffee.coffeeserviceproject.common.type.ErrorCode.NOT_PERMISSION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.coffee.coffeeserviceproject.bean.entity.Bean;
import com.coffee.coffeeserviceproject.bean.repository.BeanRepository;
import com.coffee.coffeeserviceproject.common.exception.CustomException;
import com.coffee.coffeeserviceproject.configuration.JwtProvider;
import com.coffee.coffeeserviceproject.member.entity.Member;
import com.coffee.coffeeserviceproject.member.entity.Roaster;
import com.coffee.coffeeserviceproject.order.cart.dto.CartDto;
import com.coffee.coffeeserviceproject.order.cart.dto.CartListDto;
import com.coffee.coffeeserviceproject.order.cart.dto.CartUpdateDto;
import com.coffee.coffeeserviceproject.order.cart.entity.Cart;
import com.coffee.coffeeserviceproject.order.cart.repository.CartRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

  @Mock
  private CartRepository cartRepository;

  @Mock
  private JwtProvider jwtProvider;

  @Mock
  private BeanRepository beanRepository;

  @InjectMocks
  private CartService cartService;

  private Member member;
  private Bean bean;

  @BeforeEach
  void setUp() {

    member = new Member();
    bean = new Bean();
    bean.setId(1L);
    bean.setPurchaseStatus(POSSIBLE);
    bean.setPrice(1000L);

  }

  @Test
  void addCart_Success() {
    // given
    CartDto cartDto = new CartDto(5, null);
    String token = "token";
    when(jwtProvider.getMemberFromEmail(token)).thenReturn(member);
    when(beanRepository.findById(bean.getId())).thenReturn(Optional.of(bean));
    when(cartRepository.findByMemberAndBean(member, bean)).thenReturn(null);
    // when
    cartService.addCart(bean.getId(), cartDto, token);
    // then
    verify(cartRepository).save(any());
  }

  @Test
  void addCart_Success_NewCart() {
    // given
    CartDto cartDto = new CartDto(5, null);
    String token = "token";
    Cart cart = new Cart();
    cart.setCartId(2L);
    when(jwtProvider.getMemberFromEmail(token)).thenReturn(member);
    when(beanRepository.findById(bean.getId())).thenReturn(Optional.of(bean));
    when(cartRepository.findByMemberAndBean(member, bean)).thenReturn(cart);
    // when
    cartService.addCart(bean.getId(), cartDto, token);
    // then
    verify(cartRepository).save(any());
  }

  @Test
  void addCart_Success_AddQuantity() {
    // given
    CartDto cartDto = new CartDto(50, null);
    String token = "token";
    Cart cart = new Cart();
    cart.setQuantity(50);
    when(jwtProvider.getMemberFromEmail(token)).thenReturn(member);
    when(beanRepository.findById(bean.getId())).thenReturn(Optional.of(bean));
    when(cartRepository.findByMemberAndBean(member, bean)).thenReturn(cart);
    // when
    cartService.addCart(bean.getId(), cartDto, token);
    // then
    verify(cartRepository).save(any());
    assertEquals(100, cart.getQuantity());
  }

  @Test
  void addCart_Failure_NotFoundBean() {
    // given
    CartDto cartDto = new CartDto(5, null);
    String token = "token";
    when(jwtProvider.getMemberFromEmail(token)).thenReturn(member);
    when(beanRepository.findById(bean.getId())).thenReturn(Optional.empty());
    // when
    CustomException e = assertThrows(CustomException.class,
        () -> cartService.addCart(bean.getId(), cartDto, token));
    // then
    assertEquals(NOT_FOUND_BEAN, e.getErrorCode());
  }

  @Test
  void addCart_Failure_NotAvailablePurchase() {
    // given
    CartDto cartDto = new CartDto(5, null);
    String token = "token";
    bean.setPurchaseStatus(IMPOSSIBLE);
    when(jwtProvider.getMemberFromEmail(token)).thenReturn(member);
    when(beanRepository.findById(bean.getId())).thenReturn(Optional.of(bean));
    // when
    CustomException e = assertThrows(CustomException.class,
        () -> cartService.addCart(bean.getId(), cartDto, token));
    // then
    assertEquals(NOT_AVAILABLE_PURCHASE, e.getErrorCode());
  }

  @Test
  void addCart_Failure_MaxQuantity() {
    // given
    CartDto cartDto = new CartDto(101, null);
    String token = "token";
    when(jwtProvider.getMemberFromEmail(token)).thenReturn(member);
    when(beanRepository.findById(bean.getId())).thenReturn(Optional.of(bean));
    // when
    CustomException e = assertThrows(CustomException.class,
        () -> cartService.addCart(bean.getId(), cartDto, token));
    // then
    assertEquals(MAX_QUANTITY, e.getErrorCode());
  }

  @Test
  void addCart_Failure_AddCartMaxQuantity() {
    // given
    CartDto cartDto = new CartDto(50, null);
    String token = "token";
    Cart cart = new Cart();
    cart.setQuantity(51);
    when(jwtProvider.getMemberFromEmail(token)).thenReturn(member);
    when(beanRepository.findById(bean.getId())).thenReturn(Optional.of(bean));
    when(cartRepository.findByMemberAndBean(member, bean)).thenReturn(cart);
    // when
    CustomException e = assertThrows(CustomException.class,
        () -> cartService.addCart(bean.getId(), cartDto, token));
    // then
    assertEquals(MAX_QUANTITY, e.getErrorCode());
  }

  @Test
  void getCart_Success() {
    // given
    String token = "token";

    Roaster roaster1 = new Roaster();
    Roaster roaster2 = new Roaster();
    Roaster roaster3 = new Roaster();

    roaster1.setRoasterName("1번");
    roaster2.setRoasterName("2번");
    roaster3.setRoasterName("3번");

    Member member1 = new Member();
    Member member2 = new Member();
    Member member3 = new Member();

    member1.setRoaster(roaster1);
    member2.setRoaster(roaster2);
    member3.setRoaster(roaster3);

    Bean bean1 = new Bean();
    Bean bean2 = new Bean();
    Bean bean3 = new Bean();

    bean1.setMember(member1);
    bean2.setMember(member2);
    bean3.setMember(member3);

    Cart cart1 = new Cart();
    Cart cart2 = new Cart();
    Cart cart3 = new Cart();

    cart1.setCartId(1L);
    cart1.setBean(bean1);
    cart2.setCartId(2L);
    cart2.setBean(bean2);
    cart3.setCartId(3L);
    cart3.setBean(bean3);

    when(jwtProvider.getMemberFromEmail(token)).thenReturn(member);
    when(cartRepository.findAllByMemberId(member.getId())).thenReturn(List.of(cart1, cart2, cart3));

    // when
    List<CartListDto> cartList = cartService.getCart(token);
    // then
    assertEquals(3, cartList.size());
    assertEquals("1번", cartList.get(0).getRoasterName());
    assertEquals("2번", cartList.get(1).getRoasterName());
    assertEquals("3번", cartList.get(2).getRoasterName());
  }

  @Test
  void getCart_Success_EmptyList() {
    // given
    String token = "token";
    when(jwtProvider.getMemberFromEmail(token)).thenReturn(member);
    when(cartRepository.findAllByMemberId(member.getId())).thenReturn(List.of());
    // when
    List<CartListDto> cartList = cartService.getCart(token);
    // then
    assertTrue(cartList.isEmpty());
  }

  @Test
  void updateCart_Success() {
    // given
    member.setId(1L);

    Cart cart = new Cart();
    cart.setCartId(1L);
    cart.setQuantity(3);
    cart.setRequestNote("fine 분쇄로 주세요");
    cart.setMember(member);
    cart.setBean(bean);

    CartUpdateDto cartUpdateDto = new CartUpdateDto();
    cartUpdateDto.setQuantity(5);
    cartUpdateDto.setRequestNote("홀빈으로 주세요");

    String token = "token";

    when(jwtProvider.getMemberFromEmail(token)).thenReturn(member);
    when(cartRepository.findById(cart.getCartId())).thenReturn(Optional.of(cart));
    // when
    cartService.updateCart(cart.getCartId(), cartUpdateDto, token);
    // then
    assertEquals(5, cart.getQuantity());
    assertEquals("홀빈으로 주세요", cart.getRequestNote());
    verify(cartRepository).save(any());
  }

  @Test
  void updateCart_Success_OnlyRequestNote() {
    // given
    member.setId(1L);

    Cart cart = new Cart();
    cart.setCartId(1L);
    cart.setQuantity(3);
    cart.setRequestNote("fine 분쇄로 주세요");
    cart.setMember(member);
    cart.setBean(bean);

    CartUpdateDto cartUpdateDto = new CartUpdateDto();
    cartUpdateDto.setRequestNote("홀빈으로 주세요");

    String token = "token";

    when(jwtProvider.getMemberFromEmail(token)).thenReturn(member);
    when(cartRepository.findById(cart.getCartId())).thenReturn(Optional.of(cart));
    // when
    cartService.updateCart(cart.getCartId(), cartUpdateDto, token);
    // then
    assertEquals(3, cart.getQuantity());
    assertEquals("홀빈으로 주세요", cart.getRequestNote());
    verify(cartRepository).save(any());
  }

  @Test
  void updateCart_Success_OnlyQuantity() {
    // given
    member.setId(1L);

    Cart cart = new Cart();
    cart.setCartId(1L);
    cart.setQuantity(3);
    cart.setRequestNote("fine 분쇄로 주세요");
    cart.setMember(member);
    cart.setBean(bean);

    CartUpdateDto cartUpdateDto = new CartUpdateDto();
    cartUpdateDto.setQuantity(5);

    String token = "token";

    when(jwtProvider.getMemberFromEmail(token)).thenReturn(member);
    when(cartRepository.findById(cart.getCartId())).thenReturn(Optional.of(cart));
    // when
    cartService.updateCart(cart.getCartId(), cartUpdateDto, token);
    // then
    assertEquals(5, cart.getQuantity());
    assertEquals("fine 분쇄로 주세요", cart.getRequestNote());
    verify(cartRepository).save(any());
  }

  @Test
  void updateCart_Failure_NotFoundCart() {
    // given
    Long cartId = 1L;
    CartUpdateDto cartUpdateDto = new CartUpdateDto();
    String token = "token";

    when(jwtProvider.getMemberFromEmail(token)).thenReturn(member);
    when(cartRepository.findById(cartId)).thenReturn(Optional.empty());
    // when
    CustomException e = assertThrows(CustomException.class,
        () -> cartService.updateCart(cartId, cartUpdateDto, token));
    // then
    assertEquals(NOT_FOUND_CART, e.getErrorCode());
    verify(cartRepository, never()).save(any());
  }

  @Test
  void deleteCart_Success() {
    // given
    member.setId(1L);

    Cart cart = new Cart();
    cart.setCartId(1L);
    cart.setMember(member);

    String token = "token";

    when(jwtProvider.getMemberFromEmail(token)).thenReturn(member);
    when(cartRepository.findById(cart.getCartId())).thenReturn(Optional.of(cart));
    // when
    cartService.deleteCart(cart.getCartId(), token);
    // then
    verify(cartRepository).delete(cart);
  }

  @Test
  void deleteCart_Failure_NotFoundCart() {
    // given
    member.setId(1L);

    String token = "token";

    Cart cart = new Cart();
    cart.setCartId(1L);

    when(jwtProvider.getMemberFromEmail(token)).thenReturn(member);
    when(cartRepository.findById(cart.getCartId())).thenReturn(Optional.empty());
    // when
    CustomException e = assertThrows(CustomException.class,
        () -> cartService.deleteCart(cart.getCartId(), token));
    // then
    assertEquals(NOT_FOUND_CART, e.getErrorCode());
    verify(cartRepository, never()).delete(any());
  }

  @Test
  void deleteCart_Failure_NotPermission() {
    // given
    String token = "token";

    member.setId(1L);
    Member otherMember = new Member();
    otherMember.setId(2L);

    Cart cart = new Cart();
    cart.setCartId(1L);
    cart.setMember(member);

    when(jwtProvider.getMemberFromEmail(token)).thenReturn(otherMember);
    when(cartRepository.findById(cart.getCartId())).thenReturn(Optional.of(cart));
    // when
    CustomException e = assertThrows(CustomException.class,
        () -> cartService.deleteCart(cart.getCartId(), token));
    // then
    assertEquals(NOT_PERMISSION, e.getErrorCode());
    verify(cartRepository, never()).delete(any());
  }
}