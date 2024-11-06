package com.coffee.coffeeserviceproject.review.repository;

import com.coffee.coffeeserviceproject.review.entity.Review;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {

  Page<Review> findAllByMemberId(Long memberId, Pageable pageable);

  Page<Review> findAllByBeanId(Long beanId, Pageable pageable);

  List<Review> findAllByBeanId(Long beanId);

  void deleteAllByBeanId(Long id);

  void deleteAllByMemberId(Long memberId);
}
