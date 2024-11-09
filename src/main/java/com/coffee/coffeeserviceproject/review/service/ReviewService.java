package com.coffee.coffeeserviceproject.review.service;

import static com.coffee.coffeeserviceproject.common.type.ErrorCode.NOT_FOUND_BEAN;
import static com.coffee.coffeeserviceproject.common.type.ErrorCode.NOT_FOUND_REVIEW;
import static com.coffee.coffeeserviceproject.common.type.ErrorCode.NOT_PERMISSION;
import static com.coffee.coffeeserviceproject.common.type.ErrorCode.SCORE_MULTIPLE_05;

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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

  private final ReviewRepository reviewRepository;

  private final JwtProvider jwtProvider;

  private final BeanRepository beanRepository;

  private final SearchRepository searchRepository;

  @Transactional
  public void addReview(Long beanId, ReviewRequestDto reviewRequestDto, String token) {

    if (!reviewRequestDto.isValidScore()) {
      throw new CustomException(SCORE_MULTIPLE_05);
    }

    Member member = jwtProvider.getMemberFromEmail(token);

    Bean bean = beanRepository.findById(beanId)
        .orElseThrow(() -> new CustomException(NOT_FOUND_BEAN));

    Review review = Review.fromDto(member, bean, reviewRequestDto);

    reviewRepository.save(review);
    updateAverageScore(beanId);
  }

  @Transactional(readOnly = true)
  public Page<ReviewResponseDto> getMyReviewList(Pageable pageable, String token) {

    Member member = jwtProvider.getMemberFromEmail(token);

    Long memberId = member.getId();

    Page<Review> myReviewPage = reviewRepository.findAllByMemberId(memberId, pageable);

    return myReviewPage.map(ReviewResponseDto::myReviewList);
  }

  @Transactional(readOnly = true)
  public Page<ReviewResponseDto> getBeanReviewList(Pageable pageable, Long beanId) {

    Page<Review> getReviewPage = reviewRepository.findAllByBeanId(beanId, pageable);

    return getReviewPage.map(ReviewResponseDto::getBeanReviewList);
  }

  @Transactional
  public void updateReview(Long id, String token, String comment) {

    Member member = jwtProvider.getMemberFromEmail(token);

    Review review = reviewRepository.findById(id)
        .orElseThrow(() -> new CustomException(NOT_FOUND_REVIEW));

    if (!member.getId().equals(review.getMember().getId())) {

      throw new CustomException(NOT_PERMISSION);
    }

    review.setComment(comment);

    reviewRepository.save(review);
  }

  @Transactional
  public void deleteReview(Long id, String token) {

    Member member = jwtProvider.getMemberFromEmail(token);

    Review review = reviewRepository.findById(id)
        .orElseThrow(() -> new CustomException(NOT_FOUND_REVIEW));

    Long beanId = review.getBean().getId();

    if (!member.getId().equals(review.getMember().getId())) {

      throw new CustomException(NOT_PERMISSION);
    }

    reviewRepository.delete(review);
    updateAverageScore(beanId);
  }

  @Async
  public void updateAverageScore(Long beanId) {

    List<Review> reviewList = reviewRepository.findAllByBeanId(beanId);
    if (!reviewList.isEmpty()) {
      double averageScore = reviewList.stream()
          .mapToDouble(Review::getScore)
          .average()
          .orElse(0.0);

      Bean bean = beanRepository.findById(beanId)
          .orElseThrow(() -> new CustomException(NOT_FOUND_BEAN));

      bean.setAverageScore(averageScore);
      beanRepository.save(bean);

      SearchBeanList searchBeanList = searchRepository.findById(beanId)
          .orElseThrow(() -> new CustomException(NOT_FOUND_BEAN));

      searchBeanList.setAverageScore(averageScore);
      searchRepository.save(searchBeanList);
    }
  }
}
