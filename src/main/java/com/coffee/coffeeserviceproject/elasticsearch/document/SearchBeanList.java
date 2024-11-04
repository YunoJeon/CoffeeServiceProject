package com.coffee.coffeeserviceproject.elasticsearch.document;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Document(indexName = "search_bean")
@Builder
@Data
public class SearchBeanList {

  @Id
  private Long beanId;

  private String beanName;

  private Double averageScore;

  private String roasterName;

  private String role;

  private String purchaseStatus;
}
