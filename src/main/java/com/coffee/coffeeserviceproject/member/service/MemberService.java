package com.coffee.coffeeserviceproject.member.service;

import static com.coffee.coffeeserviceproject.common.type.ErrorCode.ALREADY_EXISTS_USER;
import static com.coffee.coffeeserviceproject.common.type.ErrorCode.LOGIN_ERROR;
import static com.coffee.coffeeserviceproject.common.type.ErrorCode.WRONG_PASSWORD;
import static com.coffee.coffeeserviceproject.member.type.RoleType.SELLER;

import com.coffee.coffeeserviceproject.bean.entity.Bean;
import com.coffee.coffeeserviceproject.bean.repository.BeanRepository;
import com.coffee.coffeeserviceproject.common.exception.CustomException;
import com.coffee.coffeeserviceproject.configuration.JwtProvider;
import com.coffee.coffeeserviceproject.elasticsearch.repository.SearchRepository;
import com.coffee.coffeeserviceproject.favorite.repository.FavoriteRepository;
import com.coffee.coffeeserviceproject.member.dto.MemberDeleteDto;
import com.coffee.coffeeserviceproject.member.dto.MemberDto;
import com.coffee.coffeeserviceproject.member.dto.MemberUpdateDto;
import com.coffee.coffeeserviceproject.member.dto.RoasterDto;
import com.coffee.coffeeserviceproject.member.entity.Member;
import com.coffee.coffeeserviceproject.member.entity.Roaster;
import com.coffee.coffeeserviceproject.member.repository.MemberRepository;
import com.coffee.coffeeserviceproject.member.repository.RoasterRepository;
import com.coffee.coffeeserviceproject.review.repository.ReviewRepository;
import com.coffee.coffeeserviceproject.review.service.ReviewService;
import com.coffee.coffeeserviceproject.util.PasswordUtil;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

  private final MemberRepository memberRepository;

  private final MailService mailService;

  private final JwtProvider jwtProvider;

  private final BeanRepository beanRepository;

  private final SearchRepository searchRepository;

  private final RoasterRepository roasterRepository;

  private final ReviewRepository reviewRepository;

  private final ReviewService reviewService;

  private final FavoriteRepository favoriteRepository;

  @Transactional
  public void addMember(MemberDto memberDto) {

    if (memberRepository.findByEmail(memberDto.getEmail()).isPresent()) {
      throw new CustomException(ALREADY_EXISTS_USER);
    }

    String encodePassword = PasswordUtil.hashPassword(memberDto.getPassword());

    Member member = Member.fromDto(memberDto, encodePassword);

    memberRepository.save(member);

    mailService.sendEmail(memberDto.getEmail());
  }

  @Transactional
  public String login(String email, String password) {

    Member member = memberRepository.findByEmail(email).orElse(null);

    if (member == null || !PasswordUtil.matches(password, member.getPassword())
        || member.getCertificationAt() == null) {
      throw new CustomException(LOGIN_ERROR);
    }

    return jwtProvider.generateToken(email);
  }

  @Transactional(readOnly = true)
  public MemberDto getMember(String token) {

    Member member = getMemberByEmail(token);

    MemberDto memberDto = MemberDto.fromEntity(member);

    if (member.getRole() == SELLER) {

      Roaster roaster = member.getRoaster();

      if (roaster != null) {

        RoasterDto roasterDto = RoasterDto.fromEntity(roaster);

        memberDto.setRoasterDto(roasterDto);
      }
    }

    return memberDto;
  }

  @Transactional
  public void updateMember(String token, MemberUpdateDto memberUpdateDto) {

    Member member = getMemberByEmail(token);

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

  @Transactional
  public void deleteMember(String token, MemberDeleteDto memberDeleteDto) {

    Member member = getMemberByEmail(token);

    Long memberId = member.getId();

    if (!PasswordUtil.matches(memberDeleteDto.getConfirmPassword(), member.getPassword())) {
      throw new CustomException(WRONG_PASSWORD);
    }

    if (member.getRole() == SELLER) {
      roasterRepository.delete(member.getRoaster());
    }

    List<Bean> beanList = beanRepository.findAllByMemberId(member.getId());

    if (!beanList.isEmpty()) {

      beanRepository.deleteAll(beanList);

      List<Long> beanIds = beanList.stream()
          .map(Bean::getId)
          .collect(Collectors.toList());

      deleteBeanDataAsync(beanIds);
    }

    deleteMemberDataAsync(memberId);
  }

  @Async
  @Transactional
  public void deleteBeanDataAsync(List<Long> beanIds) {

    final int BATCH_SIZE = 100;

    for (int i = 0; i < beanIds.size(); i+= BATCH_SIZE) {

      List<Long> batchBeanIds = beanIds.subList(i, Math.min(i + BATCH_SIZE, beanIds.size()));

      for (Long beanId : batchBeanIds) {

        reviewService.updateAverageScore(beanId);
      }

      searchRepository.deleteAllByBeanIdIn(batchBeanIds);
    }
  }

  @Async
  @Transactional
  public void deleteMemberDataAsync(Long memberId) {

    reviewRepository.deleteAllByMemberId(memberId);
    favoriteRepository.deleteAllByMemberId(memberId);
    memberRepository.deleteById(memberId);
  }

  private Member getMemberByEmail(String token) {

    return jwtProvider.getMemberFromEmail(token);
  }
}