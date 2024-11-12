package com.coffee.coffeeserviceproject.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "회원 삭제 DTO")
public class MemberDeleteDto {

  @NotNull(message = "현재 비밀번호는 필수입니다.")
  @Size(min = 8, message = "비밀번호는 최소 8자 이상으로 이루어져 있어야 합니다.")
  @Schema(description = "삭제 확인용 비밀번호")
  private String confirmPassword;
}