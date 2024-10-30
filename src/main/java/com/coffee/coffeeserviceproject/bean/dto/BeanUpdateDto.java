package com.coffee.coffeeserviceproject.bean.dto;

import com.coffee.coffeeserviceproject.bean.type.PurchaseStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BeanUpdateDto {

  private String beanName;

  private String beanState;

  private String beanRegion;

  private String beanFarm;

  private String beanVariety;

  private String altitude;

  private String process;

  private String grade;

  private String roastingLevel;

  private String roastingDate;

  private String cupNote;

  private String espressoRecipe;

  private String filterRecipe;

  private String milkPairing;

  private String signatureVariation;

  private Long price;

  private PurchaseStatus purchaseStatus;
}
