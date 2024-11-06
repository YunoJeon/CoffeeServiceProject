package com.coffee.coffeeserviceproject.favorite.repository;

import com.coffee.coffeeserviceproject.bean.entity.Bean;
import com.coffee.coffeeserviceproject.favorite.entity.Favorite;
import com.coffee.coffeeserviceproject.member.entity.Member;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

  Page<Favorite> findAllByMemberId(long memberId, Pageable pageable);

  void deleteAllByBeanId(Long id);

  void deleteAllByMemberId(Long memberId);

  Optional<Favorite> findByMemberAndBean(Member member, Bean bean);
}
