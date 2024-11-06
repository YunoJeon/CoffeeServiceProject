package com.coffee.coffeeserviceproject.favorite.controller;

import com.coffee.coffeeserviceproject.common.model.ListResponseDto;
import com.coffee.coffeeserviceproject.favorite.dto.FavoriteDto;
import com.coffee.coffeeserviceproject.favorite.service.FavoriteService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/favorites")
public class FavoriteController {

  private final FavoriteService favoriteService;

  @PostMapping("/beans/{beanId}")
  public ResponseEntity<Void> addFavorite(@PathVariable Long beanId,
      @RequestHeader("AUTH-TOKEN") String token) {

    favoriteService.addFavorite(beanId, token);

    return ResponseEntity.noContent().build();
  }

  @GetMapping("/members/me")
  public ResponseEntity<ListResponseDto<List<FavoriteDto>>> getFavoriteList(
      @RequestHeader("AUTH-TOKEN") String token,
      Pageable pageable) {

    Page<FavoriteDto> favoritePage = favoriteService.getFavoriteList(token, pageable);

    ListResponseDto<List<FavoriteDto>> responseDto = new ListResponseDto<>(
        favoritePage.getContent(),
        favoritePage.getTotalElements(),
        favoritePage.getTotalPages());

    return ResponseEntity.ok(responseDto);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteFavorite(@PathVariable Long id,
      @RequestHeader("AUTH-TOKEN") String token) {

    favoriteService.deleteFavorite(id, token);

    return ResponseEntity.noContent().build();
  }
}
