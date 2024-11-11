package com.coffee.coffeeserviceproject.favorite.controller;

import com.coffee.coffeeserviceproject.common.model.ListResponseDto;
import com.coffee.coffeeserviceproject.favorite.dto.FavoriteDto;
import com.coffee.coffeeserviceproject.favorite.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Favorites API", description = "즐겨찾기 관련 API")
public class FavoriteController {

  private final FavoriteService favoriteService;

  @PostMapping("/beans/{beanId}")
  @Operation(summary = "즐겨찾기 추가", description = "이미 등록된 즐겨찾기는 예외를 발생시킵니다.")
  public ResponseEntity<Void> addFavorite(@PathVariable Long beanId,
      @RequestHeader("AUTH-TOKEN") String token) {

    favoriteService.addFavorite(beanId, token);

    return ResponseEntity.noContent().build();
  }

  @GetMapping("/members/me")
  @Operation(summary = "즐겨찾기 목록 조회", description = "페이지 형식이며, 본인이 등록한 즐겨찾기를 볼 수 있습니다.")
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
  @Operation(summary = "즐겨찾기 삭제", description = "본인이 등록한 즐겨찾기만 삭제가 가능합니다.")
  public ResponseEntity<Void> deleteFavorite(@PathVariable Long id,
      @RequestHeader("AUTH-TOKEN") String token) {

    favoriteService.deleteFavorite(id, token);

    return ResponseEntity.noContent().build();
  }
}
