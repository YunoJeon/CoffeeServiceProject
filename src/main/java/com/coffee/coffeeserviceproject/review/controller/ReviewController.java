package com.coffee.coffeeserviceproject.review.controller;

import com.coffee.coffeeserviceproject.common.model.ListResponseDto;
import com.coffee.coffeeserviceproject.review.dto.ReviewRequestDto;
import com.coffee.coffeeserviceproject.review.dto.ReviewResponseDto;
import com.coffee.coffeeserviceproject.review.service.ReviewService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
@RequestMapping("/reviews")
public class ReviewController {

  private final ReviewService reviewService;

  @PostMapping("/{beanId}")
  public ResponseEntity<Void> addReview(@PathVariable Long beanId,
      @RequestBody @Valid ReviewRequestDto reviewRequestDto,
      @RequestHeader("AUTH-TOKEN") String token) {

    reviewService.addReview(beanId, reviewRequestDto, token);

    return ResponseEntity.noContent().build();
  }

  @GetMapping("/members/me")
  public ResponseEntity<ListResponseDto<List<ReviewResponseDto>>> getMyReviewList(Pageable pageable,
      @RequestHeader("AUTH-TOKEN") String token) {

    Page<ReviewResponseDto> reviewResponseDtoPage = reviewService.getMyReviewList(pageable, token);

    ListResponseDto<List<ReviewResponseDto>> responseDto = new ListResponseDto<>(
        reviewResponseDtoPage.getContent(),
        reviewResponseDtoPage.getTotalElements(),
        reviewResponseDtoPage.getTotalPages());

    return ResponseEntity.ok(responseDto);
  }

  @GetMapping("/beans/{beanId}")
  public ResponseEntity<ListResponseDto<List<ReviewResponseDto>>> getBeanReviewList(
      Pageable pageable,
      @PathVariable("beanId") Long beanId) {

    Page<ReviewResponseDto> reviewResponseDtoPage = reviewService.getBeanReviewList(pageable,
        beanId);

    ListResponseDto<List<ReviewResponseDto>> responseDto = new ListResponseDto<>(
        reviewResponseDtoPage.getContent(),
        reviewResponseDtoPage.getTotalElements(),
        reviewResponseDtoPage.getTotalPages());

    return ResponseEntity.ok(responseDto);
  }

  @PatchMapping("/{id}")
  public ResponseEntity<Void> updateReview(@PathVariable("id") Long id,
      @RequestHeader("AUTH-TOKEN") String token,
      @RequestBody @Valid @NotBlank(message = "리뷰를 작성해 주세요.")
      @Size(min = 10, message = "최소 10글자 이상 입력해 주세요.") String comment) {

    reviewService.updateReview(id, token, comment);

    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteReview(@PathVariable("id") Long id,
      @RequestHeader("AUTH-TOKEN") String token) {

    reviewService.deleteReview(id, token);

    return ResponseEntity.noContent().build();
  }
}
