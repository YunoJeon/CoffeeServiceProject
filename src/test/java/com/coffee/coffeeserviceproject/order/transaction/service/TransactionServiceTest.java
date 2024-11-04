package com.coffee.coffeeserviceproject.order.transaction.service;

import static com.coffee.coffeeserviceproject.bean.type.PurchaseStatus.IMPOSSIBLE;
import static com.coffee.coffeeserviceproject.bean.type.PurchaseStatus.POSSIBLE;
import static com.coffee.coffeeserviceproject.common.type.ErrorCode.FAILURE_CANCEL;
import static com.coffee.coffeeserviceproject.common.type.ErrorCode.INVALID_AMOUNT;
import static com.coffee.coffeeserviceproject.common.type.ErrorCode.INVALID_STATUS;
import static com.coffee.coffeeserviceproject.common.type.ErrorCode.NOT_AVAILABLE_PURCHASE;
import static com.coffee.coffeeserviceproject.common.type.ErrorCode.NOT_FOUND_BEAN;
import static com.coffee.coffeeserviceproject.common.type.ErrorCode.NOT_FOUND_ORDER;
import static com.coffee.coffeeserviceproject.common.type.ErrorCode.NOT_PERMISSION;
import static com.coffee.coffeeserviceproject.common.type.ErrorCode.PAYMENT_ERROR;
import static com.coffee.coffeeserviceproject.common.type.ErrorCode.PRICE_CHANGE;
import static com.coffee.coffeeserviceproject.member.type.RoleType.BUYER;
import static com.coffee.coffeeserviceproject.member.type.RoleType.SELLER;
import static com.coffee.coffeeserviceproject.order.transaction.type.PaymentStatusType.CANCELED;
import static com.coffee.coffeeserviceproject.order.transaction.type.PaymentStatusType.CANCEL_REQUEST;
import static com.coffee.coffeeserviceproject.order.transaction.type.PaymentStatusType.DELIVERED;
import static com.coffee.coffeeserviceproject.order.transaction.type.PaymentStatusType.EXCHANGED;
import static com.coffee.coffeeserviceproject.order.transaction.type.PaymentStatusType.EXCHANGE_REQUEST;
import static com.coffee.coffeeserviceproject.order.transaction.type.PaymentStatusType.NOT_RECEIVED;
import static com.coffee.coffeeserviceproject.order.transaction.type.PaymentStatusType.ORDER_CONFIRMED;
import static com.coffee.coffeeserviceproject.order.transaction.type.PaymentStatusType.PAID;
import static com.coffee.coffeeserviceproject.order.transaction.type.PaymentStatusType.PURCHASE_CONFIRMED;
import static com.coffee.coffeeserviceproject.order.transaction.type.PaymentStatusType.READY;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.coffee.coffeeserviceproject.bean.entity.Bean;
import com.coffee.coffeeserviceproject.bean.repository.BeanRepository;
import com.coffee.coffeeserviceproject.common.exception.CustomException;
import com.coffee.coffeeserviceproject.configuration.JwtProvider;
import com.coffee.coffeeserviceproject.member.entity.Member;
import com.coffee.coffeeserviceproject.member.entity.Roaster;
import com.coffee.coffeeserviceproject.member.repository.RoasterRepository;
import com.coffee.coffeeserviceproject.order.cart.entity.Cart;
import com.coffee.coffeeserviceproject.order.cart.repository.CartRepository;
import com.coffee.coffeeserviceproject.order.transaction.dto.TransactionBuyerListDto;
import com.coffee.coffeeserviceproject.order.transaction.dto.TransactionDto;
import com.coffee.coffeeserviceproject.order.transaction.dto.TransactionSellerListDto;
import com.coffee.coffeeserviceproject.order.transaction.entity.Item;
import com.coffee.coffeeserviceproject.order.transaction.entity.Transaction;
import com.coffee.coffeeserviceproject.order.transaction.repository.TransactionRepository;
import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.request.CancelData;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

  @Mock
  private IamportClient iamportClient;

  @Mock
  private JwtProvider jwtProvider;

  @Mock
  private TransactionRepository transactionRepository;

  @Mock
  private BeanRepository beanRepository;

  @Mock
  private CartRepository cartRepository;

  @Mock
  private RoasterRepository roasterRepository;

  @InjectMocks
  private TransactionService transactionService;

  private Member member;
  private Member member2;
  private Roaster roaster;
  private Bean bean;
  private Bean bean2;
  private Bean bean3;
  private Cart cart;
  private Cart cart2;
  private Cart cart3;
  private Long totalPrice;

  @BeforeEach
  void setUp() {
    member = new Member();
    member.setId(1L);

    roaster = new Roaster();
    roaster.setId(1L);

    member2 = new Member();
    member2.setId(2L);
    member2.setRoaster(roaster);
    member2.setRole(SELLER);

    bean = new Bean();
    bean.setId(1L);
    bean.setBeanName("1번 원두");
    bean.setPrice(1000L);
    bean.setPurchaseStatus(POSSIBLE);
    bean.setMember(member2);

    bean2 = new Bean();
    bean2.setId(2L);
    bean2.setBeanName("2번 원두");
    bean2.setPrice(2000L);
    bean2.setPurchaseStatus(POSSIBLE);
    bean2.setMember(member2);

    bean3 = new Bean();
    bean3.setId(3L);
    bean3.setBeanName("3번 원두");
    bean3.setPrice(3000L);
    bean3.setPurchaseStatus(POSSIBLE);
    bean3.setMember(member2);

    cart = new Cart();
    cart.setCartId(1L);
    cart.setMember(member);
    cart.setBean(bean);
    cart.setPriceAtAdded(bean.getPrice());
    cart.setQuantity(1);

    cart2 = new Cart();
    cart2.setCartId(2L);
    cart2.setMember(member);
    cart2.setBean(bean2);
    cart2.setPriceAtAdded(bean2.getPrice());
    cart2.setQuantity(1);

    cart3 = new Cart();
    cart3.setCartId(3L);
    cart3.setMember(member);
    cart3.setBean(bean3);
    cart3.setPriceAtAdded(bean3.getPrice());
    cart3.setQuantity(1);

    totalPrice = (cart.getPriceAtAdded() * cart.getQuantity()) +
        (cart2.getPriceAtAdded() * cart2.getQuantity()) +
        (cart3.getPriceAtAdded() * cart3.getQuantity());
  }

  @Test
  void createOrder_Success() {
    // given
    String token = "token";

    TransactionDto transactionDto = TransactionDto.builder()
        .totalPrice(totalPrice)
        .merchant_uid("merchant_uid")
        .items(List.of(
            Item.builder()
                .beanId(cart.getBean().getId())
                .quantity(cart.getQuantity())
                .price(cart.getPriceAtAdded())
                .build(),
            Item.builder()
                .beanId(cart2.getBean().getId())
                .quantity(cart2.getQuantity())
                .price(cart2.getPriceAtAdded())
                .build(),
            Item.builder()
                .beanId(cart3.getBean().getId())
                .quantity(cart3.getQuantity())
                .price(cart3.getPriceAtAdded())
                .build()
        )).build();

    when(jwtProvider.getMemberFromEmail(token)).thenReturn(member);
    when(beanRepository.findById(bean.getId())).thenReturn(Optional.of(bean));
    when(beanRepository.findById(bean2.getId())).thenReturn(Optional.of(bean2));
    when(beanRepository.findById(bean3.getId())).thenReturn(Optional.of(bean3));
    when(roasterRepository.findById(roaster.getId())).thenReturn(Optional.of(roaster));
    // when
    transactionService.createOrder(transactionDto, token);
    // then
    verify(transactionRepository).save(any());
  }

  @Test
  void createOrder_Failure_NotFoundBean() {
    // given
    String token = "token";

    TransactionDto transactionDto = TransactionDto.builder()
        .totalPrice(totalPrice)
        .merchant_uid("merchant_uid")
        .items(List.of(
            Item.builder()
                .beanId(cart.getBean().getId())
                .quantity(cart.getQuantity())
                .price(cart.getPriceAtAdded())
                .build(),
            Item.builder()
                .beanId(cart2.getBean().getId())
                .quantity(cart2.getQuantity())
                .price(cart2.getPriceAtAdded())
                .build(),
            Item.builder()
                .beanId(cart3.getBean().getId())
                .quantity(cart3.getQuantity())
                .price(cart3.getPriceAtAdded())
                .build(),
            Item.builder()
                .beanId(4L)
                .build()
        )).build();

    when(jwtProvider.getMemberFromEmail(token)).thenReturn(member);
    when(beanRepository.findById(bean.getId())).thenReturn(Optional.of(bean));
    when(beanRepository.findById(bean2.getId())).thenReturn(Optional.of(bean2));
    when(beanRepository.findById(bean3.getId())).thenReturn(Optional.of(bean3));
    when(beanRepository.findById(4L)).thenReturn(Optional.empty());
    when(roasterRepository.findById(roaster.getId())).thenReturn(Optional.of(roaster));
    // when
    CustomException e = assertThrows(CustomException.class,
        () -> transactionService.createOrder(transactionDto, token));
    // then
    assertEquals(NOT_FOUND_BEAN, e.getErrorCode());
    verify(transactionRepository, never()).save(any());
  }

  @Test
  void createOrder_Failure_NotAvailablePurchase() {
    // given
    String token = "token";
    bean.setPurchaseStatus(IMPOSSIBLE);

    TransactionDto transactionDto = TransactionDto.builder()
        .totalPrice(totalPrice)
        .merchant_uid("merchant_uid")
        .items(List.of(
            Item.builder()
                .beanId(cart.getBean().getId())
                .quantity(cart.getQuantity())
                .price(cart.getPriceAtAdded())
                .build(),
            Item.builder()
                .beanId(cart2.getBean().getId())
                .quantity(cart2.getQuantity())
                .price(cart2.getPriceAtAdded())
                .build(),
            Item.builder()
                .beanId(cart3.getBean().getId())
                .quantity(cart3.getQuantity())
                .price(cart3.getPriceAtAdded())
                .build()
        )).build();

    when(jwtProvider.getMemberFromEmail(token)).thenReturn(member);
    when(beanRepository.findById(bean.getId())).thenReturn(Optional.of(bean));
    when(roasterRepository.findById(roaster.getId())).thenReturn(Optional.of(roaster));
    // when
    CustomException e = assertThrows(CustomException.class,
        () -> transactionService.createOrder(transactionDto, token));
    // then
    assertEquals(NOT_AVAILABLE_PURCHASE, e.getErrorCode());
    assertEquals("구매가 불가능한 상품입니다.-> " + bean.getBeanName(), e.getMessage());
    verify(transactionRepository, never()).save(any());
  }


  @Test
  void createOrder_Failure_PriceChange() {
    // given
    String token = "token";

    TransactionDto transactionDto = TransactionDto.builder()
        .totalPrice(totalPrice)
        .merchant_uid("merchant_uid")
        .items(List.of(
            Item.builder()
                .beanId(cart.getBean().getId())
                .quantity(cart.getQuantity())
                .price(cart.getPriceAtAdded())
                .build(),
            Item.builder()
                .beanId(cart2.getBean().getId())
                .quantity(cart2.getQuantity())
                .price(cart2.getPriceAtAdded())
                .build(),
            Item.builder()
                .beanId(cart3.getBean().getId())
                .quantity(cart3.getQuantity())
                .price(cart3.getPriceAtAdded())
                .build()
        )).build();

    when(jwtProvider.getMemberFromEmail(token)).thenReturn(member);
    when(beanRepository.findById(bean.getId())).thenReturn(Optional.of(bean));
    when(roasterRepository.findById(roaster.getId())).thenReturn(Optional.of(roaster));

    bean.setPrice(10000L);
    // when
    CustomException e = assertThrows(CustomException.class,
        () -> transactionService.createOrder(transactionDto, token));
    // then
    assertEquals(PRICE_CHANGE, e.getErrorCode());
    assertEquals("상품 가격이 변경되었습니다.-> " + bean.getBeanName(), e.getMessage());
    verify(transactionRepository, never()).save(any());
  }

  @Test
  void validateOrder_Success() throws IamportResponseException, IOException {
    // given
    String imp_uid = "imp_uid_123456789";
    String merchant_uid = "merchant_uid_123456789";
    Payment payment = mock(Payment.class);
    totalPrice = (cart.getPriceAtAdded() * cart.getQuantity()) +
        (cart2.getPriceAtAdded() * cart2.getQuantity());

    when(payment.getStatus()).thenReturn("paid");
    when(payment.getAmount()).thenReturn(BigDecimal.valueOf(totalPrice));
    when(payment.getMerchantUid()).thenReturn(merchant_uid);

    IamportResponse<Payment> response = mock(IamportResponse.class);
    when(response.getResponse()).thenReturn(payment);
    when(iamportClient.paymentByImpUid(imp_uid)).thenReturn(response);

    Transaction transaction = Transaction.builder()
        .transactionId(1L)
        .member(member)
        .merchantUid(merchant_uid)
        .paymentStatus(READY)
        .items(
            List.of(
                Item.builder()
                    .beanId(cart.getBean().getId())
                    .quantity(cart.getQuantity())
                    .price(cart.getPriceAtAdded())
                    .build(),
                Item.builder()
                    .beanId(cart2.getBean().getId())
                    .quantity(cart2.getQuantity())
                    .price(cart2.getPriceAtAdded())
                    .build()
            )).build();

    when(transactionRepository.findByMerchantUid(payment.getMerchantUid())).thenReturn(
        Optional.of(transaction));
    // when
    IamportResponse<Payment> iamportResponse = transactionService.validateOrder(imp_uid);
    // then
    assertNotNull(iamportResponse);
    assertEquals("paid", iamportResponse.getResponse().getStatus());
    assertEquals(merchant_uid, iamportResponse.getResponse().getMerchantUid());
    assertEquals(PAID, transaction.getPaymentStatus());
    assertEquals(totalPrice, transaction.getTotalPrice());
    verify(transactionRepository).save(any());
    for (Item item : transaction.getItems()) {
      verify(cartRepository).deleteByBeanIdAndMemberId(item.getBeanId(), member.getId());
    }
  }

  @Test
  void validateOrder_Failure_InvalidStatus() throws IamportResponseException, IOException {
    // given
    String imp_uid = "imp_uid_123456789";
    String merchant_uid = "merchant_uid_123456789";

    Payment payment = mock(Payment.class);
    when(payment.getMerchantUid()).thenReturn(merchant_uid);
    when(payment.getStatus()).thenReturn("ready");

    IamportResponse<Payment> response = mock(IamportResponse.class);
    when(response.getResponse()).thenReturn(payment);
    when(iamportClient.paymentByImpUid(imp_uid)).thenReturn(response);

    Transaction transaction = new Transaction();

    when(transactionRepository.findByMerchantUid(payment.getMerchantUid())).thenReturn(
        Optional.of(transaction));

    // when
    CustomException e = assertThrows(CustomException.class,
        () -> transactionService.validateOrder(imp_uid));
    // then
    assertEquals(INVALID_STATUS, e.getErrorCode());
    verify(transactionRepository, never()).save(any());
  }

  @Test
  void validateOrder_Failure_NotFoundOrder() throws IamportResponseException, IOException {
    // given
    String imp_uid = "imp_uid_123456789";
    String merchant_uid = "merchant_uid_123456789";

    Payment payment = mock(Payment.class);
    when(payment.getMerchantUid()).thenReturn(merchant_uid);

    IamportResponse<Payment> response = mock(IamportResponse.class);
    when(response.getResponse()).thenReturn(payment);
    when(iamportClient.paymentByImpUid(imp_uid)).thenReturn(response);
    when(transactionRepository.findByMerchantUid(merchant_uid)).thenReturn(Optional.empty());
    // when
    CustomException e = assertThrows(CustomException.class,
        () -> transactionService.validateOrder(imp_uid));
    // then
    assertEquals(NOT_FOUND_ORDER, e.getErrorCode());
    verify(transactionRepository, never()).save(any());
  }

  @Test
  void validateOrder_Failure_InvalidAmount() throws IamportResponseException, IOException {
    // given
    String imp_uid = "imp_uid_123456789";
    String merchant_uid = "merchant_uid_123456789";
    totalPrice = (cart.getPriceAtAdded() * cart.getQuantity()) +
        (cart2.getPriceAtAdded() * cart2.getQuantity());

    Payment payment = mock(Payment.class);
    when(payment.getStatus()).thenReturn("paid");
    when(payment.getAmount()).thenReturn(BigDecimal.valueOf(totalPrice - 1000));
    when(payment.getMerchantUid()).thenReturn(merchant_uid);

    IamportResponse<Payment> response = mock(IamportResponse.class);
    when(response.getResponse()).thenReturn(payment);
    when(iamportClient.paymentByImpUid(imp_uid)).thenReturn(response);

    Transaction transaction = Transaction.builder()
        .transactionId(1L)
        .member(member)
        .merchantUid(merchant_uid)
        .paymentStatus(READY)
        .items(
            List.of(
                Item.builder()
                    .beanId(cart.getBean().getId())
                    .quantity(cart.getQuantity())
                    .price(cart.getPriceAtAdded())
                    .build(),
                Item.builder()
                    .beanId(cart2.getBean().getId())
                    .quantity(cart2.getQuantity())
                    .price(cart2.getPriceAtAdded())
                    .build()
            )).build();

    when(transactionRepository.findByMerchantUid(payment.getMerchantUid())).thenReturn(
        Optional.of(transaction));
    // when
    CustomException e = assertThrows(CustomException.class,
        () -> transactionService.validateOrder(imp_uid));
    // then
    assertEquals(INVALID_AMOUNT, e.getErrorCode());
    verify(transactionRepository, never()).save(any());
  }

  @Test
  void validateOrder_Failure_PaymentError_IamportResponse()
      throws IamportResponseException, IOException {
    // given
    String imp_uid = "imp_uid_123456789";
    when(iamportClient.paymentByImpUid(imp_uid)).thenThrow(IamportResponseException.class);
    // when
    CustomException e = assertThrows(CustomException.class,
        () -> transactionService.validateOrder(imp_uid));
    // then
    assertEquals(PAYMENT_ERROR, e.getErrorCode());
  }

  @Test
  void validateOrder_Failure_PaymentError_IOE()
      throws IamportResponseException, IOException {
    // given
    String imp_uid = "imp_uid_123456789";
    when(iamportClient.paymentByImpUid(imp_uid)).thenThrow(IOException.class);
    // when
    CustomException e = assertThrows(CustomException.class,
        () -> transactionService.validateOrder(imp_uid));
    // then
    assertEquals(PAYMENT_ERROR, e.getErrorCode());
  }

  @Test
  void cancelOrder_Success() throws IamportResponseException, IOException {
    // given
    String imp_uid = "imp_uid_123456789";
    String merchant_uid = "merchant_uid_123456789";

    Payment payment = mock(Payment.class);
    when(payment.getStatus()).thenReturn("paid");
    when(payment.getMerchantUid()).thenReturn(merchant_uid);

    IamportResponse<Payment> response = mock(IamportResponse.class);
    when(response.getResponse()).thenReturn(payment);
    when(iamportClient.paymentByImpUid(imp_uid)).thenReturn(response);

    IamportResponse<Payment> cancelResponse = mock(IamportResponse.class);

    Payment cancelPayment = mock(Payment.class);
    when(cancelPayment.getStatus()).thenReturn("cancelled");

    when(cancelResponse.getResponse()).thenReturn(cancelPayment);
    when(iamportClient.cancelPaymentByImpUid(any(CancelData.class))).thenReturn(cancelResponse);

    Transaction transaction = Transaction.builder()
        .transactionId(1L)
        .member(member)
        .merchantUid(merchant_uid)
        .paymentStatus(CANCEL_REQUEST)
        .build();

    when(transactionRepository.findByMerchantUid(payment.getMerchantUid())).thenReturn(
        Optional.of(transaction));
    // when
    transactionService.cancelOrder(imp_uid);
    // then
    assertEquals(CANCELED, transaction.getPaymentStatus());
    verify(transactionRepository).save(any());
  }

  @Test
  void cancelOrder_Failure_NotFoundOrder() throws IamportResponseException, IOException {
    // given
    String imp_uid = "imp_uid_123456789";

    Payment payment = mock(Payment.class);

    IamportResponse<Payment> response = mock(IamportResponse.class);
    when(response.getResponse()).thenReturn(payment);
    when(iamportClient.paymentByImpUid(imp_uid)).thenReturn(response);

    when(transactionRepository.findByMerchantUid(payment.getMerchantUid())).thenReturn(
        Optional.empty());
    // when
    CustomException e = assertThrows(CustomException.class,
        () -> transactionService.cancelOrder(imp_uid));
    // then
    assertEquals(NOT_FOUND_ORDER, e.getErrorCode());
    verify(transactionRepository, never()).save(any());
  }

  @Test
  void cancelOrder_Failure_InvalidStatus() throws IamportResponseException, IOException {
    // given
    String imp_uid = "imp_uid_123456789";

    Payment payment = mock(Payment.class);
    when(payment.getStatus()).thenReturn("paid");

    IamportResponse<Payment> response = mock(IamportResponse.class);
    when(response.getResponse()).thenReturn(payment);
    when(iamportClient.paymentByImpUid(imp_uid)).thenReturn(response);

    Transaction transaction = Transaction.builder()
        .transactionId(1L)
        .member(member)
        .paymentStatus(READY)
        .build();

    when(transactionRepository.findByMerchantUid(payment.getMerchantUid())).thenReturn(
        Optional.of(transaction));
    // when
    CustomException e = assertThrows(CustomException.class,
        () -> transactionService.cancelOrder(imp_uid));
    // then
    assertEquals(INVALID_STATUS, e.getErrorCode());
    verify(transactionRepository, never()).save(any());
  }

  @Test
  void cancelOrder_Failure_FailureCancel() throws IamportResponseException, IOException {
    // given
    String imp_uid = "imp_uid_123456789";
    String merchant_uid = "merchant_uid_123456789";

    Payment payment = mock(Payment.class);
    when(payment.getStatus()).thenReturn("paid");
    when(payment.getMerchantUid()).thenReturn(merchant_uid);

    IamportResponse<Payment> response = mock(IamportResponse.class);
    when(response.getResponse()).thenReturn(payment);
    when(iamportClient.paymentByImpUid(imp_uid)).thenReturn(response);

    IamportResponse<Payment> cancelResponse = mock(IamportResponse.class);

    Payment cancelPayment = mock(Payment.class);
    when(cancelPayment.getStatus()).thenReturn("paid");

    when(cancelResponse.getResponse()).thenReturn(cancelPayment);
    when(iamportClient.cancelPaymentByImpUid(any(CancelData.class))).thenReturn(cancelResponse);

    Transaction transaction = Transaction.builder()
        .transactionId(1L)
        .member(member)
        .merchantUid(merchant_uid)
        .paymentStatus(CANCEL_REQUEST)
        .build();

    when(transactionRepository.findByMerchantUid(payment.getMerchantUid())).thenReturn(
        Optional.of(transaction));
    // when
    CustomException e = assertThrows(CustomException.class,
        () -> transactionService.cancelOrder(imp_uid));
    // then
    assertEquals(FAILURE_CANCEL, e.getErrorCode());
    verify(transactionRepository, never()).save(any());
  }

  @Test
  void getBuyerPurchaseList_Success() {
    // given
    String token = "token";
    Pageable pageable = Pageable.ofSize(10);

    Transaction transaction = Transaction.builder()
        .transactionId(1L)
        .member(member)
        .paymentStatus(PAID)
        .items(
            List.of(
                Item.builder()
                    .beanId(cart.getBean().getId())
                    .quantity(cart.getQuantity())
                    .price(cart.getPriceAtAdded())
                    .build(),
                Item.builder()
                    .beanId(cart2.getBean().getId())
                    .quantity(cart2.getQuantity())
                    .price(cart2.getPriceAtAdded())
                    .build()
            )).build();

    Page<Transaction> transactionPage = new PageImpl<>(List.of(transaction));
    when(jwtProvider.getMemberFromEmail(token)).thenReturn(member);
    when(transactionRepository.findByMemberId(member.getId(), pageable)).thenReturn(
        transactionPage);
    // when
    Page<TransactionBuyerListDto> transactionBuyerListDtoPage = transactionService.getBuyerPurchaseList(
        pageable, token);
    // then
    assertNotNull(transactionBuyerListDtoPage);
    assertEquals(1, transactionBuyerListDtoPage.getTotalElements());
  }

  @Test
  void getBuyerPurchaseList_Success_EmptyList() {
    // given
    String token = "token";
    Pageable pageable = Pageable.ofSize(10);

    Page<Transaction> transactionPage = new PageImpl<>(List.of());
    when(jwtProvider.getMemberFromEmail(token)).thenReturn(member);
    when(transactionRepository.findByMemberId(member.getId(), pageable)).thenReturn(
        transactionPage);
    // when
    Page<TransactionBuyerListDto> transactionBuyerListDtoPage = transactionService.getBuyerPurchaseList(
        pageable, token);
    // then
    assertNotNull(transactionBuyerListDtoPage);
    assertTrue(transactionBuyerListDtoPage.isEmpty());
  }

  @Test
  void updateBuyerPurchaseList_Success_CancelRequest() {
    // given
    String token = "token";

    Transaction transaction = Transaction.builder()
        .transactionId(1L)
        .member(member)
        .paymentStatus(PAID)
        .items(
            List.of(
                Item.builder()
                    .beanId(cart.getBean().getId())
                    .quantity(cart.getQuantity())
                    .price(cart.getPriceAtAdded())
                    .build(),
                Item.builder()
                    .beanId(cart2.getBean().getId())
                    .quantity(cart2.getQuantity())
                    .price(cart2.getPriceAtAdded())
                    .build()
            )).build();

    when(jwtProvider.getMemberFromEmail(token)).thenReturn(member);
    when(transactionRepository.findById(transaction.getTransactionId())).thenReturn(
        Optional.of(transaction));
    // when
    transactionService.updateBuyerPurchase(transaction.getTransactionId(), CANCEL_REQUEST, token);
    // then
    verify(transactionRepository).save(any());
    assertEquals(CANCEL_REQUEST, transaction.getPaymentStatus());
  }

  @Test
  void updateBuyerPurchaseList_Success_PurchaseConfirmed() {
    // given
    String token = "token";

    Transaction transaction = Transaction.builder()
        .transactionId(1L)
        .member(member)
        .paymentStatus(DELIVERED)
        .items(
            List.of(
                Item.builder()
                    .beanId(cart.getBean().getId())
                    .quantity(cart.getQuantity())
                    .price(cart.getPriceAtAdded())
                    .build(),
                Item.builder()
                    .beanId(cart2.getBean().getId())
                    .quantity(cart2.getQuantity())
                    .price(cart2.getPriceAtAdded())
                    .build()
            )).build();

    when(jwtProvider.getMemberFromEmail(token)).thenReturn(member);
    when(transactionRepository.findById(transaction.getTransactionId())).thenReturn(
        Optional.of(transaction));
    // when
    transactionService.updateBuyerPurchase(transaction.getTransactionId(), PURCHASE_CONFIRMED,
        token);
    // then
    verify(transactionRepository).save(any());
    assertEquals(PURCHASE_CONFIRMED, transaction.getPaymentStatus());
  }

  @Test
  void updateBuyerPurchaseList_Failure_NotPermission() {
    // given
    String token = "token";

    Transaction transaction = Transaction.builder()
        .transactionId(1L)
        .member(member)
        .paymentStatus(PAID)
        .items(
            List.of(
                Item.builder()
                    .beanId(cart.getBean().getId())
                    .quantity(cart.getQuantity())
                    .price(cart.getPriceAtAdded())
                    .build(),
                Item.builder()
                    .beanId(cart2.getBean().getId())
                    .quantity(cart2.getQuantity())
                    .price(cart2.getPriceAtAdded())
                    .build()
            )).build();

    when(jwtProvider.getMemberFromEmail(token)).thenReturn(member2);
    when(transactionRepository.findById(transaction.getTransactionId())).thenReturn(
        Optional.of(transaction));
    // when
    CustomException e = assertThrows(CustomException.class,
        () -> transactionService.updateBuyerPurchase(
            transaction.getTransactionId(), CANCEL_REQUEST, token));
    // then
    verify(transactionRepository, never()).save(any());
    assertEquals(NOT_PERMISSION, e.getErrorCode());
  }

  @Test
  void updateBuyerPurchaseList_Failure_InvalidStatus() {
    // given
    String token = "token";

    Transaction transaction = Transaction.builder()
        .transactionId(1L)
        .member(member)
        .paymentStatus(PAID)
        .items(
            List.of(
                Item.builder()
                    .beanId(cart.getBean().getId())
                    .quantity(cart.getQuantity())
                    .price(cart.getPriceAtAdded())
                    .build(),
                Item.builder()
                    .beanId(cart2.getBean().getId())
                    .quantity(cart2.getQuantity())
                    .price(cart2.getPriceAtAdded())
                    .build()
            )).build();

    when(jwtProvider.getMemberFromEmail(token)).thenReturn(member);
    when(transactionRepository.findById(transaction.getTransactionId())).thenReturn(
        Optional.of(transaction));
    // when
    CustomException e = assertThrows(CustomException.class,
        () -> transactionService.updateBuyerPurchase(
            transaction.getTransactionId(), NOT_RECEIVED, token));
    // then
    verify(transactionRepository, never()).save(any());
    assertEquals(INVALID_STATUS, e.getErrorCode());
  }

  @Test
  void getSellerPurchaseList_Success() {
    // given
    String token = "token";
    Pageable pageable = Pageable.ofSize(10);

    Transaction transaction = Transaction.builder()
        .transactionId(1L)
        .member(member)
        .roaster(roaster)
        .paymentStatus(PAID)
        .items(
            List.of(
                Item.builder()
                    .beanId(cart.getBean().getId())
                    .quantity(cart.getQuantity())
                    .price(cart.getPriceAtAdded())
                    .build(),
                Item.builder()
                    .beanId(cart2.getBean().getId())
                    .quantity(cart2.getQuantity())
                    .price(cart2.getPriceAtAdded())
                    .build()
            )).build();

    Page<Transaction> transactionPage = new PageImpl<>(List.of(transaction));
    when(jwtProvider.getMemberFromEmail(token)).thenReturn(member2);
    when(transactionRepository.findByRoasterId(roaster.getId(), pageable)).thenReturn(
        transactionPage);
    // when
    Page<TransactionSellerListDto> transactionSellerListDtoPage = transactionService.getSellerPurchaseList(
        pageable, token);
    // then
    assertNotNull(transactionSellerListDtoPage);
    assertEquals(1, transactionSellerListDtoPage.getTotalElements());
  }

  @Test
  void getSellerPurchaseList_Success_EmptyList() {
    // given
    String token = "token";
    Pageable pageable = Pageable.ofSize(10);

    Page<Transaction> transactionPage = new PageImpl<>(List.of());
    when(jwtProvider.getMemberFromEmail(token)).thenReturn(member2);
    when(transactionRepository.findByRoasterId(roaster.getId(), pageable)).thenReturn(
        transactionPage);
    // when
    Page<TransactionSellerListDto> transactionSellerListDtoPage = transactionService.getSellerPurchaseList(
        pageable, token);
    // then
    assertNotNull(transactionSellerListDtoPage);
    assertTrue(transactionSellerListDtoPage.isEmpty());
  }

  @Test
  void getSellerPurchaseList_Failure_NotPermission() {
    // given
    String token = "token";
    Pageable pageable = Pageable.ofSize(10);
    member2.setRole(BUYER);

    when(jwtProvider.getMemberFromEmail(token)).thenReturn(member2);
    // when
    CustomException e = assertThrows(CustomException.class,
        () -> transactionService.getSellerPurchaseList(
            pageable, token));
    // then
    assertEquals(NOT_PERMISSION, e.getErrorCode());
  }

  @Test
  void updateSellerPurchaseList_Success_OrderConfirmed() {
    // given
    String token = "token";

    Transaction transaction = Transaction.builder()
        .transactionId(1L)
        .member(member)
        .roaster(roaster)
        .paymentStatus(PAID)
        .items(
            List.of(
                Item.builder()
                    .beanId(cart.getBean().getId())
                    .quantity(cart.getQuantity())
                    .price(cart.getPriceAtAdded())
                    .build(),
                Item.builder()
                    .beanId(cart2.getBean().getId())
                    .quantity(cart2.getQuantity())
                    .price(cart2.getPriceAtAdded())
                    .build()
            )).build();

    when(jwtProvider.getMemberFromEmail(token)).thenReturn(member2);
    when(transactionRepository.findById(transaction.getTransactionId())).thenReturn(
        Optional.of(transaction));
    // when
    transactionService.updateSellerPurchase(transaction.getTransactionId(), ORDER_CONFIRMED, token);
    // then
    verify(transactionRepository).save(any());
    assertEquals(ORDER_CONFIRMED, transaction.getPaymentStatus());
  }

  @Test
  void updateSellerPurchaseList_Success_Exchanged() {
    // given
    String token = "token";

    Transaction transaction = Transaction.builder()
        .transactionId(1L)
        .member(member)
        .roaster(roaster)
        .paymentStatus(EXCHANGE_REQUEST)
        .items(
            List.of(
                Item.builder()
                    .beanId(cart.getBean().getId())
                    .quantity(cart.getQuantity())
                    .price(cart.getPriceAtAdded())
                    .build(),
                Item.builder()
                    .beanId(cart2.getBean().getId())
                    .quantity(cart2.getQuantity())
                    .price(cart2.getPriceAtAdded())
                    .build()
            )).build();

    when(jwtProvider.getMemberFromEmail(token)).thenReturn(member2);
    when(transactionRepository.findById(transaction.getTransactionId())).thenReturn(
        Optional.of(transaction));
    // when
    transactionService.updateSellerPurchase(transaction.getTransactionId(), EXCHANGED, token);
    // then
    verify(transactionRepository).save(any());
    assertEquals(EXCHANGED, transaction.getPaymentStatus());
  }

  @Test
  void updateSellerPurchaseList_Failure_NotPermission_NotSeller() {
    // given
    String token = "token";
    member2.setRole(BUYER);

    Transaction transaction = Transaction.builder()
        .transactionId(1L)
        .member(member)
        .roaster(roaster)
        .paymentStatus(EXCHANGE_REQUEST)
        .items(
            List.of(
                Item.builder()
                    .beanId(cart.getBean().getId())
                    .quantity(cart.getQuantity())
                    .price(cart.getPriceAtAdded())
                    .build(),
                Item.builder()
                    .beanId(cart2.getBean().getId())
                    .quantity(cart2.getQuantity())
                    .price(cart2.getPriceAtAdded())
                    .build()
            )).build();

    when(jwtProvider.getMemberFromEmail(token)).thenReturn(member2);
    // when
    CustomException e = assertThrows(CustomException.class,
        () -> transactionService.updateSellerPurchase(
            transaction.getTransactionId(), EXCHANGED, token));
    // then
    verify(transactionRepository, never()).save(any());
    assertEquals(NOT_PERMISSION, e.getErrorCode());
  }

  @Test
  void updateSellerPurchaseList_Failure_NotPerMission_NotSame() {
    // given
    String token = "token";
    Member member3 = new Member();
    member3.setId(3L);

    Transaction transaction = Transaction.builder()
        .transactionId(1L)
        .member(member)
        .roaster(roaster)
        .paymentStatus(EXCHANGE_REQUEST)
        .items(
            List.of(
                Item.builder()
                    .beanId(cart.getBean().getId())
                    .quantity(cart.getQuantity())
                    .price(cart.getPriceAtAdded())
                    .build(),
                Item.builder()
                    .beanId(cart2.getBean().getId())
                    .quantity(cart2.getQuantity())
                    .price(cart2.getPriceAtAdded())
                    .build()
            )).build();

    when(jwtProvider.getMemberFromEmail(token)).thenReturn(member3);
    // when
    CustomException e = assertThrows(CustomException.class,
        () -> transactionService.updateSellerPurchase(
            transaction.getTransactionId(), EXCHANGED, token));
    // then
    verify(transactionRepository, never()).save(any());
    assertEquals(NOT_PERMISSION, e.getErrorCode());
  }

  @Test
  void updateSellerPurchaseList_Failure_InvalidStatus() {
    // given
    String token = "token";

    Transaction transaction = Transaction.builder()
        .transactionId(1L)
        .member(member)
        .roaster(roaster)
        .paymentStatus(EXCHANGE_REQUEST)
        .items(
            List.of(
                Item.builder()
                    .beanId(cart.getBean().getId())
                    .quantity(cart.getQuantity())
                    .price(cart.getPriceAtAdded())
                    .build(),
                Item.builder()
                    .beanId(cart2.getBean().getId())
                    .quantity(cart2.getQuantity())
                    .price(cart2.getPriceAtAdded())
                    .build()
            )).build();

    when(jwtProvider.getMemberFromEmail(token)).thenReturn(member2);
    when(transactionRepository.findById(transaction.getTransactionId())).thenReturn(
        Optional.of(transaction));
    // when
    CustomException e = assertThrows(CustomException.class,
        () -> transactionService.updateSellerPurchase(transaction.getTransactionId(), DELIVERED,
            token));
    // then
    verify(transactionRepository, never()).save(any());
    assertEquals(INVALID_STATUS, e.getErrorCode());
  }
}