package com.coffee.coffeeserviceproject.bean.repository;

import com.coffee.coffeeserviceproject.bean.entity.Bean;
import com.coffee.coffeeserviceproject.bean.type.PurchaseStatus;
import com.coffee.coffeeserviceproject.member.type.RoleType;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BeanRepository extends JpaRepository<Bean, Long> {

  List<Bean> findByMemberId(Long id);

  Page<Bean> findAllByOrderByIdDesc(Pageable pageable);

  Page<Bean> findByMemberRole(RoleType role, Pageable pageable);

  Page<Bean> findByPurchaseStatus(PurchaseStatus purchaseStatus, Pageable pageable);

  Page<Bean> findByMemberRoleAndPurchaseStatus(RoleType roleType, PurchaseStatus purchaseStatus, Pageable pageable);
}
