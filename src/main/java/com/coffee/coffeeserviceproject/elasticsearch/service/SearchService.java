package com.coffee.coffeeserviceproject.elasticsearch.service;

import com.coffee.coffeeserviceproject.elasticsearch.document.SearchBeanList;
import com.coffee.coffeeserviceproject.elasticsearch.repository.SearchRepository;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchService {

  private final SearchRepository searchRepository;

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
}
