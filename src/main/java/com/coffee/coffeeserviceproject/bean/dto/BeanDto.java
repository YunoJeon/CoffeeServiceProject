package com.coffee.coffeeserviceproject.bean.dto;

import com.coffee.coffeeserviceproject.bean.entity.Bean;
import com.coffee.coffeeserviceproject.bean.type.PurchaseStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BeanDto {

  @NotBlank(message = "원두명을 입력해 주세요.")
  private String beanName;

  @NotBlank(message = "원두 산지를 입력해 주세요.")
  private String beanState;

  private String beanRegion;

  private String beanFarm;

  @NotBlank(message = "품종을 입력해 주세요.")
  private String beanVariety;

  @NotBlank(message = "고도를 입력해 주세요.")
  private String altitude;

  @NotBlank(message = "가공법을 입력해 주세요.")
  private String process;

  private String grade;

  @NotBlank(message = "로스팅 레벨을 입력해 주세요.")
  private String roastingLevel;

  private String roastingDate;

  @NotBlank(message = "컵노트를 입력해 주세요.")
  private String cupNote;

  private String espressoRecipe;

  private String filterRecipe;

  private String milkPairing;

  private String signatureVariation;

  private Long price;

  private PurchaseStatus purchaseStatus;

  private String memberName;

  private Double averageScore;

  public static BeanDto fromEntity(Bean bean) {

    return BeanDto.builder()
        .memberName(bean.getMember().getMemberName())
        .beanName(bean.getBeanName())
        .beanState(bean.getBeanState())
        .beanRegion(bean.getBeanRegion())
        .beanFarm(bean.getBeanFarm())
        .beanVariety(bean.getBeanVariety())
        .altitude(bean.getAltitude())
        .process(bean.getProcess())
        .grade(bean.getGrade())
        .roastingLevel(bean.getRoastingLevel())
        .roastingDate(bean.getRoastingDate())
        .cupNote(bean.getCupNote())
        .espressoRecipe(bean.getEspressoRecipe())
        .filterRecipe(bean.getFilterRecipe())
        .milkPairing(bean.getMilkPairing())
        .signatureVariation(bean.getSignatureVariation())
        .price(bean.getPrice())
        .purchaseStatus(bean.getPurchaseStatus())
        .averageScore(bean.getAverageScore())
        .build();
  }
}
