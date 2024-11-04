package com.coffee.coffeeserviceproject.elasticsearch.repository;

import com.coffee.coffeeserviceproject.elasticsearch.document.SearchBeanList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface SearchRepository extends ElasticsearchRepository<SearchBeanList, Long> {

  @Query("{\"bool\": {\"should\": [{\"wildcard\": {\"beanName\": \"*?0*\"}}, {\"wildcard\": {\"roasterName\": \"*?0*\"}}]}}")
  Page<SearchBeanList> searchByBeanNameOrRoasterName(String query, Pageable pageable);

  void deleteAllByBeanIdIn(List<Long> searchBeanIds);
}

