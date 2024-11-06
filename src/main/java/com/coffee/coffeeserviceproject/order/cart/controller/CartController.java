package com.coffee.coffeeserviceproject.order.cart.controller;

import com.coffee.coffeeserviceproject.order.cart.dto.CartDto;
import com.coffee.coffeeserviceproject.order.cart.dto.CartListDto;
import com.coffee.coffeeserviceproject.order.cart.dto.CartUpdateDto;
import com.coffee.coffeeserviceproject.order.cart.service.CartService;
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
public class CartController {

  private final CartService cartService;

  @PostMapping("/{beanId}")
  public ResponseEntity<Void> addCart(@PathVariable("beanId") Long beanId,
      @RequestBody @Valid CartDto cartDto,
      @RequestHeader("AUTH-TOKEN") String token) {

    cartService.addCart(beanId, cartDto, token);

    return ResponseEntity.noContent().build();
  }

  @GetMapping
  public ResponseEntity<List<CartListDto>> getCart(@RequestHeader("AUTH-TOKEN") String token) {

    List<CartListDto> cartListDto = cartService.getCart(token);

    return ResponseEntity.ok(cartListDto);
  }

  @PatchMapping("/{id}")
  public ResponseEntity<Void> updateCart(@PathVariable("id") Long id,
      @RequestBody @Valid CartUpdateDto cartUpdateDto,
      @RequestHeader("AUTH-TOKEN") String token) {

    cartService.updateCart(id, cartUpdateDto, token);

    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteCart(@PathVariable("id") Long id,
      @RequestHeader("AUTH-TOKEN") String token) {

    cartService.deleteCart(id, token);

    return ResponseEntity.noContent().build();
  }
}
