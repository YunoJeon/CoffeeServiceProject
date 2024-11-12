package com.coffee.coffeeserviceproject.elasticsearch.document;

import com.coffee.coffeeserviceproject.bean.entity.Bean;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Document(indexName = "search_bean")
@Builder
@Data
@Schema(description = "원두, 로스터 검색 결과 Index")
public class SearchBeanList {

  @Id
  @Schema(description = "원두 ID - 메인 DB 와 매핑")
  private Long beanId;

  @Schema(description = "원두 이름")
  private String beanName;

  @Schema(description = "평균 별점")
  private Double averageScore;

  @Schema(description = "로스터 이름")
  private String roasterName;

  @Schema(description = "해당 원두 등록한 회원 유형")
  private String role;

  @Schema(description = "판매 상태")
  private String purchaseStatus;

  @Schema(description = "조회수")
  private Long viewCount;

  public static SearchBeanList fromBeanEntity(
      Bean bean, String roasterName, String purchaseStatus, String role) {

    return SearchBeanList.builder()
        .beanId(bean.getId())
        .beanName(bean.getBeanName())
        .averageScore(bean.getAverageScore())
        .roasterName(roasterName)
        .purchaseStatus(purchaseStatus)
        .role(role)
        .viewCount(bean.getViewCount())
        .build();
  }
}
