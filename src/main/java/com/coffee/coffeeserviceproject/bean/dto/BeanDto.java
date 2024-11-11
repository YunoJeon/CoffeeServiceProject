package com.coffee.coffeeserviceproject.bean.dto;

import com.coffee.coffeeserviceproject.bean.entity.Bean;
import com.coffee.coffeeserviceproject.bean.type.PurchaseStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "원두 등록 DTO")
public class BeanDto {

  @NotBlank(message = "원두명을 입력해 주세요.")
  @Schema(description = "원두 이름", example = "에티오피아 아리차 내추럴 G1")
  private String beanName;

  @NotBlank(message = "원두 산지를 입력해 주세요.")
  @Schema(description = "원두 생산 국가", example = "에티오피아")
  private String beanState;

  @Schema(description = "원두 생산 지역", example = "안티구아", nullable = true)
  private String beanRegion;

  @Schema(description = "원두 농장", example = "라 에스메랄다", nullable = true)
  private String beanFarm;

  @NotBlank(message = "품종을 입력해 주세요.")
  @Schema(description = "품종", example = "게이샤")
  private String beanVariety;

  @NotBlank(message = "고도를 입력해 주세요.")
  @Schema(description = "고도", example = "1800m")
  private String altitude;

  @NotBlank(message = "가공법을 입력해 주세요.")
  @Schema(description = "가공법", example = "anaerobic")
  private String process;

  @Schema(description = "등급", example = "SHB", nullable = true)
  private String grade;

  @NotBlank(message = "로스팅 레벨을 입력해 주세요.")
  @Schema(description = "로스팅 레벨", example = "right")
  private String roastingLevel;

  @Schema(description = "로스팅 날짜", example = "2024-11-11", nullable = true)
  private String roastingDate;

  @NotBlank(message = "컵노트를 입력해 주세요.")
  @Schema(description = "컵노트", example = "복합적인 과일 산미, 긴 여운, 밀크 초콜릿, 주스같은 마우스필")
  private String cupNote;

  @Schema(description = "에스프레소 레시피", example = "말코닉 기준 분쇄도 : --, --g 도징, --초, --g 추출", nullable = true)
  private String espressoRecipe;

  @Schema(description = "필터 레시피", example = "말코닉 기준 분쇄도 : --, 추천 드리퍼 : --, 물 온도 : --도, --g 도징, --초, --g 추출", nullable = true)
  private String filterRecipe;

  @Schema(description = "추천 우유", example = "--우유 마스터 or 바리스타 등", nullable = true)
  private String milkPairing;

  @Schema(description = "시그니처 레시피", example = "에스프레소 --샷, --우유 --ml, --시럽 --g, --생크림 --g", nullable = true)
  private String signatureVariation;

  @Schema(description = "판매가격(판매자만 필수 입력)", defaultValue = "null", nullable = true)
  private Long price;

  @Schema(description = "판매상태(판매자만 필수 입력)", defaultValue = "IMPOSSIBLE", nullable = true)
  private PurchaseStatus purchaseStatus;

  @Schema(description = "등록한 회원(원두 등록 시 자동입력)")
  private String memberName;

  @Schema(description = "평균 별점(리뷰 작성 시 자동입력)")
  private Double averageScore;

  @Schema(description = "조회수(원두 조회 시 자동입력)")
  private Long viewCount;

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
        .viewCount(bean.getViewCount())
        .build();
  }
}
