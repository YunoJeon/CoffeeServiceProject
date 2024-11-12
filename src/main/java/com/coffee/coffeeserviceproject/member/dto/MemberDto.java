package com.coffee.coffeeserviceproject.member.dto;

import com.coffee.coffeeserviceproject.member.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "회원 등록 DTO")
public class MemberDto {

  @NotEmpty(message = "이름은 필수 입력사항 입니다.")
  @Size(max = 50)
  @Schema(description = "회원 이름")
  private String memberName;

  @NotEmpty(message = "휴대폰번호는 필수 입력사항 입니다.")
  @Pattern(regexp = "01(?:0|1|[6-9])[.-]?(\\d{3}|\\d{4})[.-]?(\\d{4})$", message = "대한민국 전화번호 형식에 맞게 입력해주세요.")
  @Schema(description = "휴대폰 번호")
  private String phone;

  @NotEmpty(message = "비밀번호는 필수 입력사항 입니다.")
  @Size(min = 8, message = "비밀번호는 최소 8자 이상으로 이루어져 있어야 합니다.")
  @Schema(description = "비밀번호")
  private String password;

  @Email(message = "이메일 형식이 아닙니다.")
  @NotEmpty(message = "이메일은 필수 입력사항 입니다.")
  @Schema(description = "이메일")
  private String email;

  @NotEmpty(message = "주소는 필수 입력사항 입니다.")
  @Schema(description = "주소")
  private String address;

  @Schema(description = "로스터 이름(회원 조회 시 로스터가 있으면 자동 입력)")
  private RoasterDto roasterDto;

  public static MemberDto fromEntity(Member member) {

    return MemberDto.builder()
        .memberName(member.getMemberName())
        .phone(member.getPhone())
        .email(member.getEmail())
        .address(member.getAddress())
        .build();
  }
}