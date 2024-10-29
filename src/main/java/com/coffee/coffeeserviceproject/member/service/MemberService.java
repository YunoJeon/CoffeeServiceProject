package com.coffee.coffeeserviceproject.member.service;

import static com.coffee.coffeeserviceproject.common.type.ErrorCode.ALREADY_EXISTS_USER;
import static com.coffee.coffeeserviceproject.common.type.ErrorCode.LOGIN_ERROR;
import static com.coffee.coffeeserviceproject.common.type.ErrorCode.WRONG_PASSWORD;
import static com.coffee.coffeeserviceproject.member.type.RoleType.BUYER;
import static com.coffee.coffeeserviceproject.member.type.RoleType.SELLER;

import com.coffee.coffeeserviceproject.common.exception.CustomException;
import com.coffee.coffeeserviceproject.member.dto.MemberDeleteDto;
import com.coffee.coffeeserviceproject.member.dto.MemberDto;
import com.coffee.coffeeserviceproject.member.dto.MemberUpdateDto;
import com.coffee.coffeeserviceproject.member.dto.RoasterDto;
import com.coffee.coffeeserviceproject.member.entity.Member;
import com.coffee.coffeeserviceproject.member.entity.Roaster;
import com.coffee.coffeeserviceproject.member.repository.MemberRepository;
import com.coffee.coffeeserviceproject.util.JwtUtil;
import com.coffee.coffeeserviceproject.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

  private final MemberRepository memberRepository;

  private final MailService mailService;

  private final JwtUtil jwtUtil;

  public void addMember(MemberDto memberDto) {

    if (memberRepository.findByEmail(memberDto.getEmail()).isPresent()) {
      throw new CustomException(ALREADY_EXISTS_USER);
    }

    String encodePassword = PasswordUtil.hashPassword(memberDto.getPassword());

    Member member = Member.builder()
        .memberName(memberDto.getMemberName())
        .phone(memberDto.getPhone())
        .email(memberDto.getEmail())
        .password(encodePassword)
        .address(memberDto.getAddress())
        .role(BUYER)
        .build();

    memberRepository.save(member);

    mailService.sendEmail(memberDto.getEmail());
  }

  public String login(String email, String password) {

    Member member = memberRepository.findByEmail(email).orElse(null);

    if (member == null || !PasswordUtil.matches(password, member.getPassword())
        || member.getCertificationAt() == null) {
      throw new CustomException(LOGIN_ERROR);
    }

    return jwtUtil.generateToken(email);
  }

  public MemberDto getMember(String token) {

    Member member = jwtUtil.getMemberFromEmail(token);

    MemberDto memberDto = MemberDto.builder()
        .memberName(member.getMemberName())
        .phone(member.getPhone())
        .email(member.getEmail())
        .address(member.getAddress())
        .build();

    if (member.getRole() == SELLER) {
      Roaster roaster = member.getRoaster();
      if (roaster != null) {
        RoasterDto roasterDto = RoasterDto.builder()
            .roasterName(roaster.getRoasterName())
            .officeAddress(roaster.getOfficeAddress())
            .contactInfo(roaster.getContactInfo())
            .description(roaster.getDescription())
            .build();

        memberDto.setRoasterDto(roasterDto);
      }
    }

    return memberDto;
  }

  public void updateMember(String token, MemberUpdateDto memberUpdateDto) {

    Member member = jwtUtil.getMemberFromEmail(token);

    if (!PasswordUtil.matches(memberUpdateDto.getCurrentPassword(), member.getPassword())) {
      throw new CustomException(WRONG_PASSWORD);
    }

    if (memberUpdateDto.getPhone() != null) {
      member.setPhone(memberUpdateDto.getPhone());
    }

    if (memberUpdateDto.getEmail() != null && !member.getEmail()
        .equals(memberUpdateDto.getEmail())) {

      if (memberRepository.existsByEmail(memberUpdateDto.getEmail())) {
        throw new CustomException(ALREADY_EXISTS_USER);
      }

      member.setEmail(memberUpdateDto.getEmail());
      member.setCertificationAt(null);
    }

    if (memberUpdateDto.getAddress() != null) {
      member.setAddress(memberUpdateDto.getAddress());
    }

    if (memberUpdateDto.getPassword() != null) {
      member.setPassword(PasswordUtil.hashPassword(memberUpdateDto.getPassword()));
    }

    memberRepository.save(member);

    if (member.getCertificationAt() == null) {
      mailService.sendEmail(memberUpdateDto.getEmail());
    }
  }

  public void deleteMember(String token, MemberDeleteDto memberDeleteDto) {

    Member member = jwtUtil.getMemberFromEmail(token);

    if (!PasswordUtil.matches(memberDeleteDto.getConfirmPassword(), member.getPassword())) {
      throw new CustomException(WRONG_PASSWORD);
    }

    memberRepository.delete(member);
  }
}