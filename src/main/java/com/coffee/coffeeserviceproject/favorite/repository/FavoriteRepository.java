package com.coffee.coffeeserviceproject.favorite.repository;

import com.coffee.coffeeserviceproject.bean.entity.Bean;
import com.coffee.coffeeserviceproject.favorite.entity.Favorite;
import com.coffee.coffeeserviceproject.member.entity.Member;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

  Page<Favorite> findAllByMemberId(long memberId, Pageable pageable);

  @Modifying
  @Query(value = "delete from favorite where bean_id = :id", nativeQuery = true)
  void deleteAllByBeanId(Long id);

  @Modifying
  @Query(value = "delete from favorite where member_id = :memberId", nativeQuery = true)
  void deleteAllByMemberId(Long memberId);

  Optional<Favorite> findByMemberAndBean(Member member, Bean bean);
}
