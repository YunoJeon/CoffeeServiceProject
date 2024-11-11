package com.coffee.coffeeserviceproject.member.dto;

import com.coffee.coffeeserviceproject.common.annotation.ValidNotNull;
import com.coffee.coffeeserviceproject.common.annotation.ValidPhone;
import com.coffee.coffeeserviceproject.member.entity.Roaster;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "로스터 등록 DTO")
public class RoasterDto {

  @ValidNotNull(message = "로스터명은 필수 입력사항 입니다.")
  @Schema(description = "로스터 이름")
  private String roasterName;

  @ValidNotNull(message = "매장 주소는 필수 입력사항 입니다.")
  @Schema(description = "매장 주소")
  private String officeAddress;

  @ValidPhone
  @Schema(description = "매장 연락처", nullable = true)
  private String contactInfo;

  @Schema(description = "매장 or 자기 소개", nullable = true)
  private String description;

  public static RoasterDto fromEntity(Roaster roaster) {

    return RoasterDto.builder()
        .roasterName(roaster.getRoasterName())
        .officeAddress(roaster.getOfficeAddress())
        .contactInfo(roaster.getContactInfo())
        .description(roaster.getDescription())
        .build();
  }
}