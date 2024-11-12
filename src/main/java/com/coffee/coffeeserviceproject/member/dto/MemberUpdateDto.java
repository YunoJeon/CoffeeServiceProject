package com.coffee.coffeeserviceproject.member.dto;

import com.coffee.coffeeserviceproject.common.annotation.ValidEmail;
import com.coffee.coffeeserviceproject.common.annotation.ValidPassword;
import com.coffee.coffeeserviceproject.common.annotation.ValidPhone;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "회원 수정 DTO")
public class MemberUpdateDto {

  @ValidPhone
  @Schema(description = "휴대폰 번호", nullable = true)
  private String phone;

  @ValidPassword
  @Schema(description = "비밀번호", nullable = true)
  private String password;

  @ValidEmail
  @Schema(description = "이메일(이메일 수정 시 인증은 취소되고 메일이 새로 발송)", nullable = true)
  private String email;

  @Schema(description = "주소", nullable = true)
  private String address;

  @NotNull(message = "현재 비밀번호는 필수입니다.")
  @Size(min = 8, message = "비밀번호는 최소 8자 이상으로 이루어져 있어야 합니다.")
  @Schema(description = "수정 확인용 비밀번호")
  private String currentPassword;
}