package com.coffee.coffeeserviceproject.member.service;

import static com.coffee.coffeeserviceproject.common.type.ErrorCode.ALREADY_EXISTS_USER;
import static com.coffee.coffeeserviceproject.common.type.ErrorCode.LOGIN_ERROR;
import static com.coffee.coffeeserviceproject.common.type.ErrorCode.NOT_FOUND_USER;
import static com.coffee.coffeeserviceproject.common.type.ErrorCode.NOT_MATCH_TOKEN;
import static com.coffee.coffeeserviceproject.common.type.ErrorCode.WRONG_PASSWORD;
import static com.coffee.coffeeserviceproject.member.type.RoleType.BUYER;
import static com.coffee.coffeeserviceproject.member.type.RoleType.SELLER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.coffee.coffeeserviceproject.common.exception.CustomException;
import com.coffee.coffeeserviceproject.member.dto.MemberDeleteDto;
import com.coffee.coffeeserviceproject.member.dto.MemberDto;
import com.coffee.coffeeserviceproject.member.dto.MemberUpdateDto;
import com.coffee.coffeeserviceproject.member.entity.Member;
import com.coffee.coffeeserviceproject.member.entity.Roaster;
import com.coffee.coffeeserviceproject.member.repository.MemberRepository;
import com.coffee.coffeeserviceproject.util.JwtUtil;
import com.coffee.coffeeserviceproject.util.PasswordUtil;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

  @InjectMocks
  private MemberService memberService;

  @Mock
  private MemberRepository memberRepository;

  @Mock
  private MailService mailService;

  @Mock
  private JwtUtil jwtUtil;

  private Member member;
  private MemberDto memberDto;

  @BeforeEach
  void setUp() {
    member = Member.builder()
        .email("test@mail.com")
        .password("123456789")
        .memberName("이름")
        .phone("010-1234-5678")
        .address("주소")
        .role(BUYER)
        .build();

    memberDto = MemberDto.builder()
        .email("test@mail.com")
        .password(PasswordUtil.hashPassword("123456789"))
        .memberName("이름")
        .phone("010-1234-5678")
        .address("주소")
        .build();
  }

  @Test
  void addMember_Success() {
    // given
    when(memberRepository.findByEmail(memberDto.getEmail()))
        .thenReturn(Optional.empty());
    // when
    memberService.addMember(memberDto);
    // then
    verify(memberRepository).save(any());
    verify(mailService).sendEmail(memberDto.getEmail());
  }

  @Test
  void addMember_AlreadyExists() {
    // given
    when(memberRepository.findByEmail(memberDto.getEmail()))
        .thenReturn(Optional.of(member));
    // when
    CustomException e = assertThrows(CustomException.class,
        () -> memberService.addMember(memberDto));
    // then
    assertEquals(ALREADY_EXISTS_USER, e.getErrorCode());
    verify(memberRepository, never()).save(any());
  }

  @Test
  void login_Success() {
    // given
    member.certify();

    String hashPassword = PasswordUtil.hashPassword(memberDto.getPassword());
    member.setPassword(hashPassword);

    when(memberRepository.findByEmail(memberDto.getEmail()))
        .thenReturn(Optional.of(member));

    when(jwtUtil.generateToken(member.getEmail())).thenReturn("token");
    // when
    String token = memberService.login(memberDto.getEmail(), memberDto.getPassword());
    // then
    assertNotNull(token);
    assertEquals("token", token);
  }

  @Test
  void login_Failure_UserNotFound() {
    // given
    when(memberRepository.findByEmail(memberDto.getEmail()))
        .thenReturn(Optional.empty());
    // when
    CustomException e = assertThrows(CustomException.class,
        () -> memberService.login(memberDto.getEmail(), memberDto.getPassword()));
    // then
    assertEquals(LOGIN_ERROR, e.getErrorCode());
  }

  @Test
  void login_Failure_WrongPassword() {
    // given
    when(memberRepository.findByEmail(memberDto.getEmail()))
        .thenReturn(Optional.of(member));
    // when
    CustomException e = assertThrows(CustomException.class,
        () -> memberService.login(memberDto.getEmail(), "wrongPassword"));
    // then
    assertEquals(LOGIN_ERROR, e.getErrorCode());
  }

  @Test
  void getMember_Success() {
    // given
    when(jwtUtil.getMemberFromEmail(anyString())).thenReturn(member);
    // when
    MemberDto result = memberService.getMember("token");
    // then
    assertNotNull(result);
    assertEquals(member.getEmail(), result.getEmail());
    assertEquals(member.getMemberName(), result.getMemberName());
    assertEquals(member.getPhone(), result.getPhone());
    assertEquals(member.getAddress(), result.getAddress());
  }

  @Test
  void getMember_Failure_InvalidToken() {
    // given
    when(jwtUtil.getMemberFromEmail(anyString())).thenThrow(new CustomException(NOT_MATCH_TOKEN));
    // when
    CustomException e = assertThrows(CustomException.class,
        () -> memberService.getMember("InvalidToken"));
    // then
    assertEquals(NOT_MATCH_TOKEN, e.getErrorCode());
  }

  @Test
  void getMember_Failure_UserNotFound() {
    // given
    when(jwtUtil.getMemberFromEmail(anyString())).thenThrow(new CustomException(NOT_FOUND_USER));
    // when
    CustomException e = assertThrows(CustomException.class, () -> memberService.getMember("token"));
    // then
    assertEquals(NOT_FOUND_USER, e.getErrorCode());
  }

  @Test
  void getMember_WithRoaster_Success() {
    // given
    Roaster roaster = Roaster.builder()
        .roasterName("로스터 명")
        .officeAddress("매장 주소")
        .description("설명")
        .build();
    member.setRole(SELLER);
    member.setRoaster(roaster);

    when(jwtUtil.getMemberFromEmail(anyString())).thenReturn(member);
    // when
    MemberDto result = memberService.getMember("token");
    // then
    assertNotNull(result.getRoasterDto());
    assertEquals(roaster.getRoasterName(), result.getRoasterDto().getRoasterName());
    assertEquals(roaster.getOfficeAddress(), result.getRoasterDto().getOfficeAddress());
    assertEquals(roaster.getDescription(), result.getRoasterDto().getDescription());
  }

  @Test
  void updateMember_Success() {
    // given
    String password = member.getPassword();
    String hashPassword = PasswordUtil.hashPassword(password);
    member.setPassword(hashPassword);

    when(jwtUtil.getMemberFromEmail(anyString())).thenReturn(member);
    when(memberRepository.existsByEmail(anyString())).thenReturn(false);

    MemberUpdateDto memberUpdateDto = MemberUpdateDto.builder()
        .currentPassword(password)
        .email("new@Email.com")
        .build();

    // when
    memberService.updateMember("token", memberUpdateDto);
    // then
    verify(memberRepository).save(any());
    verify(mailService).sendEmail(memberUpdateDto.getEmail());
  }

  @Test
  void updateMember_Failure_WrongPassword() {
    // given
    when(jwtUtil.getMemberFromEmail(anyString())).thenReturn(member);

    MemberUpdateDto memberUpdateDto = MemberUpdateDto.builder()
        .currentPassword("wrongPassword")
        .build();
    // when
    CustomException e = assertThrows(CustomException.class,
        () -> memberService.updateMember("token", memberUpdateDto));
    // then
    assertEquals(WRONG_PASSWORD, e.getErrorCode());
  }

  @Test
  void deleteMember_Success() {
    // given
    String password = member.getPassword();
    String hashPassword = PasswordUtil.hashPassword(password);
    member.setPassword(hashPassword);

    when(jwtUtil.getMemberFromEmail(anyString())).thenReturn(member);

    MemberDeleteDto deleteDto = MemberDeleteDto.builder()
        .confirmPassword(password)
        .build();
    // when
    memberService.deleteMember("token", deleteDto);
    // then
    verify(memberRepository).delete(any());
  }

  @Test
  void deleteMember_Failure_WrongPassword() {
    // given
    when(jwtUtil.getMemberFromEmail(anyString())).thenReturn(member);

    MemberDeleteDto memberDeleteDto = MemberDeleteDto.builder()
        .confirmPassword("wrongPassword")
        .build();
    // when
    CustomException e = assertThrows(CustomException.class,
        () -> memberService.deleteMember("token", memberDeleteDto));
    // then
    assertEquals(WRONG_PASSWORD, e.getErrorCode());
  }
}