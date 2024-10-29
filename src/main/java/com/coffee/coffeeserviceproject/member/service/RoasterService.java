package com.coffee.coffeeserviceproject.member.service;

import static com.coffee.coffeeserviceproject.common.type.ErrorCode.ALREADY_REGISTERED_ROASTER;
import static com.coffee.coffeeserviceproject.common.type.ErrorCode.WRONG_PASSWORD;
import static com.coffee.coffeeserviceproject.member.type.RoleType.SELLER;

import com.coffee.coffeeserviceproject.common.exception.CustomException;
import com.coffee.coffeeserviceproject.member.dto.RoasterDto;
import com.coffee.coffeeserviceproject.member.dto.RoasterUpdateDto;
import com.coffee.coffeeserviceproject.member.entity.Member;
import com.coffee.coffeeserviceproject.member.entity.Roaster;
import com.coffee.coffeeserviceproject.member.repository.MemberRepository;
import com.coffee.coffeeserviceproject.member.repository.RoasterRepository;
import com.coffee.coffeeserviceproject.util.JwtUtil;
import com.coffee.coffeeserviceproject.util.PasswordUtil;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoasterService {

  private final MemberRepository memberRepository;

  private final RoasterRepository roasterRepository;

  private final JwtUtil jwtUtil;

  public void addRoaster(String token, RoasterDto roasterDto) {

    Member member = jwtUtil.getMemberFromEmail(token);

    if (member.getRole() == SELLER) {
      throw new CustomException(ALREADY_REGISTERED_ROASTER);
    }

    if (roasterRepository.findByRoasterName(roasterDto.getRoasterName()).isPresent()) {
      throw new CustomException(ALREADY_REGISTERED_ROASTER);
    }

    Roaster roaster = Roaster.builder()
        .roasterName(roasterDto.getRoasterName())
        .officeAddress(roasterDto.getOfficeAddress())
        .contactInfo(
            roasterDto.getContactInfo() == null || roasterDto.getContactInfo().isEmpty()
                ? member.getPhone() : roasterDto.getContactInfo())
        .description(roasterDto.getDescription())
        .member(member)
        .build();

    member.setRole(SELLER);
    member.setUpdatedAt(LocalDateTime.now());

    memberRepository.save(member);
    roasterRepository.save(roaster);
  }

  public void updateRoaster(String token, RoasterUpdateDto roasterUpdateDto) {

    Member member = jwtUtil.getMemberFromEmail(token);

    if (!PasswordUtil.matches(roasterUpdateDto.getPassword(), member.getPassword())) {
      throw new CustomException(WRONG_PASSWORD);
    }

    Long memberId = member.getId();

    Roaster roaster = roasterRepository.findByMemberId(memberId);

    if (roasterUpdateDto.getRoasterName() != null) {
      roaster.setRoasterName(roasterUpdateDto.getRoasterName());
    }

    if (roasterUpdateDto.getOfficeAddress() != null) {
      roaster.setOfficeAddress(roasterUpdateDto.getOfficeAddress());
    }

    if (roasterUpdateDto.getContactInfo() != null) {
      roaster.setContactInfo(roasterUpdateDto.getContactInfo());
    }

    if (roasterUpdateDto.getDescription() != null) {
      roaster.setDescription(roasterUpdateDto.getDescription());
    }

    roasterRepository.save(roaster);
  }
}