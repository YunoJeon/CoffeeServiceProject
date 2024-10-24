package com.coffee.coffeeserviceproject.member.controller;

import com.coffee.coffeeserviceproject.member.dto.MemberDeleteDto;
import com.coffee.coffeeserviceproject.member.dto.MemberDto;
import com.coffee.coffeeserviceproject.member.dto.MemberLoginDto;
import com.coffee.coffeeserviceproject.member.dto.MemberUpdateDto;
import com.coffee.coffeeserviceproject.member.service.MemberService;
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
public class MemberController {

  private final MemberService memberService;

  @PostMapping
  public ResponseEntity<Void> addMember(@RequestBody @Valid MemberDto memberDto) {

    memberService.addMember(memberDto);

    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @PostMapping("/login")
  public ResponseEntity<String> login(@RequestBody @Valid MemberLoginDto memberLoginDto) {

    String token = memberService.login(memberLoginDto.getEmail(), memberLoginDto.getPassword());

    return ResponseEntity.ok(token);
  }

  @GetMapping("/info")
  public ResponseEntity<MemberDto> getMember(
      @RequestHeader("AUTH-TOKEN") String token) {

    MemberDto memberDto = memberService.getMember(token);

    return ResponseEntity.ok(memberDto);
  }

  @PatchMapping("/update")
  public ResponseEntity<Void> updateMember(@RequestHeader("AUTH-TOKEN") String token,
      @RequestBody @Valid MemberUpdateDto memberUpdateDto) {

    memberService.updateMember(token, memberUpdateDto);

    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/cancel-membership")
  public ResponseEntity<Void> deleteMember(@RequestHeader("AUTH-TOKEN") String token,
      @RequestBody @Valid MemberDeleteDto memberDeleteDto) {

    memberService.deleteMember(token, memberDeleteDto);

    return ResponseEntity.noContent().build();
  }
}