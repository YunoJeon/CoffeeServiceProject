package com.coffee.coffeeserviceproject.bean.service;

import static com.coffee.coffeeserviceproject.bean.type.PurchaseStatus.POSSIBLE;
import static com.coffee.coffeeserviceproject.common.type.ErrorCode.NOT_FOUND_BEAN;
import static com.coffee.coffeeserviceproject.common.type.ErrorCode.NOT_PERMISSION;
import static com.coffee.coffeeserviceproject.common.type.ErrorCode.PRICE_REQUIRED;
import static com.coffee.coffeeserviceproject.common.type.ErrorCode.PURCHASE_STATUS_REQUIRED;
import static com.coffee.coffeeserviceproject.member.type.RoleType.SELLER;

import com.coffee.coffeeserviceproject.bean.dto.BeanDto;
import com.coffee.coffeeserviceproject.bean.dto.BeanListDto;
import com.coffee.coffeeserviceproject.bean.dto.BeanUpdateDto;
import com.coffee.coffeeserviceproject.bean.entity.Bean;
import com.coffee.coffeeserviceproject.bean.repository.BeanRepository;
import com.coffee.coffeeserviceproject.bean.type.PurchaseStatus;
import com.coffee.coffeeserviceproject.common.exception.CustomException;
import com.coffee.coffeeserviceproject.configuration.JwtProvider;
import com.coffee.coffeeserviceproject.elasticsearch.document.SearchBeanList;
import com.coffee.coffeeserviceproject.elasticsearch.repository.SearchRepository;
import com.coffee.coffeeserviceproject.favorite.repository.FavoriteRepository;
import com.coffee.coffeeserviceproject.member.entity.Member;
import com.coffee.coffeeserviceproject.member.type.RoleType;
import com.coffee.coffeeserviceproject.review.repository.ReviewRepository;
import com.coffee.coffeeserviceproject.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BeanService {

  private final BeanRepository beanRepository;

  private final JwtProvider jwtProvider;

  private final SearchRepository searchRepository;

  private final ReviewRepository reviewRepository;

  private final ReviewService reviewService;

  private final FavoriteRepository favoriteRepository;

  @Transactional
  public void addBean(BeanDto beanDto, String token) {

    Member member = jwtProvider.getMemberFromEmail(token);

    Bean bean = Bean.builder()
        .member(member)
        .averageScore(0.0)
        .beanName(beanDto.getBeanName())
        .beanState(beanDto.getBeanState())
        .beanFarm(beanDto.getBeanFarm())
        .beanRegion(beanDto.getBeanRegion())
        .beanVariety(beanDto.getBeanVariety())
        .altitude(beanDto.getAltitude())
        .process(beanDto.getProcess())
        .grade(beanDto.getGrade())
        .roastingLevel(beanDto.getRoastingLevel())
        .roastingDate(beanDto.getRoastingDate())
        .cupNote(beanDto.getCupNote())
        .espressoRecipe(beanDto.getEspressoRecipe())
        .filterRecipe(beanDto.getFilterRecipe())
        .milkPairing(beanDto.getMilkPairing())
        .signatureVariation(beanDto.getSignatureVariation())
        .price(null)
        .purchaseStatus(null)
        .build();

    if (member.getRole() == SELLER) {
      if (beanDto.getPurchaseStatus() == null || beanDto.getPurchaseStatus().toString().isEmpty()) {
        throw new CustomException(PURCHASE_STATUS_REQUIRED);
      }

      if (beanDto.getPrice() == null) {
        throw new CustomException(PRICE_REQUIRED);
      }

      bean.setPurchaseStatus(beanDto.getPurchaseStatus());
      bean.setPrice(beanDto.getPrice());
    }

    Bean save = beanRepository.save(bean);

    String roasterName = null;
    String purchaseStatus = null;
    String role = null;
    if (member.getRoaster() != null) {
      roasterName = member.getRoaster().getRoasterName();
      role = member.getRole().name();
      purchaseStatus = save.getPurchaseStatus().name();
    }

    SearchBeanList searchBeanList = SearchBeanList.builder()
        .beanId(save.getId())
        .beanName(save.getBeanName())
        .averageScore(save.getAverageScore())
        .roasterName(roasterName)
        .purchaseStatus(purchaseStatus)
        .role(role)
        .build();

    searchRepository.save(searchBeanList);
  }

  @Transactional(readOnly = true)
  public Page<BeanListDto> getBeanList(Pageable pageable, RoleType role,
      PurchaseStatus purchaseStatus) {

    Page<Bean> beanList;

    if (role == null && purchaseStatus == null) {
      beanList = beanRepository.findAllByOrderByIdDesc(pageable);
    } else if (role == null) {
      beanList = beanRepository.findByPurchaseStatus(POSSIBLE, pageable);
    } else if (purchaseStatus == null) {
      beanList = beanRepository.findByMemberRole(SELLER, pageable);
    } else {
      beanList = beanRepository.findByMemberRoleAndPurchaseStatus(SELLER, POSSIBLE, pageable);
    }

    return beanList.map(bean -> BeanListDto.builder()
        .beanId(bean.getId())
        .beanName(bean.getBeanName())
        .averageScore(bean.getAverageScore())
        .roasterName(bean.getRoasterName())
        .build());
  }

  @Transactional(readOnly = true)
  public BeanDto getBean(Long id) {

    Bean bean = beanRepository.findById(id)
        .orElseThrow(() -> new CustomException(NOT_FOUND_BEAN));

    return BeanDto.fromEntity(bean);
  }

  @Transactional
  public void updateBean(Long id, BeanUpdateDto beanUpdateDto, String token) {

    Bean bean = beanRepository.findById(id)
        .orElseThrow(() -> new CustomException(NOT_FOUND_BEAN));

    SearchBeanList searchBeanList = searchRepository.findById(bean.getId())
        .orElseThrow(() -> new CustomException(NOT_FOUND_BEAN));

    Member member = jwtProvider.getMemberFromEmail(token);

    if (!bean.getMember().getId().equals(member.getId())) {
      throw new CustomException(NOT_PERMISSION);
    }

    if (beanUpdateDto.getBeanName() != null) {
      bean.setBeanName(beanUpdateDto.getBeanName());
      searchBeanList.setBeanName(beanUpdateDto.getBeanName());
    }

    if (beanUpdateDto.getBeanState() != null) {
      bean.setBeanState(beanUpdateDto.getBeanState());
    }

    if (beanUpdateDto.getBeanRegion() != null) {
      bean.setBeanRegion(beanUpdateDto.getBeanRegion());
    }

    if (beanUpdateDto.getBeanFarm() != null) {
      bean.setBeanFarm(beanUpdateDto.getBeanFarm());
    }

    if (beanUpdateDto.getBeanVariety() != null) {
      bean.setBeanVariety(beanUpdateDto.getBeanVariety());
    }

    if (beanUpdateDto.getAltitude() != null) {
      bean.setAltitude(beanUpdateDto.getAltitude());
    }

    if (beanUpdateDto.getProcess() != null) {
      bean.setProcess(beanUpdateDto.getProcess());
    }

    if (beanUpdateDto.getGrade() != null) {
      bean.setGrade(beanUpdateDto.getGrade());
    }

    if (beanUpdateDto.getRoastingLevel() != null) {
      bean.setRoastingLevel(beanUpdateDto.getRoastingLevel());
    }

    if (beanUpdateDto.getRoastingDate() != null) {
      bean.setRoastingDate(beanUpdateDto.getRoastingDate());
    }

    if (beanUpdateDto.getCupNote() != null) {
      bean.setCupNote(beanUpdateDto.getCupNote());
    }

    if (beanUpdateDto.getEspressoRecipe() != null) {
      bean.setEspressoRecipe(beanUpdateDto.getEspressoRecipe());
    }

    if (beanUpdateDto.getFilterRecipe() != null) {
      bean.setFilterRecipe(beanUpdateDto.getFilterRecipe());
    }

    if (beanUpdateDto.getMilkPairing() != null) {
      bean.setMilkPairing(beanUpdateDto.getMilkPairing());
    }

    if (beanUpdateDto.getSignatureVariation() != null) {
      bean.setSignatureVariation(beanUpdateDto.getSignatureVariation());
    }

    if (bean.getMember().getRole() == SELLER) {
      if (beanUpdateDto.getPrice() != null) {
        bean.setPrice(beanUpdateDto.getPrice());
      }

      if (beanUpdateDto.getPurchaseStatus() != null) {
        bean.setPurchaseStatus(beanUpdateDto.getPurchaseStatus());
      }
    } else {
      if (beanUpdateDto.getPrice() != null || beanUpdateDto.getPurchaseStatus() != null) {
        throw new CustomException(NOT_PERMISSION);
      }
    }

    beanRepository.save(bean);
    searchRepository.save(searchBeanList);
  }

  @Transactional
  public void deleteBean(Long id, String token) {

    Bean bean = beanRepository.findById(id)
        .orElseThrow(() -> new CustomException(NOT_FOUND_BEAN));

    searchRepository.findById(id)
        .orElseThrow(() -> new CustomException(NOT_FOUND_BEAN));

    Member member = jwtProvider.getMemberFromEmail(token);

    if (!bean.getMember().getId().equals(member.getId())) {
      throw new CustomException(NOT_PERMISSION);
    }

    reviewService.updateAverageScore(id);

    deleteDataAsync(id);
  }

  @Async
  public void deleteDataAsync(Long beanId) {

    searchRepository.deleteById(beanId);
    favoriteRepository.deleteAllByBeanId(beanId);
    reviewRepository.deleteAllByBeanId(beanId);
    beanRepository.deleteById(beanId);
  }
}
