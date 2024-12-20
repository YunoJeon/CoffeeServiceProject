package com.coffee.coffeeserviceproject.bean.service;

import static com.coffee.coffeeserviceproject.bean.type.PurchaseStatus.POSSIBLE;
import static com.coffee.coffeeserviceproject.common.type.ErrorCode.NOT_FOUND_BEAN;
import static com.coffee.coffeeserviceproject.common.type.ErrorCode.NOT_PERMISSION;
import static com.coffee.coffeeserviceproject.common.type.ErrorCode.PRICE_REQUIRED;
import static com.coffee.coffeeserviceproject.common.type.ErrorCode.PURCHASE_STATUS_REQUIRED;
import static com.coffee.coffeeserviceproject.member.type.RoleType.SELLER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.coffee.coffeeserviceproject.bean.dto.BeanDto;
import com.coffee.coffeeserviceproject.bean.dto.BeanListDto;
import com.coffee.coffeeserviceproject.bean.dto.BeanUpdateDto;
import com.coffee.coffeeserviceproject.bean.entity.Bean;
import com.coffee.coffeeserviceproject.bean.repository.BeanRepository;
import com.coffee.coffeeserviceproject.common.exception.CustomException;
import com.coffee.coffeeserviceproject.configuration.JwtProvider;
import com.coffee.coffeeserviceproject.elasticsearch.document.SearchBeanList;
import com.coffee.coffeeserviceproject.elasticsearch.repository.SearchRepository;
import com.coffee.coffeeserviceproject.favorite.repository.FavoriteRepository;
import com.coffee.coffeeserviceproject.member.entity.Member;
import com.coffee.coffeeserviceproject.review.repository.ReviewRepository;
import com.coffee.coffeeserviceproject.review.service.ReviewService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class BeanServiceTest {

  @Mock
  private BeanRepository beanRepository;

  @Mock
  private JwtProvider jwtProvider;

  @Mock
  private SearchRepository searchRepository;

  @Mock
  private ReviewRepository reviewRepository;

  @Mock
  private ReviewService reviewService;

  @Mock
  private FavoriteRepository favoriteRepository;

  @Mock
  private RedisTemplate<String, String> redisTemplateForCount;

  @Mock
  private ValueOperations<String, String> valueOperations;

  @Mock
  private HttpServletRequest request;

  @InjectMocks
  private BeanService beanService;

  private BeanDto beanDto;
  private Member member;

  @BeforeEach
  void setUp() {
    member = new Member();
    member.setId(1L);
    member.setRole(SELLER);
    member.setMemberName("홍길동");
    beanDto = BeanDto.builder()
        .beanName("테스트원두")
        .beanState("나라")
        .beanVariety("품종")
        .altitude("고도")
        .process("가공방식")
        .roastingLevel("로스팅레벨")
        .cupNote("컵노트")
        .price(1000L)
        .purchaseStatus(POSSIBLE)
        .build();
  }

  @Test
  void addBean_Success() {
    // given
    when(jwtProvider.getMemberFromEmail(anyString())).thenReturn(member);
    when(beanRepository.save(any())).thenReturn(new Bean());
    // when
    beanService.addBean(beanDto, "token");
    // then
    verify(beanRepository).save(any());
  }

  @Test
  void addBean_Failure_PriceRequired() {
    // given
    beanDto.setPrice(null);
    when(jwtProvider.getMemberFromEmail(anyString())).thenReturn(member);
    // when
    CustomException e = assertThrows(CustomException.class,
        () -> beanService.addBean(beanDto, "token"));
    // then
    assertEquals(PRICE_REQUIRED, e.getErrorCode());
  }

  @Test
  void addBean_Failure_PurchaseStatusRequired() {
    // given
    beanDto.setPurchaseStatus(null);
    when(jwtProvider.getMemberFromEmail(anyString())).thenReturn(member);
    // when
    CustomException e = assertThrows(CustomException.class,
        () -> beanService.addBean(beanDto, "token"));
    // then
    assertEquals(PURCHASE_STATUS_REQUIRED, e.getErrorCode());
  }

  @Test
  void getBeanList_Success() {
    // given
    Pageable pageable = Pageable.ofSize(10);

    Bean bean = new Bean();
    bean.setId(1L);
    bean.setMember(member);

    Page<Bean> beanList = new PageImpl<>(List.of(bean));

    when(beanRepository.findAllByOrderByIdDesc(pageable)).thenReturn(beanList);
    // when
    Page<BeanListDto> beanListDtoPage = beanService.getBeanList(pageable, null, null);
    // then
    assertNull(beanListDtoPage.getContent().get(0).getRoasterName());
    assertNotNull(beanListDtoPage);
    assertEquals(1, beanListDtoPage.getTotalElements());
  }

  @Test
  void getBean_Success() {
    // given
    Bean bean = new Bean();
    bean.setId(1L);
    bean.setMember(member);

    String token = "token";

    when(jwtProvider.getMemberFromEmail(token)).thenReturn(member);
    when(beanRepository.findById(anyLong())).thenReturn(Optional.of(bean));
    when(redisTemplateForCount.hasKey(anyString())).thenReturn(false);
    when(redisTemplateForCount.opsForValue()).thenReturn(valueOperations);
    // when
    BeanDto dto = beanService.getBean(1L, token, request);
    // then
    assertNotNull(dto);
  }

  @Test
  void getBean_Failure_NotFoundBean() {
    // given
    when(beanRepository.findById(anyLong())).thenReturn(Optional.empty());
    String token = "token";
    // when
    CustomException e = assertThrows(CustomException.class,
        () -> beanService.getBean(1L, token, request));
    // then
    assertEquals(NOT_FOUND_BEAN, e.getErrorCode());
  }

  @Test
  void updateBean_Success() {
    // given
    Bean bean = new Bean();
    bean.setId(1L);
    bean.setMember(member);

    SearchBeanList searchBeanList = SearchBeanList.builder().beanId(bean.getId()).build();

    when(beanRepository.findById(anyLong())).thenReturn(Optional.of(bean));
    when(jwtProvider.getMemberFromEmail(anyString())).thenReturn(member);
    when(searchRepository.findById(bean.getId())).thenReturn(Optional.of(searchBeanList));

    BeanUpdateDto updateDto = BeanUpdateDto.builder()
        .beanName("수정 원두").build();
    // when
    beanService.updateBean(1L, updateDto, "token");
    // then
    verify(beanRepository).save(bean);
    verify(searchRepository).save(searchBeanList);
    assertEquals("수정 원두", bean.getBeanName());
  }

  @Test
  void updateBean_Failure_NotPermission() {
    // given
    Member otherMember = new Member();
    otherMember.setId(2L);

    Bean bean = new Bean();
    bean.setId(1L);
    bean.setMember(member);

    SearchBeanList searchBeanList = SearchBeanList.builder().beanId(bean.getId()).build();

    BeanUpdateDto updateDto = BeanUpdateDto.builder().build();

    when(beanRepository.findById(anyLong())).thenReturn(Optional.of(bean));
    when(jwtProvider.getMemberFromEmail(anyString())).thenReturn(otherMember);
    when(searchRepository.findById(bean.getId())).thenReturn(Optional.of(searchBeanList));
    // when
    CustomException e = assertThrows(CustomException.class,
        () -> beanService.updateBean(1L, updateDto, "token"));
    // then
    assertEquals(NOT_PERMISSION, e.getErrorCode());
  }

  @Test
  void deleteBean_Success() {
    // given
    Bean bean = new Bean();
    bean.setId(1L);
    bean.setMember(member);

    SearchBeanList searchBeanList = SearchBeanList.builder().beanId(bean.getId()).build();

    when(beanRepository.findById(anyLong())).thenReturn(Optional.of(bean));
    when(jwtProvider.getMemberFromEmail(anyString())).thenReturn(member);
    when(searchRepository.findById(bean.getId())).thenReturn(Optional.of(searchBeanList));
    // when
    beanService.deleteBean(1L, "token");
    // then
    verify(beanRepository).deleteById(bean.getId());
    verify(searchRepository).deleteById(searchBeanList.getBeanId());
    verify(reviewRepository).deleteAllByBeanId(bean.getId());
    verify(reviewService).updateAverageScore(bean.getId());
    verify(favoriteRepository).deleteAllByBeanId(bean.getId());
  }

  @Test
  void deleteBean_Failure_NotPermission() {
    // given
    Member otherMember = new Member();
    otherMember.setId(2L);

    Bean bean = new Bean();
    bean.setId(1L);
    bean.setMember(member);

    SearchBeanList searchBeanList = SearchBeanList.builder().beanId(bean.getId()).build();

    when(beanRepository.findById(anyLong())).thenReturn(Optional.of(bean));
    when(jwtProvider.getMemberFromEmail(anyString())).thenReturn(otherMember);
    when(searchRepository.findById(bean.getId())).thenReturn(Optional.of(searchBeanList));
    // when
    CustomException e = assertThrows(CustomException.class,
        () -> beanService.deleteBean(1L, "token"));
    // then
    assertEquals(NOT_PERMISSION, e.getErrorCode());
    verify(reviewRepository, never()).deleteAllByBeanId(bean.getId());
  }

  @Test
  void getBean_IncreasesViewCountForMember() {
    // given
    Bean bean = new Bean();
    bean.setId(1L);
    bean.setMember(member);

    String token = "token";

    when(jwtProvider.getMemberFromEmail(anyString())).thenReturn(member);
    when(beanRepository.findById(bean.getId())).thenReturn(Optional.of(bean));
    when(redisTemplateForCount.hasKey(anyString())).thenReturn(false);
    when(redisTemplateForCount.opsForValue()).thenReturn(valueOperations);
    // when
    beanService.getBean(bean.getId(), token, request);
    // then
    verify(valueOperations).increment(eq("bean:viewCount:1"), eq(1L));
  }

  @Test
  void getBean_IncreasesViewCountForGuest() {
    // given
    Bean bean = new Bean();
    bean.setId(1L);
    bean.setMember(member);

    when(beanRepository.findById(bean.getId())).thenReturn(Optional.of(bean));
    when(request.getHeader("X-Forwarded-For")).thenReturn("127.0.0.1");
    when(redisTemplateForCount.hasKey(anyString())).thenReturn(false);
    when(redisTemplateForCount.opsForValue()).thenReturn(valueOperations);
    // when
    beanService.getBean(bean.getId(), null, request);
    // then
    verify(valueOperations).increment(eq("bean:viewCount:1"), eq(1L));
  }

  @Test
  void getIpAddress_IpAvailable() {
    // given
    Bean bean = new Bean();
    bean.setId(1L);
    bean.setMember(member);

    when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.0.1");
    when(beanRepository.findById(bean.getId())).thenReturn(Optional.of(bean));
    when(redisTemplateForCount.opsForValue()).thenReturn(valueOperations);
    // when
    beanService.getBean(bean.getId(), null, request);
    // then
    verify(redisTemplateForCount).hasKey("bean:viewed:guest:192.168.0.1:1");
  }

  @Test
  void getIpAddress_IpUnavailable() {
    // given
    Bean bean = new Bean();
    bean.setId(1L);
    bean.setMember(member);

    when(request.getHeader("X-Forwarded-For")).thenReturn(null);
    when(beanRepository.findById(bean.getId())).thenReturn(Optional.of(bean));
    when(redisTemplateForCount.opsForValue()).thenReturn(valueOperations);
    // when
    beanService.getBean(bean.getId(), null, request);
    // then
    verify(redisTemplateForCount).hasKey("bean:viewed:guest:unknown:1");
  }

  @Test
  void getBean_DoesNotIncreaseViewCountAlreadyViewed() {
    // given
    Bean bean = new Bean();
    bean.setId(1L);
    bean.setMember(member);

    String token = "token";

    when(jwtProvider.getMemberFromEmail(token)).thenReturn(member);
    when(beanRepository.findById(bean.getId())).thenReturn(Optional.of(bean));
    when(redisTemplateForCount.hasKey(anyString())).thenReturn(true);
    // when
    beanService.getBean(bean.getId(), token, request);
    // then
    verify(valueOperations, never()).increment(anyString(), anyLong());
  }

  @Test
  void saveDailyViewCountsToDB_UpdateDB() {
    // given
    Bean bean = new Bean();
    bean.setId(1L);
    bean.setMember(member);
    bean.setViewCount(100L);

    SearchBeanList searchBeanList = SearchBeanList.builder().beanId(bean.getId()).build();

    String redisKey = "bean:viewCount:1";

    when(redisTemplateForCount.opsForValue()).thenReturn(valueOperations);
    when(redisTemplateForCount.keys("bean:viewCount:*")).thenReturn(Set.of(redisKey));
    when(redisTemplateForCount.opsForValue().get(redisKey)).thenReturn("10");
    when(beanRepository.findById(bean.getId())).thenReturn(Optional.of(bean));
    when(searchRepository.findById(bean.getId())).thenReturn(Optional.of(searchBeanList));
    // when
    beanService.saveDailyViewCountsToDB();
    // then
    verify(beanRepository).save(bean);
    assertEquals(110, bean.getViewCount());
  }
}