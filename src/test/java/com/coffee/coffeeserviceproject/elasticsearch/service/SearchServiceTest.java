package com.coffee.coffeeserviceproject.elasticsearch.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.coffee.coffeeserviceproject.elasticsearch.document.SearchBeanList;
import com.coffee.coffeeserviceproject.elasticsearch.repository.SearchRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

  @Mock
  private SearchRepository searchRepository;

  @InjectMocks
  private SearchService searchService;

  @Test
  void searchBeanList_Success() {
    // given
    String query = "test";
    Pageable pageable = Pageable.ofSize(10);
    List<SearchBeanList> searchBeanList = List.of(SearchBeanList.builder().build());
    Page<SearchBeanList> searchBeanListPage = new PageImpl<>(searchBeanList);
    when(searchRepository.searchByBeanNameOrRoasterName(query, pageable)).thenReturn(
        searchBeanListPage);
    // when
    Page<SearchBeanList> result = searchService.searchBeanList(query, null, null, pageable);
    // then
    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
  }

  @Test
  void searchBeanList_Success_Empty() {
    // given
    String query = "";
    Pageable pageable = Pageable.ofSize(10);
    Page<SearchBeanList> searchBeanListPage = new PageImpl<>(List.of());
    when(searchRepository.searchByBeanNameOrRoasterName(query, pageable)).thenReturn(
        searchBeanListPage);
    // when
    Page<SearchBeanList> result = searchService.searchBeanList(query, null,
        null, pageable);
    // then
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }
}