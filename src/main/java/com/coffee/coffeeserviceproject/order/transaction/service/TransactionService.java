package com.coffee.coffeeserviceproject.order.transaction.service;

import static com.coffee.coffeeserviceproject.bean.type.PurchaseStatus.IMPOSSIBLE;
import static com.coffee.coffeeserviceproject.common.type.ErrorCode.FAILURE_CANCEL;
import static com.coffee.coffeeserviceproject.common.type.ErrorCode.INVALID_AMOUNT;
import static com.coffee.coffeeserviceproject.common.type.ErrorCode.INVALID_STATUS;
import static com.coffee.coffeeserviceproject.common.type.ErrorCode.NOT_AVAILABLE_PURCHASE;
import static com.coffee.coffeeserviceproject.common.type.ErrorCode.NOT_FOUND_BEAN;
import static com.coffee.coffeeserviceproject.common.type.ErrorCode.NOT_FOUND_ORDER;
import static com.coffee.coffeeserviceproject.common.type.ErrorCode.NOT_FOUND_ROASTER;
import static com.coffee.coffeeserviceproject.common.type.ErrorCode.NOT_PERMISSION;
import static com.coffee.coffeeserviceproject.common.type.ErrorCode.PAYMENT_ERROR;
import static com.coffee.coffeeserviceproject.common.type.ErrorCode.PRICE_CHANGE;
import static com.coffee.coffeeserviceproject.common.type.ErrorCode.SAME_ROASTER;
import static com.coffee.coffeeserviceproject.member.type.RoleType.SELLER;
import static com.coffee.coffeeserviceproject.order.transaction.type.PaymentStatusType.CANCELED;
import static com.coffee.coffeeserviceproject.order.transaction.type.PaymentStatusType.CANCEL_REQUEST;
import static com.coffee.coffeeserviceproject.order.transaction.type.PaymentStatusType.DELIVERED;
import static com.coffee.coffeeserviceproject.order.transaction.type.PaymentStatusType.DELIVERY;
import static com.coffee.coffeeserviceproject.order.transaction.type.PaymentStatusType.EXCHANGED;
import static com.coffee.coffeeserviceproject.order.transaction.type.PaymentStatusType.EXCHANGE_REQUEST;
import static com.coffee.coffeeserviceproject.order.transaction.type.PaymentStatusType.ORDER_CONFIRMED;
import static com.coffee.coffeeserviceproject.order.transaction.type.PaymentStatusType.PAID;
import static com.coffee.coffeeserviceproject.order.transaction.type.PaymentStatusType.PREPARE;
import static com.coffee.coffeeserviceproject.order.transaction.type.PaymentStatusType.READY;
import static java.math.BigDecimal.ZERO;

import com.coffee.coffeeserviceproject.bean.entity.Bean;
import com.coffee.coffeeserviceproject.bean.repository.BeanRepository;
import com.coffee.coffeeserviceproject.common.exception.CustomException;
import com.coffee.coffeeserviceproject.configuration.JwtProvider;
import com.coffee.coffeeserviceproject.member.entity.Member;
import com.coffee.coffeeserviceproject.member.entity.Roaster;
import com.coffee.coffeeserviceproject.member.repository.RoasterRepository;
import com.coffee.coffeeserviceproject.order.cart.repository.CartRepository;
import com.coffee.coffeeserviceproject.order.transaction.dto.TransactionBuyerListDto;
import com.coffee.coffeeserviceproject.order.transaction.dto.TransactionDto;
import com.coffee.coffeeserviceproject.order.transaction.dto.TransactionSellerListDto;
import com.coffee.coffeeserviceproject.order.transaction.entity.Item;
import com.coffee.coffeeserviceproject.order.transaction.entity.Transaction;
import com.coffee.coffeeserviceproject.order.transaction.repository.TransactionRepository;
import com.coffee.coffeeserviceproject.order.transaction.type.PaymentStatusType;
import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.request.CancelData;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransactionService {

  private final TransactionRepository transactionRepository;

  private final JwtProvider jwtProvider;

  private final BeanRepository beanRepository;

  private final IamportClient iamportClient;

  private final CartRepository cartRepository;

  private final RoasterRepository roasterRepository;

  @Transactional
  public void createOrder(TransactionDto transactionDto, String token) {

    Member member = getMemberFromToken(token);

    List<Item> items = new ArrayList<>();

    Long firstBeanId = transactionDto.getItems().get(0).getBeanId();

    Long roasterId = findByBeanIdFromBeanRepository(firstBeanId).getRoasterId();

    Roaster roaster = findByRoasterIdFromRoasterRepository(roasterId);

    addItemsFromCart(transactionDto, items, roasterId);

    Transaction transaction = Transaction.fromDto(transactionDto, member, roaster, items);

    transactionRepository.save(transaction);
  }

  private void addItemsFromCart(TransactionDto transactionDto, List<Item> items, Long roasterId) {

    for (Item item : transactionDto.getItems()) {

      Bean bean = findByBeanIdFromBeanRepository(item.getBeanId());

      if (bean.getPurchaseStatus() == IMPOSSIBLE) {
        throw new CustomException(NOT_AVAILABLE_PURCHASE, bean.getBeanName());
      }

      if (!bean.getPrice().equals(item.getPrice())) {
        throw new CustomException(PRICE_CHANGE, bean.getBeanName());
      }

      Long currentRoasterId = bean.getRoasterId();

      if (!roasterId.equals(currentRoasterId)) {
        throw new CustomException(SAME_ROASTER);
      }

      items.add(Item.of(item));
    }
  }

  @Transactional
  public IamportResponse<Payment> validateOrder(String impUid) {

    try {

      IamportResponse<Payment> response = iamportClient.paymentByImpUid(impUid);
      Payment payment = response.getResponse();

      Transaction transaction = findByMerchantUidFromTransactionRepository(payment.getMerchantUid());

      if (!payment.getStatus().equals("paid") || !transaction.getPaymentStatus().equals(READY)) {

        throw new CustomException(INVALID_STATUS);
      }

      BigDecimal totalAmount = transaction.getItems().stream().map(
              item -> BigDecimal.valueOf(item.getPrice())
                  .multiply(BigDecimal.valueOf(item.getQuantity())))
          .reduce(ZERO, BigDecimal::add);

      if (payment.getAmount().compareTo(totalAmount) != 0) {

        throw new CustomException(INVALID_AMOUNT);
      }

      transaction.setTotalPrice(totalAmount.longValue());
      transaction.setPaymentStatus(PAID);
      transactionRepository.save(transaction);

      deleteItemsFromCart(transaction);

      return response;

    } catch (IamportResponseException | IOException e) {

      throw new CustomException(PAYMENT_ERROR);
    }
  }

  private void deleteItemsFromCart(Transaction transaction) {

    for (Item item : transaction.getItems()) {

      cartRepository.deleteByBeanIdAndMemberId(item.getBeanId(),
          transaction.getMember().getId());
    }
  }

  @Transactional
  public IamportResponse<Payment> cancelOrder(String impUid) {

    try {

      IamportResponse<Payment> response = iamportClient.paymentByImpUid(impUid);
      Payment payment = response.getResponse();

      Transaction transaction = findByMerchantUidFromTransactionRepository(payment.getMerchantUid());

      if (!payment.getStatus().equals("paid") || !transaction.getPaymentStatus()
          .equals(CANCEL_REQUEST)) {

        throw new CustomException(INVALID_STATUS);
      }

      CancelData cancelData = new CancelData(impUid, true);

      IamportResponse<Payment> cancelResponse = iamportClient.cancelPaymentByImpUid(cancelData);

      if (cancelResponse.getResponse() == null || !cancelResponse.getResponse().getStatus()
          .equals("cancelled")) {

        throw new CustomException(FAILURE_CANCEL);
      }

      transaction.setPaymentStatus(CANCELED);
      transactionRepository.save(transaction);

      return cancelResponse;

    } catch (IamportResponseException | IOException e) {

      throw new CustomException(PAYMENT_ERROR);
    }
  }

  @Transactional(readOnly = true)
  public Page<TransactionBuyerListDto> getBuyerPurchaseList(Pageable pageable, String token) {

    Member member = getMemberFromToken(token);

    Long memberId = member.getId();

    Page<Transaction> transactionPage = transactionRepository.findByMemberId(memberId, pageable);

    return transactionPage.map(transaction -> {

      List<Item> items = new ArrayList<>(transaction.getItems());

      return TransactionBuyerListDto.fromEntity(transaction, items);
    });
  }

  public void updateBuyerPurchase(Long id, PaymentStatusType paymentStatus, String token) {

    Member member = getMemberFromToken(token);

    Transaction transaction = findByTransactionIdFromTransactionRepository(id);

    if (!member.getId().equals(transaction.getMember().getId())) {

      throw new CustomException(NOT_PERMISSION);
    }

    if (paymentStatus == CANCEL_REQUEST) {

      if (transaction.getPaymentStatus() == PAID
          || transaction.getPaymentStatus() == ORDER_CONFIRMED) {

        transaction.setPaymentStatus(CANCEL_REQUEST);
      }
    } else {

      switch (paymentStatus) {
        case EXCHANGE_REQUEST:
        case NOT_RECEIVED:
        case PURCHASE_CONFIRMED:

          if (transaction.getPaymentStatus() == DELIVERED) {
            transaction.setPaymentStatus(paymentStatus);
          } else {
            throw new CustomException(INVALID_STATUS);
          }
          break;

        default:
          throw new CustomException(INVALID_STATUS);
      }
    }

    transactionRepository.save(transaction);
  }

  @Transactional(readOnly = true)
  public Page<TransactionSellerListDto> getSellerPurchaseList(Pageable pageable, String token) {

    Member member = getMemberFromToken(token);

    if (member.getRole() != SELLER) {

      throw new CustomException(NOT_PERMISSION);
    }

    Long roasterId = member.getRoaster().getId();

    Page<Transaction> transactionPage = transactionRepository.findByRoasterId(roasterId, pageable);

    return transactionPage.map(transaction -> {

      List<Item> items = new ArrayList<>(transaction.getItems());

      return TransactionSellerListDto.fromEntity(transaction, items);
    });
  }

  public void updateSellerPurchase(Long id, PaymentStatusType paymentStatus, String token) {

    Member member = getMemberFromToken(token);

    if (member.getRole() != SELLER) {

      throw new CustomException(NOT_PERMISSION);
    }

    Transaction transaction = findByTransactionIdFromTransactionRepository(id);

    if (!member.getRoaster().getId().equals(transaction.getRoaster().getId())) {

      throw new CustomException(NOT_PERMISSION);
    }

    if (paymentStatus == ORDER_CONFIRMED && transaction.getPaymentStatus() == PAID) {

      transaction.setPaymentStatus(ORDER_CONFIRMED);

    } else if (paymentStatus == PREPARE && transaction.getPaymentStatus() == ORDER_CONFIRMED) {

      transaction.setPaymentStatus(PREPARE);

    } else if (paymentStatus == DELIVERY && transaction.getPaymentStatus() == PREPARE) {

      transaction.setPaymentStatus(DELIVERY);

    } else if (paymentStatus == DELIVERED && transaction.getPaymentStatus() == DELIVERY) {

      transaction.setPaymentStatus(DELIVERED);

    } else if (paymentStatus == EXCHANGED && transaction.getPaymentStatus() == EXCHANGE_REQUEST) {

      transaction.setPaymentStatus(EXCHANGED);

    } else {

      throw new CustomException(INVALID_STATUS);
    }

    transactionRepository.save(transaction);
  }

  private Member getMemberFromToken(String token) {

    return jwtProvider.getMemberFromEmail(token);
  }

  private Bean findByBeanIdFromBeanRepository(Long id) {

    return beanRepository.findById(id).orElseThrow(() -> new CustomException(NOT_FOUND_BEAN));
  }

  private Roaster findByRoasterIdFromRoasterRepository(Long id) {

    return roasterRepository.findById(id).orElseThrow(() -> new CustomException(NOT_FOUND_ROASTER));
  }

  private Transaction findByMerchantUidFromTransactionRepository(String id) {

    return transactionRepository.findByMerchantUid(id)
        .orElseThrow(() -> new CustomException(NOT_FOUND_ORDER));
  }

  private Transaction findByTransactionIdFromTransactionRepository(Long id) {

    return transactionRepository.findById(id).orElseThrow(() -> new CustomException(NOT_FOUND_ORDER));
  }
}
