package com.coffee.coffeeserviceproject.favorite.service;

import static com.coffee.coffeeserviceproject.common.type.ErrorCode.ALREADY_FAVORITE_BEAN;
import static com.coffee.coffeeserviceproject.common.type.ErrorCode.NOT_FOUND_BEAN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.coffee.coffeeserviceproject.bean.entity.Bean;
import com.coffee.coffeeserviceproject.bean.repository.BeanRepository;
import com.coffee.coffeeserviceproject.common.exception.CustomException;
import com.coffee.coffeeserviceproject.configuration.JwtProvider;
import com.coffee.coffeeserviceproject.favorite.dto.FavoriteDto;
import com.coffee.coffeeserviceproject.favorite.entity.Favorite;
import com.coffee.coffeeserviceproject.favorite.repository.FavoriteRepository;
import com.coffee.coffeeserviceproject.member.entity.Member;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

  @Mock
  private FavoriteRepository favoriteRepository;

  @Mock
  private JwtProvider jwtProvider;

  @Mock
  private BeanRepository beanRepository;

  @InjectMocks
  private FavoriteService favoriteService;

  private Member member;
  private Bean bean;
  private Favorite favorite;
  private String token;

  @BeforeEach
  void setUp() {

    token = "token";

    member = new Member();
    member.setId(1L);

    bean = new Bean();
    bean.setId(1L);

    favorite = new Favorite();
    favorite.setMember(member);
    favorite.setBean(bean);
  }

  @Test
  void addFavorite_Success() {
    // given
    when(jwtProvider.getMemberFromEmail(token)).thenReturn(member);
    when(beanRepository.findById(bean.getId())).thenReturn(Optional.of(bean));
    // when
    favoriteService.addFavorite(bean.getId(), token);
    // then
    verify(favoriteRepository).save(favorite);
  }

  @Test
  void addFavorite_Failure_AlreadyFavoriteBean() {
    // given
    Favorite existsFavorite = Favorite.builder()
        .id(2L)
        .member(member)
        .bean(bean)
        .build();

    when(jwtProvider.getMemberFromEmail(token)).thenReturn(member);
    when(beanRepository.findById(bean.getId())).thenReturn(Optional.of(bean));
    when(favoriteRepository.findByMemberAndBean(member, bean)).thenReturn(Optional.of(existsFavorite));
    // when
    CustomException e = assertThrows(CustomException.class,
        () -> favoriteService.addFavorite(bean.getId(), token));
    // then
    assertEquals(ALREADY_FAVORITE_BEAN, e.getErrorCode());
    verify(favoriteRepository, never()).save(favorite);
  }

  @Test
  void addFavorite_Failure_NotFoundBean() {
    // given
    when(jwtProvider.getMemberFromEmail(token)).thenReturn(member);
    when(beanRepository.findById(bean.getId())).thenReturn(Optional.empty());
    // when
    CustomException e = assertThrows(CustomException.class,
        () -> favoriteService.addFavorite(bean.getId(), token));
    // then
    assertEquals(NOT_FOUND_BEAN, e.getErrorCode());
    verify(favoriteRepository, never()).save(favorite);
  }

  @Test
  void getFavorites_Success() {
    // given
    Pageable pageable = Pageable.ofSize(10);

    Favorite favorite2 = Favorite.builder()
        .id(2L)
        .member(member)
        .bean(bean)
        .build();

    Page<Favorite> getFavoritePage = new PageImpl<>(List.of(favorite, favorite2));
    when(jwtProvider.getMemberFromEmail(token)).thenReturn(member);
    when(favoriteRepository.findAllByMemberId(member.getId(), pageable)).thenReturn(getFavoritePage);
    // when
    Page<FavoriteDto> favoritePage = favoriteService.getFavoriteList(token, pageable);
    // then
    assertNotNull(favoritePage);
    assertEquals(2, favoritePage.getTotalElements());
  }
}