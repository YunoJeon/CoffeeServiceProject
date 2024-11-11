package com.coffee.coffeeserviceproject.order.transaction.controller;

import com.coffee.coffeeserviceproject.common.model.ListResponseDto;
import com.coffee.coffeeserviceproject.order.transaction.dto.TransactionBuyerListDto;
import com.coffee.coffeeserviceproject.order.transaction.dto.TransactionDto;
import com.coffee.coffeeserviceproject.order.transaction.dto.TransactionSellerListDto;
import com.coffee.coffeeserviceproject.order.transaction.service.TransactionService;
import com.coffee.coffeeserviceproject.order.transaction.type.PaymentStatusType;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Order API", description = "결제, 주문 관련 API")
public class TransactionController {

  private final TransactionService transactionService;

  @PostMapping
  @Operation(summary = "주문서 생성", description = "장바구니에 담은 제품들중 같은 로스터의 원두들로만 구매 시 주문서가 생성이 됩니다. 클라이언트에서 넘겨준 Port One 과 매핑할 Uid 를 주문서 생성 시 등록됩니다.")
  public ResponseEntity<Void> createOrder(@RequestBody TransactionDto transactionDto,
      @RequestHeader("AUTH-TOKEN") String token) {

    transactionService.createOrder(transactionDto, token);

    return ResponseEntity.noContent().build();
  }

  @PostMapping("/verification/{impUid}")
  @Operation(summary = "주문서 비교", description = "Port One 결제대행 서비스에서 생성한 impUid 와 결제 API impUid 를 확인해 주문서와 매핑이 되는지 확인 후 결제 완료처리를 합니다. 주문서의 상태가 올바르지 않거나, 결제 금액이 다르면 결제 진행이 되지 않습니다.")
  public IamportResponse<Payment> validatePayment(@PathVariable String impUid) {

    return transactionService.validateOrder(impUid);
  }

  @PostMapping("/cancel/{impUid}")
  @Operation(summary = "결제 취소", description = "Port One 결제대행 서비스에서 생성한 impUid 와 결제취소 API impUid 를 확인해 주문서와 매핑이 되는지 확인 후 결제 취소처리를 합니다. 주문서의 상태가 올바르지 않으면 결제 취소 진행이 되지 않습니다.")
  public IamportResponse<Payment> cancelOrder(@PathVariable String impUid) {

    return transactionService.cancelOrder(impUid);
  }

  @GetMapping("/buyer/purchase")
  @Operation(summary = "구매 목록 조회", description = "본인이 구매한 구매 목록을 조회할 수 있고, 페이지 형식으로 반환됩니다.")
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
  @Operation(summary = "배송 상태 변경 - 구매자용", description = "본인이 구매한 주문서만 수정이 가능하며, 단계별로 진행이 됩니다.(결제완료, 주문확인 상태 -> 취소요청, 배송완료 상태 -> 교환요청, 미수령, 구매확정)")
  public ResponseEntity<Void> updateBuyerPurchaseList(@PathVariable Long id,
      @RequestParam PaymentStatusType paymentStatus,
      @RequestHeader("AUTH-TOKEN") String token) {

    transactionService.updateBuyerPurchase(id, paymentStatus, token);

    return ResponseEntity.noContent().build();
  }

  @GetMapping("/seller/purchase")
  @Operation(summary = "판매 목록 조회 - 판매자용", description = "본인이 판매한 판매 목록을 조회할 수 있고, 페이지 형식으로 반환됩니다.")
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
  @Operation(summary = "배송 상태 변경 - 판매자용", description = "본인이 판매한 주문서만 수정이 가능하며, 단계별로 진행이 됩니다.(결제완료 -> 주문확인, 주문확인 -> 배송준비중, 배송준비중 -> 배송중 ...)")
  public ResponseEntity<Void> updateSellerPurchaseList(@PathVariable Long id,
      @RequestParam PaymentStatusType paymentStatus,
      @RequestHeader("AUTH-TOKEN") String token) {

    transactionService.updateSellerPurchase(id, paymentStatus, token);

    return ResponseEntity.noContent().build();
  }
}
