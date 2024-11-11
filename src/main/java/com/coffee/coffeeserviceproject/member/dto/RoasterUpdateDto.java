package com.coffee.coffeeserviceproject.member.dto;

import com.coffee.coffeeserviceproject.common.annotation.ValidPhone;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "로스터 수정 DTO")
public class RoasterUpdateDto {

  @Schema(description = "로스터 이름", nullable = true)
  private String roasterName;

  @Schema(description = "매장 주소", nullable = true)
  private String officeAddress;

  @ValidPhone
  @Schema(description = "매장 연락처", nullable = true)
  private String contactInfo;

  @Schema(description = "매장 or 자기 소개", nullable = true)
  private String description;

  @NotNull(message = "현재 비밀번호는 필수입니다.")
  @Size(min = 8, message = "비밀번호는 최소 8자 이상으로 이루어져 있어야 합니다.")
  @Schema(description = "수정 확인용 비밀번호")
  private String password;
}