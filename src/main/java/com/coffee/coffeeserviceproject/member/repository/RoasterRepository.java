package com.coffee.coffeeserviceproject.member.repository;

import com.coffee.coffeeserviceproject.member.entity.Roaster;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoasterRepository extends JpaRepository<Roaster, Long> {

  Optional<Roaster> findByRoasterName(String roasterName);

  Roaster findByMemberId(Long id);
}
