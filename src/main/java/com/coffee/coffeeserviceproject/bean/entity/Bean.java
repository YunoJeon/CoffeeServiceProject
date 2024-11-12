package com.coffee.coffeeserviceproject.bean.entity;

import com.coffee.coffeeserviceproject.bean.dto.BeanDto;
import com.coffee.coffeeserviceproject.bean.type.PurchaseStatus;
import com.coffee.coffeeserviceproject.member.entity.Member;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Bean {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  private Double averageScore;
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

  @Enumerated(EnumType.STRING)
  private PurchaseStatus purchaseStatus;

  @JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime createdAt;

  @JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime updatedAt;

  private Long viewCount;

  @PrePersist
  public void onCreate() {
    this.createdAt = LocalDateTime.now();
  }

  @PreUpdate
  public void onUpdate() {
    this.updatedAt = LocalDateTime.now();
  }

  public String getRoasterName() {
    if (member != null && member.getRoaster() != null) {
      return member.getRoaster().getRoasterName();
    }
    return null;
  }

  public Long getRoasterId() {
    if (member != null && member.getRoaster() != null) {
      return member.getRoaster().getId();
    }
    return null;
  }

  public static Bean fromDto (Member member, BeanDto beanDto) {

    return Bean.builder()
        .member(member)
        .averageScore(0.0)
        .viewCount(0L)
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
        .signatureVariation(beanDto.getSignatureVariation())
        .price(null)
        .purchaseStatus(null)
        .build();
  }
}
