package com.coffee.coffeeserviceproject.member.controller;

import com.coffee.coffeeserviceproject.member.dto.MemberDeleteDto;
import com.coffee.coffeeserviceproject.member.dto.MemberDto;
import com.coffee.coffeeserviceproject.member.dto.MemberLoginDto;
import com.coffee.coffeeserviceproject.member.dto.MemberUpdateDto;
import com.coffee.coffeeserviceproject.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
@Tag(name = "Members API", description = "회원 관련 API")
public class MemberController {

  private final MemberService memberService;

  @PostMapping
  @Operation(summary = "회원 가입", description = "이미 등록된 이메일이 있는 경우 회원가입이 되지 않습니다.")
  public ResponseEntity<Void> addMember(@RequestBody @Valid MemberDto memberDto) {

    memberService.addMember(memberDto);

    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @PostMapping("/login")
  @Operation(summary = "로그인", description = "비밀번호 입력은 필수이며, \"AUTH-TOKEN\" 이 발행됩니다.")
  public ResponseEntity<String> login(@RequestBody @Valid MemberLoginDto memberLoginDto) {

    String token = memberService.login(memberLoginDto.getEmail(), memberLoginDto.getPassword());

    return ResponseEntity.ok(token);
  }

  @GetMapping("/info")
  @Operation(summary = "회원 조회", description = "회원 본인의 정보를 조회합니다. 로스터면 로스터 정보도 같이 조회가 됩니다.")
  public ResponseEntity<MemberDto> getMember(
      @RequestHeader("AUTH-TOKEN") String token) {

    MemberDto memberDto = memberService.getMember(token);

    return ResponseEntity.ok(memberDto);
  }

  @PatchMapping("/me")
  @Operation(summary = "회원 수정", description = "수정하지 않는 필드는 \"null\" 값으로 넘기면 해당 필드는 수정되지 않습니다. 또한 수정 시 비밀번호 입력은 필수입니다.")
  public ResponseEntity<Void> updateMember(@RequestHeader("AUTH-TOKEN") String token,
      @RequestBody @Valid MemberUpdateDto memberUpdateDto) {

    memberService.updateMember(token, memberUpdateDto);

    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "회원 탈퇴", description = "회원 탈퇴 시 비밀번호 입력은 필수입니다.")
  @DeleteMapping("/cancel-membership")
  public ResponseEntity<Void> deleteMember(@RequestHeader("AUTH-TOKEN") String token,
      @RequestBody @Valid MemberDeleteDto memberDeleteDto) {

    memberService.deleteMember(token, memberDeleteDto);

    return ResponseEntity.noContent().build();
  }
}