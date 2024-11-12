package com.coffee.coffeeserviceproject.bean.controller;

import com.coffee.coffeeserviceproject.bean.dto.BeanDto;
import com.coffee.coffeeserviceproject.bean.dto.BeanListDto;
import com.coffee.coffeeserviceproject.bean.dto.BeanUpdateDto;
import com.coffee.coffeeserviceproject.bean.service.BeanService;
import com.coffee.coffeeserviceproject.bean.type.PurchaseStatus;
import com.coffee.coffeeserviceproject.common.model.ListResponseDto;
import com.coffee.coffeeserviceproject.member.type.RoleType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
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
@Tag(name = "Beans API", description = "원두 관련 API")
public class BeanController {

  private final BeanService beanService;

  @PostMapping
  @Operation(summary = "원두 둥록", description = "회원유형 상관없이 등록가능합니다.")
  public ResponseEntity<Void> addBean(@RequestBody @Valid BeanDto beanDto,
      @RequestHeader("AUTH-TOKEN") String token) {

    beanService.addBean(beanDto, token);

    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @GetMapping
  @Operation(summary = "원두 목록 조회", description = "비회원도 상관없이 원두를 조회할 수 있고, 페이지 형식이며, 필터 기능도 있어 로스터가 올린 글인지, 구매 가능한 원두인지 필터링이 가능합니다.")
  public ResponseEntity<ListResponseDto<List<BeanListDto>>> getBeanList(Pageable pageable,
      @RequestParam(required = false) RoleType role,
      @RequestParam(required = false) PurchaseStatus purchaseStatus) {

    Page<BeanListDto> beanListDtoPage = beanService.getBeanList(pageable, role, purchaseStatus);

    ListResponseDto<List<BeanListDto>> responseDto = new ListResponseDto<>(
        beanListDtoPage.getContent(),
        beanListDtoPage.getTotalElements(),
        beanListDtoPage.getTotalPages()
    );

    return ResponseEntity.ok(responseDto);
  }

  @GetMapping("/{id}/info")
  @Operation(summary = "원두 상세 조회", description = "비회원도 상관없이 원두를 조회할 수 있고, 회원, 비회원 상관없이 하루 한번 조회수가 Count 됩니다.")
  public ResponseEntity<BeanDto> getBean(@PathVariable("id") Long id,
      @RequestHeader("AUTH-TOKEN") String token, HttpServletRequest request) {

    BeanDto beanDto = beanService.getBean(id, token, request);

    return ResponseEntity.ok(beanDto);
  }

  @PatchMapping("/{id}")
  @Operation(summary = "원두 수정", description = "원두를 등록한 본인만 수정할 수 있고, 수정하지 않는 필드는 \"null\" 값으로 넘기면 해당 필드는 수정되지 않습니다. 또한 수정 시 비밀번호 입력은 필수입니다.")
  public ResponseEntity<BeanUpdateDto> updateBean(@PathVariable("id") Long id,
      @RequestBody @Valid BeanUpdateDto beanUpdateDto,
      @RequestHeader("AUTH-TOKEN") String token) {

    beanService.updateBean(id, beanUpdateDto, token);

    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "원두 삭제", description = "원두를 등록한 본인만 삭제할 수 있습니다.")
  public ResponseEntity<Void> deleteBean(@PathVariable("id") Long id,
      @RequestHeader("AUTH-TOKEN") String token) {

    beanService.deleteBean(id, token);

    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/share")
  @Operation(summary = "원두 공유", description = "비회원도 상관없이 원두를 공유할 수 있고, return 값으로 url 이 자동 생성됩니다.")
  public ResponseEntity<String> shareBeanUrl(@PathVariable("id") Long id) {

    String url = "http://localhost:8080/beans/" + id;

    return ResponseEntity.ok(url);
  }
}
