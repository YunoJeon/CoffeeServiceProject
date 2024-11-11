package com.coffee.coffeeserviceproject.order.cart.controller;

import com.coffee.coffeeserviceproject.order.cart.dto.CartDto;
import com.coffee.coffeeserviceproject.order.cart.dto.CartListDto;
import com.coffee.coffeeserviceproject.order.cart.dto.CartUpdateDto;
import com.coffee.coffeeserviceproject.order.cart.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/carts")
@Tag(name = "Carts API", description = "장바구니 관련 API")
public class CartController {

  private final CartService cartService;

  @PostMapping("/{beanId}")
  @Operation(summary = "장바구니 추가", description = "구매 가능한 원두만 장바구니에 담을 수 있고, 장바구니에 같은 제품 추가 시 수량이 업데이트 됩니다.")
  public ResponseEntity<Void> addCart(@PathVariable("beanId") Long beanId,
      @RequestBody @Valid CartDto cartDto,
      @RequestHeader("AUTH-TOKEN") String token) {

    cartService.addCart(beanId, cartDto, token);

    return ResponseEntity.noContent().build();
  }

  @GetMapping
  @Operation(summary = "장바구니 조회", description = "본인의 장바구니만 조회 가능합니다.")
  public ResponseEntity<List<CartListDto>> getCart(@RequestHeader("AUTH-TOKEN") String token) {

    List<CartListDto> cartListDto = cartService.getCart(token);

    return ResponseEntity.ok(cartListDto);
  }

  @PatchMapping("/{id}")
  @Operation(summary = "장바구니 수정", description = "장바구니를 추가한 본인만 수정할 수 있고, 수정하지 않는 필드는 \"null\" 값으로 넘기면 해당 필드는 수정되지 않습니다.")
  public ResponseEntity<Void> updateCart(@PathVariable("id") Long id,
      @RequestBody @Valid CartUpdateDto cartUpdateDto,
      @RequestHeader("AUTH-TOKEN") String token) {

    cartService.updateCart(id, cartUpdateDto, token);

    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "장바구니 삭제", description = "본인의 장바구니만 삭제 가능합니다.")
  public ResponseEntity<Void> deleteCart(@PathVariable("id") Long id,
      @RequestHeader("AUTH-TOKEN") String token) {

    cartService.deleteCart(id, token);

    return ResponseEntity.noContent().build();
  }
}
