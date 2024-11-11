package com.coffee.coffeeserviceproject.favorite.service;

import static com.coffee.coffeeserviceproject.common.type.ErrorCode.ALREADY_FAVORITE_BEAN;
import static com.coffee.coffeeserviceproject.common.type.ErrorCode.NOT_FOUND_BEAN;
import static com.coffee.coffeeserviceproject.common.type.ErrorCode.NOT_FOUND_FAVORITE;
import static com.coffee.coffeeserviceproject.common.type.ErrorCode.NOT_PERMISSION;

import com.coffee.coffeeserviceproject.bean.entity.Bean;
import com.coffee.coffeeserviceproject.bean.repository.BeanRepository;
import com.coffee.coffeeserviceproject.common.exception.CustomException;
import com.coffee.coffeeserviceproject.configuration.JwtProvider;
import com.coffee.coffeeserviceproject.favorite.dto.FavoriteDto;
import com.coffee.coffeeserviceproject.favorite.entity.Favorite;
import com.coffee.coffeeserviceproject.favorite.repository.FavoriteRepository;
import com.coffee.coffeeserviceproject.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FavoriteService {

  private final FavoriteRepository favoriteRepository;

  private final JwtProvider jwtProvider;

  private final BeanRepository beanRepository;

  @Transactional
  public void addFavorite(Long beanId, String token) {

    Member member = getMemberFromToken(token);

    Bean bean = findByBeanIdFromBeanRepository(beanId);

    findByMemberAndBeanFromFavoriteRepository(member, bean);

    Favorite favorite = Favorite.of(member, bean);

    favoriteRepository.save(favorite);
  }

  @Transactional(readOnly = true)
  public Page<FavoriteDto> getFavoriteList(String token, Pageable pageable) {

    Member member = getMemberFromToken(token);

    Long memberId = member.getId();

    Page<Favorite> favoritePage = favoriteRepository.findAllByMemberId(memberId, pageable);

    return favoritePage.map(FavoriteDto::fromEntity);
  }

  @Transactional
  public void deleteFavorite(Long id, String token) {

    Member member = getMemberFromToken(token);

    Long memberId = member.getId();

    Favorite favorite = findByFavoriteIdFromFavoriteRepository(id);

    if (!memberId.equals(favorite.getMember().getId())) {
      throw new CustomException(NOT_PERMISSION);
    }

    favoriteRepository.delete(favorite);
  }

  private Member getMemberFromToken(String token) {

    return jwtProvider.getMemberFromEmail(token);
  }

  private Bean findByBeanIdFromBeanRepository(Long id) {

    return beanRepository.findById(id).orElseThrow(() -> new CustomException(NOT_FOUND_BEAN));
  }

  private void findByMemberAndBeanFromFavoriteRepository(Member member, Bean bean) {

    favoriteRepository.findByMemberAndBean(member, bean)
        .ifPresent(existsFavorite -> {
          throw new CustomException(ALREADY_FAVORITE_BEAN);
        });
  }

  private Favorite findByFavoriteIdFromFavoriteRepository(Long id) {

    return favoriteRepository.findById(id)
        .orElseThrow(() -> new CustomException(NOT_FOUND_FAVORITE));
  }
}