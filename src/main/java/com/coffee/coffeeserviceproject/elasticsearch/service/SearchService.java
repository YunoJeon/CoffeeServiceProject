package com.coffee.coffeeserviceproject.elasticsearch.service;

import static com.coffee.coffeeserviceproject.common.type.ErrorCode.ROASTER_REGISTRATION_FAILED;

import com.coffee.coffeeserviceproject.bean.entity.Bean;
import com.coffee.coffeeserviceproject.bean.repository.BeanRepository;
import com.coffee.coffeeserviceproject.common.exception.CustomException;
import com.coffee.coffeeserviceproject.elasticsearch.document.SearchBeanList;
import com.coffee.coffeeserviceproject.elasticsearch.repository.SearchRepository;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SearchService {

  private final SearchRepository searchRepository;

  private final BeanRepository beanRepository;

  public Page<SearchBeanList> searchBeanList(String query, String role,
      String purchaseStatus, Pageable pageable) {

    Page<SearchBeanList> searchBeanListPage = searchRepository.searchByBeanNameOrRoasterName(query,
        pageable);

    List<SearchBeanList> filteredSearchBeanList = searchBeanListPage.stream()
        .filter(bean -> (role == null || Objects.equals(bean.getRole(), role)) &&
            (purchaseStatus == null || Objects.equals(bean.getPurchaseStatus(), purchaseStatus)))
        .toList();

    return new PageImpl<>(filteredSearchBeanList, pageable, searchBeanListPage.getTotalElements());
  }

  @Async
  @Transactional
  public void updateSearchDataForRoaster(String roasterName, Long memberId) {

    List<Bean> beanList = beanRepository.findAllByMemberId(memberId);

    for (Bean bean : beanList) {
      SearchBeanList searchBeanList = findByBeanIdFromSearchRepository(bean.getId());

      searchBeanList.setRoasterName(roasterName);
      searchRepository.save(searchBeanList);
    }
  }

  private SearchBeanList findByBeanIdFromSearchRepository(Long id) {

    return searchRepository.findById(id)
        .orElseThrow(() -> new CustomException(ROASTER_REGISTRATION_FAILED));
  }
}