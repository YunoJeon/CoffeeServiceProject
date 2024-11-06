package com.coffee.coffeeserviceproject.order.transaction.repository;

import com.coffee.coffeeserviceproject.order.transaction.entity.Transaction;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

  Optional<Transaction> findByMerchantUid(String merchantUid);

  Page<Transaction> findByMemberId(Long memberId, Pageable pageable);

  Page<Transaction> findByRoasterId(Long roasterId, Pageable pageable);
}
