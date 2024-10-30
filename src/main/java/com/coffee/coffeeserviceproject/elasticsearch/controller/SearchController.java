package com.coffee.coffeeserviceproject.elasticsearch.controller;

import com.coffee.coffeeserviceproject.elasticsearch.document.SearchBeanList;
import com.coffee.coffeeserviceproject.elasticsearch.service.SearchService;
import java.util.HashMap;
import java.util.Map;
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
  public ResponseEntity<Map<String, Object>> searchBeanList(@RequestParam String query,
      Pageable pageable,
      @RequestParam(required = false) String role,
      @RequestParam(required = false) String purchaseStatus) {

    Page<SearchBeanList> searchBeanLists = searchService.searchBeanList(query, role, purchaseStatus, pageable);

    Map<String, Object> response = new HashMap<>();
    response.put("data", searchBeanLists.getContent());
    response.put("totalElements", searchBeanLists.getTotalElements());
    response.put("totalPages", searchBeanLists.getTotalPages());

    return ResponseEntity.ok(response);
  }
}
