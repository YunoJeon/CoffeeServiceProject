package com.coffee.coffeeserviceproject.bean.controller;

import com.coffee.coffeeserviceproject.bean.dto.BeanDto;
import com.coffee.coffeeserviceproject.bean.dto.BeanListDto;
import com.coffee.coffeeserviceproject.bean.dto.BeanUpdateDto;
import com.coffee.coffeeserviceproject.bean.service.BeanService;
import com.coffee.coffeeserviceproject.bean.type.PurchaseStatus;
import com.coffee.coffeeserviceproject.member.type.RoleType;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/beans")
public class BeanController {

  private final BeanService beanService;

  @PostMapping
  public ResponseEntity<Void> addBean(@RequestBody @Valid BeanDto beanDto,
      @RequestHeader("AUTH-TOKEN") String token) {

    beanService.addBean(beanDto, token);

    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @GetMapping("/list")
  public ResponseEntity<Map<String, Object>> getBeanList(Pageable pageable,
      @RequestParam(required = false) RoleType role,
      @RequestParam(required = false) PurchaseStatus purchaseStatus) {

    Page<BeanListDto> beanListDtoPage = beanService.getBeanList(pageable, role, purchaseStatus);

    Map<String, Object> response = new HashMap<>();
    response.put("data", beanListDtoPage.getContent());
    response.put("totalElements", beanListDtoPage.getTotalElements());
    response.put("totalPages", beanListDtoPage.getTotalPages());

    return ResponseEntity.ok(response);
  }

  @GetMapping("/{id}/info")
  public ResponseEntity<BeanDto> getBean(@PathVariable("id") Long id) {

    BeanDto beanDto = beanService.getBean(id);

    return ResponseEntity.ok(beanDto);
  }

  @PatchMapping("/{id}")
  public ResponseEntity<BeanUpdateDto> updateBean(@PathVariable("id") Long id,
      @RequestBody @Valid BeanUpdateDto beanUpdateDto,
      @RequestHeader("AUTH-TOKEN") String token) {

    beanService.updateBean(id, beanUpdateDto, token);

    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteBean(@PathVariable("id") Long id,
      @RequestHeader("AUTH-TOKEN") String token) {

    beanService.deleteBean(id, token);

    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/share")
  public ResponseEntity<String> shareBeanUrl(@PathVariable("id") Long id) {

    String url = "http://localhost:8080/beans/" + id;

    return ResponseEntity.ok(url);
  }
}
