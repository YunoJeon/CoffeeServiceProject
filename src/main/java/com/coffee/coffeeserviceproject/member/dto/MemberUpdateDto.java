package com.coffee.coffeeserviceproject.member.dto;

import com.coffee.coffeeserviceproject.common.annotation.ValidEmail;
import com.coffee.coffeeserviceproject.common.annotation.ValidPassword;
import com.coffee.coffeeserviceproject.common.annotation.ValidPhone;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MemberUpdateDto {

  @ValidPhone
  private String phone;

  @ValidPassword
  private String password;

  @ValidEmail
  private String email;

  private String address;

  @NotNull(message = "현재 비밀번호는 필수입니다.")
  @Size(min = 8, message = "비밀번호는 최소 8자 이상으로 이루어져 있어야 합니다.")
  private String currentPassword;
}