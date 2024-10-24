package com.coffee.coffeeserviceproject.member.dto;

import com.coffee.coffeeserviceproject.common.annotation.ValidNotNull;
import com.coffee.coffeeserviceproject.common.annotation.ValidPhone;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoasterDto {

  @ValidNotNull(message = "로스터명은 필수 입력사항 입니다.")
  private String roasterName;

  @ValidNotNull(message = "매장 주소는 필수 입력사항 입니다.")
  private String officeAddress;

  @ValidPhone
  private String contactInfo;

  private String description;
}