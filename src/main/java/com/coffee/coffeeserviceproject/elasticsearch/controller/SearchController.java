package com.coffee.coffeeserviceproject.elasticsearch.controller;

import com.coffee.coffeeserviceproject.common.model.ListResponseDto;
import com.coffee.coffeeserviceproject.elasticsearch.document.SearchBeanList;
import com.coffee.coffeeserviceproject.elasticsearch.service.SearchService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SearchController {

  private final SearchService searchService;

  @GetMapping("/search")
  public ResponseEntity<ListResponseDto<List<SearchBeanList>>> searchBeanList(@RequestParam String query,
      Pageable pageable,
      @RequestParam(required = false) String role,
      @RequestParam(required = false) String purchaseStatus) {

    Page<SearchBeanList> searchBeanLists = searchService.searchBeanList(query, role, purchaseStatus, pageable);

    ListResponseDto<List<SearchBeanList>> responseDto = new ListResponseDto<>(
        searchBeanLists.getContent(),
        searchBeanLists.getTotalElements(),
        searchBeanLists.getTotalPages()
    );

    return ResponseEntity.ok(responseDto);
  }
}
