package com.coffee.coffeeserviceproject.review.service;

import static com.coffee.coffeeserviceproject.common.type.ErrorCode.NOT_FOUND_REVIEW;
import static com.coffee.coffeeserviceproject.common.type.ErrorCode.NOT_PERMISSION;
import static com.coffee.coffeeserviceproject.common.type.ErrorCode.SCORE_MULTIPLE_05;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import com.coffee.coffeeserviceproject.elasticsearch.document.SearchBeanList;
import com.coffee.coffeeserviceproject.elasticsearch.repository.SearchRepository;
import com.coffee.coffeeserviceproject.member.entity.Member;
import com.coffee.coffeeserviceproject.review.dto.ReviewRequestDto;
import com.coffee.coffeeserviceproject.review.dto.ReviewResponseDto;
import com.coffee.coffeeserviceproject.review.entity.Review;
import com.coffee.coffeeserviceproject.review.repository.ReviewRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

  @Mock
  private ReviewRepository reviewRepository;

  @Mock
  private JwtProvider jwtProvider;

  @Mock
  private BeanRepository beanRepository;

  @Mock
  private SearchRepository searchRepository;

  @InjectMocks
  private ReviewService reviewService;

  private Member member;
  private Bean bean;
  private ReviewRequestDto reviewRequestDto;

  private String token;

  @BeforeEach
  void setUp() {

    token = "token";

    member = new Member();
    member.setId(1L);

    bean = new Bean();
    bean.setId(1L);
    bean.setAverageScore(0.0);

    reviewRequestDto = new ReviewRequestDto();
    reviewRequestDto.setScore(5.0);
    reviewRequestDto.setComment("굿");
  }

  @Test
  void addReview_Success() {
    // given
    when(jwtProvider.getMemberFromEmail(token)).thenReturn(member);
    when(beanRepository.findById(bean.getId())).thenReturn(Optional.of(bean));

    // when
    reviewService.addReview(bean.getId(), reviewRequestDto, token);
    // then
    ArgumentCaptor<Review> captor = ArgumentCaptor.forClass(Review.class);
    verify(reviewRepository).save(captor.capture());

    Review review = captor.getValue();
    assertEquals("굿", review.getComment());
    assertEquals(5.0, review.getScore());
  }

  @Test
  void addReview_Success_AverageScore() {
    // given
    Review review = Review.builder()
        .score(5.0)
        .bean(bean)
        .build();

    Review newReview = new Review();
    newReview.setBean(bean);
    newReview.setScore(3.0);

    when(reviewRepository.findAllByBeanId(bean.getId())).thenReturn(List.of(review, newReview));
    when(beanRepository.findById(bean.getId())).thenReturn(Optional.of(bean));
    when(searchRepository.findById(bean.getId())).thenReturn(
        Optional.of(SearchBeanList.builder().build()));
    // when
    reviewService.updateAverageScore(bean.getId());

    // then
    ArgumentCaptor<Bean> captor = ArgumentCaptor.forClass(Bean.class);

    verify(beanRepository).save(captor.capture());
    assertEquals(4.0, captor.getValue().getAverageScore());

    ArgumentCaptor<SearchBeanList> searchCaptor = ArgumentCaptor.forClass(SearchBeanList.class);

    verify(searchRepository).save(searchCaptor.capture());
    assertEquals(4.0, searchCaptor.getValue().getAverageScore());
  }

  @Test
  void addReview_Failure_SCORE_MULTIPLE_05() {
    // given
    reviewRequestDto.setScore(4.3);
    // when
    CustomException e = assertThrows(CustomException.class,
        () -> reviewService.addReview(bean.getId(), reviewRequestDto, token));
    // then
    verify(reviewRepository, never()).save(any());
    assertEquals(SCORE_MULTIPLE_05, e.getErrorCode());
  }

  @Test
  void getMyReview_Success() {
    // given
    Pageable pageable = Pageable.ofSize(10);

    Bean bean2 = new Bean();
    bean2.setId(2L);

    Bean bean3 = new Bean();
    bean3.setId(3L);

    Review review1 = Review.builder()
        .score(5.0)
        .member(member)
        .bean(bean)
        .build();

    Review review2 = Review.builder()
        .score(5.0)
        .member(member)
        .bean(bean2)
        .build();

    Review review3 = Review.builder()
        .score(5.0)
        .member(member)
        .bean(bean3)
        .build();

    Page<Review> getReviewPage = new PageImpl<>(List.of(review1, review2, review3));

    when(jwtProvider.getMemberFromEmail(token)).thenReturn(member);
    when(reviewRepository.findAllByMemberId(member.getId(), pageable)).thenReturn(getReviewPage);
    // when
    Page<ReviewResponseDto> myReviews = reviewService.getMyReviewList(pageable, token);
    // then
    assertNotNull(myReviews);
    assertEquals(3, myReviews.getTotalElements());
  }

  @Test
  void getMyReview_Success_EmptyList() {
    // given
    Pageable pageable = Pageable.ofSize(10);

    Page<Review> getReviewPage = new PageImpl<>(List.of());

    when(jwtProvider.getMemberFromEmail(token)).thenReturn(member);
    when(reviewRepository.findAllByMemberId(member.getId(), pageable)).thenReturn(getReviewPage);
    // when
    Page<ReviewResponseDto> myReviews = reviewService.getMyReviewList(pageable, token);
    // then
    assertNotNull(myReviews);
    assertTrue(myReviews.getContent().isEmpty());
  }

  @Test
  void getBeanReviewList_Success() {
    // given
    Pageable pageable = Pageable.ofSize(10);

    Member member2 = new Member();
    member2.setId(2L);

    Review review1 = Review.builder()
        .score(5.0)
        .member(member)
        .bean(bean)
        .build();

    Review review2 = Review.builder()
        .score(5.0)
        .member(member)
        .bean(bean)
        .build();

    Review review3 = Review.builder()
        .score(5.0)
        .member(member2)
        .bean(bean)
        .build();

    Page<Review> getReviewPage = new PageImpl<>(List.of(review1, review2, review3));
    when(reviewRepository.findAllByBeanId(bean.getId(), pageable)).thenReturn(getReviewPage);
    // when
    Page<ReviewResponseDto> beanReviews = reviewService.getBeanReviewList(pageable, bean.getId());
    // then
    assertNotNull(beanReviews);
    assertEquals(3, beanReviews.getTotalElements());
  }

  @Test
  void getBeanReviewList_Success_EmptyList() {
    // given
    Pageable pageable = Pageable.ofSize(10);

    Page<Review> getReviewPage = new PageImpl<>(List.of());
    when(reviewRepository.findAllByBeanId(bean.getId(), pageable)).thenReturn(getReviewPage);
    // when
    Page<ReviewResponseDto> beanReviews = reviewService.getBeanReviewList(pageable, bean.getId());
    // then
    assertNotNull(beanReviews);
    assertTrue(beanReviews.getContent().isEmpty());
  }

  @Test
  void updateReview_Success() {
    // given
    Review review = Review.builder()
        .id(1L)
        .score(5.0)
        .member(member)
        .bean(bean)
        .comment("굿")
        .build();

    when(jwtProvider.getMemberFromEmail(token)).thenReturn(member);
    when(reviewRepository.findById(review.getId())).thenReturn(Optional.of(review));
    // when
    reviewService.updateReview(review.getId(), token, "베리 굿");
    // then
    verify(reviewRepository).save(any(Review.class));
    assertEquals("베리 굿", review.getComment());
  }

  @Test
  void updateReview_Failure_NotFoundReview() {
    // given
    when(jwtProvider.getMemberFromEmail(token)).thenReturn(member);
    when(reviewRepository.findById(any())).thenReturn(Optional.empty());
    // when
    CustomException e = assertThrows(CustomException.class,
        () -> reviewService.updateReview(any(), token, "베리 굿"));
    // then
    verify(reviewRepository, never()).save(any(Review.class));
    assertEquals(NOT_FOUND_REVIEW, e.getErrorCode());
  }

  @Test
  void updateReview_Failure_NotPermission() {
    // given
    Member member2 = new Member();
    member2.setId(2L);

    Review review = Review.builder()
        .id(1L)
        .score(5.0)
        .member(member)
        .bean(bean)
        .comment("굿")
        .build();

    when(jwtProvider.getMemberFromEmail(token)).thenReturn(member2);
    when(reviewRepository.findById(review.getId())).thenReturn(Optional.of(review));
    // when
    CustomException e = assertThrows(CustomException.class,
        () -> reviewService.updateReview(review.getId(), token, "베리 굿"));
    // then
    verify(reviewRepository, never()).save(any(Review.class));
    assertEquals(NOT_PERMISSION, e.getErrorCode());
  }

  @Test
  void deleteReview_Success() {
    // given
    Review review = Review.builder()
        .id(1L)
        .score(5.0)
        .member(member)
        .bean(bean)
        .build();

    when(jwtProvider.getMemberFromEmail(token)).thenReturn(member);
    when(reviewRepository.findById(review.getId())).thenReturn(Optional.of(review));
    // when
    reviewService.deleteReview(review.getId(), token);
    // then
    verify(reviewRepository).delete(any(Review.class));
  }

  @Test
  void deleteReview_Success_AverageScore() {
    // given
    Member member2 = new Member();
    member2.setId(2L);

    Review review = Review.builder()
        .id(1L)
        .score(5.0)
        .member(member)
        .bean(bean)
        .build();

    Review review2 = Review.builder()
        .id(2L)
        .score(3.0)
        .member(member2)
        .bean(bean)
        .build();

    when(reviewRepository.findAllByBeanId(bean.getId())).thenReturn(List.of(review, review2));
    when(beanRepository.findById(bean.getId())).thenReturn(Optional.of(bean));
    when(searchRepository.findById(bean.getId())).thenReturn(
        Optional.of(SearchBeanList.builder().build()));
    // when
    when(reviewRepository.findAllByBeanId(bean.getId())).thenReturn(List.of(review2));
    reviewService.updateAverageScore(bean.getId());

    // then
    ArgumentCaptor<Bean> captor = ArgumentCaptor.forClass(Bean.class);

    verify(beanRepository).save(captor.capture());
    assertEquals(3.0, captor.getValue().getAverageScore());

    ArgumentCaptor<SearchBeanList> searchCaptor = ArgumentCaptor.forClass(SearchBeanList.class);

    verify(searchRepository).save(searchCaptor.capture());
    assertEquals(3.0, searchCaptor.getValue().getAverageScore());
  }

  @Test
  void deleteReview_Failure_NotFoundReview() {
    // given
    when(jwtProvider.getMemberFromEmail(token)).thenReturn(member);
    when(reviewRepository.findById(1L)).thenReturn(Optional.empty());
    // when
    CustomException e = assertThrows(CustomException.class,
        () -> reviewService.deleteReview(1L, token));
    // then
    verify(reviewRepository, never()).delete(any(Review.class));
    assertEquals(NOT_FOUND_REVIEW, e.getErrorCode());
  }

  @Test
  void deleteReview_Failure_NotPermission() {
    // given
    Member member2 = new Member();
    member2.setId(2L);

    Review review = Review.builder()
        .id(1L)
        .score(5.0)
        .member(member)
        .bean(bean)
        .build();

    when(jwtProvider.getMemberFromEmail(token)).thenReturn(member2);
    when(reviewRepository.findById(review.getId())).thenReturn(Optional.of(review));
    // when
    CustomException e = assertThrows(CustomException.class,
        () -> reviewService.deleteReview(review.getId(), token));
    // then
    verify(reviewRepository, never()).delete(any(Review.class));
    assertEquals(NOT_PERMISSION, e.getErrorCode());
  }
}