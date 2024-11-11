package com.coffee.coffeeserviceproject.bean.service;

import static com.coffee.coffeeserviceproject.bean.type.PurchaseStatus.POSSIBLE;
import static com.coffee.coffeeserviceproject.common.type.ErrorCode.NOT_FOUND_BEAN;
import static com.coffee.coffeeserviceproject.common.type.ErrorCode.NOT_PERMISSION;
import static com.coffee.coffeeserviceproject.common.type.ErrorCode.PRICE_REQUIRED;
import static com.coffee.coffeeserviceproject.common.type.ErrorCode.PURCHASE_STATUS_REQUIRED;
import static com.coffee.coffeeserviceproject.member.type.RoleType.SELLER;

import com.coffee.coffeeserviceproject.bean.dto.BeanDto;
import com.coffee.coffeeserviceproject.bean.dto.BeanListDto;
import com.coffee.coffeeserviceproject.bean.dto.BeanUpdateDto;
import com.coffee.coffeeserviceproject.bean.entity.Bean;
import com.coffee.coffeeserviceproject.bean.repository.BeanRepository;
import com.coffee.coffeeserviceproject.bean.type.PurchaseStatus;
import com.coffee.coffeeserviceproject.common.exception.CustomException;
import com.coffee.coffeeserviceproject.configuration.JwtProvider;
import com.coffee.coffeeserviceproject.elasticsearch.document.SearchBeanList;
import com.coffee.coffeeserviceproject.elasticsearch.repository.SearchRepository;
import com.coffee.coffeeserviceproject.favorite.repository.FavoriteRepository;
import com.coffee.coffeeserviceproject.member.entity.Member;
import com.coffee.coffeeserviceproject.member.type.RoleType;
import com.coffee.coffeeserviceproject.review.repository.ReviewRepository;
import com.coffee.coffeeserviceproject.review.service.ReviewService;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class BeanService {

  private final BeanRepository beanRepository;

  private final JwtProvider jwtProvider;

  private final SearchRepository searchRepository;

  private final ReviewRepository reviewRepository;

  private final ReviewService reviewService;

  private final FavoriteRepository favoriteRepository;

  private final RedisTemplate<String, String> redisTemplateForCount;

  private static final String BEAN_VIEWED_KEY_PREFIX = "bean:viewed:";

  private static final String BEAN_VIEW_COUNT_KEY_PREFIX = "bean:viewCount:";

  private static final String VIEWED_VALUE = "viewed";

  private static final String GUEST = "guest:";

  private static final String HEADER_X_FORWARDED_FOR = "X-Forwarded-For";

  private static final String UNKNOWN_IP = "unknown";

  @Transactional
  public void addBean(BeanDto beanDto, String token) {

    Member member = getMemberFromToken(token);

    Bean bean = Bean.fromDto(member, beanDto);

    if (member.getRole() == SELLER) {
      if (beanDto.getPurchaseStatus() == null || beanDto.getPurchaseStatus().toString().isEmpty()) {
        throw new CustomException(PURCHASE_STATUS_REQUIRED);
      }

      if (beanDto.getPrice() == null) {
        throw new CustomException(PRICE_REQUIRED);
      }

      bean.setPurchaseStatus(beanDto.getPurchaseStatus());
      bean.setPrice(beanDto.getPrice());
    }

    Bean save = beanRepository.save(bean);

    String roasterName = null;
    String purchaseStatus = null;
    String role = null;

    if (member.getRoaster() != null) {
      roasterName = member.getRoaster().getRoasterName();
      role = member.getRole().name();
      purchaseStatus = save.getPurchaseStatus().name();
    }

    SearchBeanList searchBeanList = SearchBeanList.fromBeanEntity(
        save, roasterName, purchaseStatus, role);

    searchRepository.save(searchBeanList);
  }

  @Transactional(readOnly = true)
  public Page<BeanListDto> getBeanList(Pageable pageable, RoleType role,
      PurchaseStatus purchaseStatus) {

    Page<Bean> beanList;

    if (role == null && purchaseStatus == null) {
      beanList = beanRepository.findAllByOrderByIdDesc(pageable);
    } else if (role == null) {
      beanList = beanRepository.findByPurchaseStatus(POSSIBLE, pageable);
    } else if (purchaseStatus == null) {
      beanList = beanRepository.findByMemberRole(SELLER, pageable);
    } else {
      beanList = beanRepository.findByMemberRoleAndPurchaseStatus(SELLER, POSSIBLE, pageable);
    }

    return beanList.map(BeanListDto::fromEntity);
  }

  @Transactional(readOnly = true)
  public BeanDto getBean(Long id, String token, HttpServletRequest request) {

    Bean bean = findByBeanIdFromBeanRepository(id);

    String cacheKey = generateCacheKey(id, token, request);

    if (isFirstTimeToDay(cacheKey)) {

      incrementViewCount(id);

      setViewedToday(cacheKey);
    }

    return BeanDto.fromEntity(bean);
  }

  private String generateCacheKey(Long beanId, String token, HttpServletRequest request) {

    if (StringUtils.hasText(token)) {

      Member member = getMemberFromToken(token);

      return BEAN_VIEWED_KEY_PREFIX + member.getId() + ":" + beanId;

    } else {

      String ipAddress = getIpAddress(request);

      return BEAN_VIEWED_KEY_PREFIX + GUEST + ipAddress + ":" + beanId;
    }
  }

  private String getIpAddress(HttpServletRequest request) {

    String ipAddress = request.getHeader(HEADER_X_FORWARDED_FOR);

    if (ipAddress == null || ipAddress.isEmpty() || UNKNOWN_IP.equalsIgnoreCase(ipAddress)) {

      ipAddress = request.getRemoteAddr();
    }

    if (ipAddress == null || ipAddress.isEmpty()) {

      ipAddress = UNKNOWN_IP;
    } else {

      ipAddress = ipAddress.split(",")[0].trim();
    }
    return ipAddress;
  }

  private boolean isFirstTimeToDay(String cacheKey) {

    return Boolean.FALSE.equals(redisTemplateForCount.hasKey(cacheKey));
  }

  private void incrementViewCount(Long beanId) {

    String viewCountKey = BEAN_VIEW_COUNT_KEY_PREFIX + beanId;

    redisTemplateForCount.opsForValue().increment(viewCountKey, 1);

    long timeUntilMidnight = getTimeUntilMidnight();

    redisTemplateForCount.expire(viewCountKey, timeUntilMidnight, TimeUnit.MILLISECONDS);
  }

  private void setViewedToday(String cacheKey) {

    long timeUntilMidnight = getTimeUntilMidnight();

    redisTemplateForCount.opsForValue()
        .set(cacheKey, VIEWED_VALUE, timeUntilMidnight, TimeUnit.MILLISECONDS);
  }

  private long getTimeUntilMidnight() {

    LocalDateTime now = LocalDateTime.now();

    LocalDateTime midnight = now.toLocalDate().atStartOfDay().plusDays(1);

    return Duration.between(now, midnight).toMillis();
  }

  @Scheduled(cron = "0 0 0 * * *")
  @Transactional
  @Retryable(maxAttempts = 5, backoff = @Backoff(delay = 2000))
  public void saveDailyViewCountsToDB() {

    Set<String> keys = redisTemplateForCount.keys(BEAN_VIEW_COUNT_KEY_PREFIX + "*");

    if (keys != null) {

      for (String key : keys) {

        Long beanId = Long.valueOf(key.split(":")[2]);

        String dailyViewCountStr = redisTemplateForCount.opsForValue().get(key);

        Integer dailyViewCount =
            (dailyViewCountStr != null) ? Integer.parseInt(dailyViewCountStr) : 0;

        Bean bean = findByBeanIdFromBeanRepository(beanId);

        SearchBeanList searchBeanList = findByBeanIdFromSearchRepository(beanId);

        long totalViewCount = bean.getViewCount() + dailyViewCount;

        bean.setViewCount(totalViewCount);

        searchBeanList.setViewCount(totalViewCount);

        beanRepository.save(bean);

        searchRepository.save(searchBeanList);
      }
    }
  }

  @Transactional
  public void updateBean(Long id, BeanUpdateDto beanUpdateDto, String token) {

    Bean bean = findByBeanIdFromBeanRepository(id);

    SearchBeanList searchBeanList = findByBeanIdFromSearchRepository(id);

    Member member = getMemberFromToken(token);

    if (!bean.getMember().getId().equals(member.getId())) {
      throw new CustomException(NOT_PERMISSION);
    }

    if (beanUpdateDto.getBeanName() != null) {
      bean.setBeanName(beanUpdateDto.getBeanName());
      searchBeanList.setBeanName(beanUpdateDto.getBeanName());
    }

    if (beanUpdateDto.getBeanState() != null) {
      bean.setBeanState(beanUpdateDto.getBeanState());
    }

    if (beanUpdateDto.getBeanRegion() != null) {
      bean.setBeanRegion(beanUpdateDto.getBeanRegion());
    }

    if (beanUpdateDto.getBeanFarm() != null) {
      bean.setBeanFarm(beanUpdateDto.getBeanFarm());
    }

    if (beanUpdateDto.getBeanVariety() != null) {
      bean.setBeanVariety(beanUpdateDto.getBeanVariety());
    }

    if (beanUpdateDto.getAltitude() != null) {
      bean.setAltitude(beanUpdateDto.getAltitude());
    }

    if (beanUpdateDto.getProcess() != null) {
      bean.setProcess(beanUpdateDto.getProcess());
    }

    if (beanUpdateDto.getGrade() != null) {
      bean.setGrade(beanUpdateDto.getGrade());
    }

    if (beanUpdateDto.getRoastingLevel() != null) {
      bean.setRoastingLevel(beanUpdateDto.getRoastingLevel());
    }

    if (beanUpdateDto.getRoastingDate() != null) {
      bean.setRoastingDate(beanUpdateDto.getRoastingDate());
    }

    if (beanUpdateDto.getCupNote() != null) {
      bean.setCupNote(beanUpdateDto.getCupNote());
    }

    if (beanUpdateDto.getEspressoRecipe() != null) {
      bean.setEspressoRecipe(beanUpdateDto.getEspressoRecipe());
    }

    if (beanUpdateDto.getFilterRecipe() != null) {
      bean.setFilterRecipe(beanUpdateDto.getFilterRecipe());
    }

    if (beanUpdateDto.getMilkPairing() != null) {
      bean.setMilkPairing(beanUpdateDto.getMilkPairing());
    }

    if (beanUpdateDto.getSignatureVariation() != null) {
      bean.setSignatureVariation(beanUpdateDto.getSignatureVariation());
    }

    if (bean.getMember().getRole() == SELLER) {
      if (beanUpdateDto.getPrice() != null) {
        bean.setPrice(beanUpdateDto.getPrice());
      }

      if (beanUpdateDto.getPurchaseStatus() != null) {
        bean.setPurchaseStatus(beanUpdateDto.getPurchaseStatus());
      }
    } else {
      if (beanUpdateDto.getPrice() != null || beanUpdateDto.getPurchaseStatus() != null) {
        throw new CustomException(NOT_PERMISSION);
      }
    }

    beanRepository.save(bean);
    searchRepository.save(searchBeanList);
  }

  @Transactional
  public void deleteBean(Long id, String token) {

    Bean bean = findByBeanIdFromBeanRepository(id);

    findByBeanIdFromSearchRepository(id);

    Member member = getMemberFromToken(token);

    if (!bean.getMember().getId().equals(member.getId())) {
      throw new CustomException(NOT_PERMISSION);
    }

    reviewService.updateAverageScore(id);

    deleteDataAsync(id);
  }

  @Async
  @Transactional
  public void deleteDataAsync(Long beanId) {

    searchRepository.deleteById(beanId);
    favoriteRepository.deleteAllByBeanId(beanId);
    reviewRepository.deleteAllByBeanId(beanId);
    beanRepository.deleteById(beanId);
  }

  private Member getMemberFromToken(String token) {

    return jwtProvider.getMemberFromEmail(token);
  }

  private Bean findByBeanIdFromBeanRepository(Long id) {

    return beanRepository.findById(id).orElseThrow(() -> new CustomException(NOT_FOUND_BEAN));
  }

  private SearchBeanList findByBeanIdFromSearchRepository(Long id) {

    return searchRepository.findById(id).orElseThrow(() -> new CustomException(NOT_FOUND_BEAN));
  }
}