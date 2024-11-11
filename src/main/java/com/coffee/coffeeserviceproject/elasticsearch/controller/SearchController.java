package com.coffee.coffeeserviceproject.elasticsearch.controller;

import com.coffee.coffeeserviceproject.common.model.ListResponseDto;
import com.coffee.coffeeserviceproject.elasticsearch.document.SearchBeanList;
import com.coffee.coffeeserviceproject.elasticsearch.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Search API", description = "원두, 로스터 검색 API")
public class SearchController {

  private final SearchService searchService;

  @GetMapping("/search")
  @Operation(summary = "원두, 로스터 검색", description = "비회원도 상관없이 원두를 검색할 수 있고, 페이지 형식이며, 필터 기능도 있어 로스터가 올린 글인지, 구매 가능한 원두인지 필터링이 가능합니다.")
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
