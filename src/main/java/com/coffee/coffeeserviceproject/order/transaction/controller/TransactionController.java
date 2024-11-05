package com.coffee.coffeeserviceproject.order.transaction.controller;

import com.coffee.coffeeserviceproject.common.model.ListResponseDto;
import com.coffee.coffeeserviceproject.order.transaction.dto.TransactionBuyerListDto;
import com.coffee.coffeeserviceproject.order.transaction.dto.TransactionDto;
import com.coffee.coffeeserviceproject.order.transaction.dto.TransactionSellerListDto;
import com.coffee.coffeeserviceproject.order.transaction.service.TransactionService;
import com.coffee.coffeeserviceproject.order.transaction.type.PaymentStatusType;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/order")
public class TransactionController {

  private final TransactionService transactionService;

  @PostMapping
  public ResponseEntity<Void> createOrder(@RequestBody TransactionDto transactionDto,
      @RequestHeader("AUTH-TOKEN") String token) {

    transactionService.createOrder(transactionDto, token);

    return ResponseEntity.noContent().build();
  }

  @PostMapping("/verification/{imp_uid}")
  public IamportResponse<Payment> validatePayment(@PathVariable String imp_uid) {

    return transactionService.validateOrder(imp_uid);
  }

  @PostMapping("/cancel/{imp_uid}")
  public IamportResponse<Payment> cancelOrder(@PathVariable String imp_uid) {

    return transactionService.cancelOrder(imp_uid);
  }

  @GetMapping("/buyer/purchase")
  public ResponseEntity<ListResponseDto<List<TransactionBuyerListDto>>> getBuyerPurchaseList(
      Pageable pageable,
      @RequestHeader("AUTH-TOKEN") String token) {

    Page<TransactionBuyerListDto> buyerListDtoPage = transactionService.getBuyerPurchaseList(
        pageable, token);

    ListResponseDto<List<TransactionBuyerListDto>> responseDto = new ListResponseDto<>(
        buyerListDtoPage.getContent(),
        buyerListDtoPage.getTotalElements(),
        buyerListDtoPage.getTotalPages()
    );

    return ResponseEntity.ok(responseDto);
  }

  @PatchMapping("/buyer/purchase/{id}")
  public ResponseEntity<Void> updateBuyerPurchaseList(@PathVariable Long id,
      @RequestParam PaymentStatusType paymentStatus,
      @RequestHeader("AUTH-TOKEN") String token) {

    transactionService.updateBuyerPurchase(id, paymentStatus, token);

    return ResponseEntity.noContent().build();
  }

  @GetMapping("/seller/purchase")
  public ResponseEntity<ListResponseDto<List<TransactionSellerListDto>>> getSellerPurchaseList(
      Pageable pageable,
      @RequestHeader("AUTH-TOKEN") String token) {

    Page<TransactionSellerListDto> sellerListDtoPage = transactionService.getSellerPurchaseList(
        pageable, token);

    ListResponseDto<List<TransactionSellerListDto>> responseDto = new ListResponseDto<>(
        sellerListDtoPage.getContent(),
        sellerListDtoPage.getTotalElements(),
        sellerListDtoPage.getTotalPages()
    );

    return ResponseEntity.ok(responseDto);
  }

  @PatchMapping("/seller/purchase/{id}")
  public ResponseEntity<Void> updateSellerPurchaseList(@PathVariable Long id,
      @RequestParam PaymentStatusType paymentStatus,
      @RequestHeader("AUTH-TOKEN") String token) {

    transactionService.updateSellerPurchase(id, paymentStatus, token);

    return ResponseEntity.noContent().build();
  }
}
