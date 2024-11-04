package com.coffee.coffeeserviceproject.common.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

  ALREADY_EXISTS_USER("ALREADY_EXISTS_USER", "등록된 계정이 존재합니다."),
  ALREADY_VERIFY("ALREADY_VERIFY", "승인이 완료된 이메일 입니다."),
  NOT_FOUND_USER("NOT_FOUND_USER", "계정을 찾지 못했습니다."),
  MAIL_ERROR("MAIL_ERROR", "메일 전송 중 오류가 발생했습니다."),
  LOGIN_ERROR("LOGIN_ERROR", "이메일 인증을 받지 않았거나, 회원정보가 일치하지 않습니다."),
  NOT_MATCH_TOKEN("NOT_MATCH_TOKEN", "토큰이 만료되었거나, 일치하지 않습니다."),
  INTERNAL_SEVER_ERROR("INTERNAL_SEVER_ERROR", "서버 내부 오류가 발생했습니다."),
  WRONG_PASSWORD("WRONG_PASSWORD", "비밀번호가 일치하지 않습니다."),
  ALREADY_REGISTERED_ROASTER("ALREADY_REGISTERED_ROASTER", "이미 로스터 정보가 등록되어 있습니다."),
  PURCHASE_STATUS_REQUIRED("PURCHASE_STATUS_REQUIRED", "로스터는 판매상태 입력이 필수입니다."),
  PRICE_REQUIRED("PRICE_REQUIRED", "판매상태가 등록된 경우 가격은 필수 입력입니다."),
  NOT_FOUND_BEAN("NOT_FOUND_BEAN", "원두정보를 찾을수 없습니다."),
  NOT_PERMISSION("NOT_PERMISSION", "승인되지 않은 요청입니다."),
  ROASTER_REGISTRATION_FAILED("ROASTER_REGISTRATION_FAILED", "로스터 등록에 실패하였습니다. 고객센터에 문의해 주세요")
  ;

  private final String code;
  private final String message;
}