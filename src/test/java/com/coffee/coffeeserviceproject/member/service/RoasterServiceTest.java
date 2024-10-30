package com.coffee.coffeeserviceproject.member.service;

import static com.coffee.coffeeserviceproject.common.type.ErrorCode.ALREADY_REGISTERED_ROASTER;
import static com.coffee.coffeeserviceproject.common.type.ErrorCode.WRONG_PASSWORD;
import static com.coffee.coffeeserviceproject.member.type.RoleType.BUYER;
import static com.coffee.coffeeserviceproject.member.type.RoleType.SELLER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.coffee.coffeeserviceproject.bean.repository.BeanRepository;
import com.coffee.coffeeserviceproject.common.exception.CustomException;
import com.coffee.coffeeserviceproject.member.dto.RoasterDto;
import com.coffee.coffeeserviceproject.member.dto.RoasterUpdateDto;
import com.coffee.coffeeserviceproject.member.entity.Member;
import com.coffee.coffeeserviceproject.member.entity.Roaster;
import com.coffee.coffeeserviceproject.member.repository.MemberRepository;
import com.coffee.coffeeserviceproject.member.repository.RoasterRepository;
import com.coffee.coffeeserviceproject.configuration.JwtProvider;
import com.coffee.coffeeserviceproject.util.PasswordUtil;
import java.util.ArrayList;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RoasterServiceTest {

  @InjectMocks
  private RoasterService roasterService;

  @Mock
  private MemberRepository memberRepository;

  @Mock
  private RoasterRepository roasterRepository;

  @Mock
  private BeanRepository beanRepository;

  @Mock
  private JwtProvider jwtProvider;

  private Member member;

  private Roaster roaster;

  @BeforeEach
  void setUp() {
    member = Member.builder()
        .id(1L)
        .email("test@mail.com")
        .password("123456789")
        .phone("010-1234-5678")
        .role(BUYER)
        .build();

    roaster = Roaster.builder()
        .roasterName("로스터명")
        .officeAddress("매장주소")
        .member(member)
        .build();
  }

  @Test
  void addRoaster_Success() {
    // given
    RoasterDto roasterDto = RoasterDto.builder()
        .roasterName("로스터명1")
        .officeAddress("우리의주소")
        .build();

    when(jwtProvider.getMemberFromEmail(anyString())).thenReturn(member);
    when(roasterRepository.findByRoasterName(anyString())).thenReturn(Optional.empty());
    when(beanRepository.findByMemberId(anyLong())).thenReturn(new ArrayList<>());
    // when
    roasterService.addRoaster("token", roasterDto);

    ArgumentCaptor<Roaster> roasterArgumentCaptor = ArgumentCaptor.forClass(Roaster.class);
    verify(roasterRepository).save(roasterArgumentCaptor.capture());
    Roaster roaster = roasterArgumentCaptor.getValue();
    // then
    assertEquals("로스터명1", roaster.getRoasterName());
    assertEquals("우리의주소", roaster.getOfficeAddress());
    assertEquals(member, roaster.getMember());
    verify(memberRepository).save(member);
    verify(roasterRepository).save(roaster);
  }

  @Test
  void addRoaster_Failure_AlreadyRegistered() {
    // given
    member.setRole(SELLER);

    RoasterDto roasterDto = RoasterDto.builder().build();

    when(jwtProvider.getMemberFromEmail(anyString())).thenReturn(member);
    // when
    CustomException e = assertThrows(CustomException.class,
        () -> roasterService.addRoaster("token", roasterDto));
    // then
    verify(memberRepository, never()).save(member);
    verify(roasterRepository, never()).save(roaster);
    assertEquals(ALREADY_REGISTERED_ROASTER, e.getErrorCode());
  }

  @Test
  void addRoaster_Failure_AlreadySameRoasterName() {
    // given
    RoasterDto roasterDto = RoasterDto.builder()
        .roasterName("로스터명")
        .build();

    when(jwtProvider.getMemberFromEmail(anyString())).thenReturn(member);
    when(roasterRepository.findByRoasterName(roasterDto.getRoasterName())).thenReturn(
        Optional.of(roaster));
    // when
    CustomException e = assertThrows(CustomException.class,
        () -> roasterService.addRoaster("token", roasterDto));
    // then
    verify(memberRepository, never()).save(member);
    verify(roasterRepository, never()).save(roaster);
    assertEquals(ALREADY_REGISTERED_ROASTER, e.getErrorCode());
  }

  @Test
  void updateRoaster_Success() {
    // given
    String password = member.getPassword();
    String hashedPassword = PasswordUtil.hashPassword(password);
    member.setPassword(hashedPassword);

    RoasterUpdateDto roasterUpdateDto = RoasterUpdateDto.builder()
        .roasterName("새 로스터명")
        .password(password)
        .build();

    when(jwtProvider.getMemberFromEmail(anyString())).thenReturn(member);
    when(roasterRepository.findByMemberId(anyLong())).thenReturn(roaster);
    // when
    roasterService.updateRoaster("token", roasterUpdateDto);
    // then
    verify(roasterRepository).save(roaster);
    assertEquals(roaster.getRoasterName(), roasterUpdateDto.getRoasterName());
  }

  @Test
  void updateRoaster_Failure_WrongPassword() {
    // given
    RoasterUpdateDto roasterUpdateDto = RoasterUpdateDto.builder()
        .password("wrong password")
        .build();

    when(jwtProvider.getMemberFromEmail(anyString())).thenReturn(member);
    // when
    CustomException e = assertThrows(CustomException.class,
        () -> roasterService.updateRoaster("token", roasterUpdateDto));
    // then
    verify(roasterRepository, never()).save(roaster);
    assertEquals(WRONG_PASSWORD, e.getErrorCode());
  }
}