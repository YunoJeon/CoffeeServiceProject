package com.coffee.coffeeserviceproject.member.controller;

import com.coffee.coffeeserviceproject.member.service.MailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Mail API", description = "메일전송 API")
public class MailController {

  private final MailService mailService;

  @GetMapping("/verification")
  @Operation(summary = "메일 전송", description = "회원 가입, 회원 이메일 수정 시 메일이 자동 전송 됩니다.")
  public ResponseEntity<String> verify(@RequestParam String token) {

    mailService.verifyEmail(token);

    return ResponseEntity.ok("이메일 인증이 완료되었습니다.");
  }
}