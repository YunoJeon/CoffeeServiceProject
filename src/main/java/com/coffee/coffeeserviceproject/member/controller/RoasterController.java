package com.coffee.coffeeserviceproject.member.controller;

import com.coffee.coffeeserviceproject.member.dto.RoasterDto;
import com.coffee.coffeeserviceproject.member.dto.RoasterUpdateDto;
import com.coffee.coffeeserviceproject.member.service.RoasterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
@Tag(name = "Roaster API", description = "로스터 관련 API")
public class RoasterController {

  private final RoasterService roasterService;

  @PostMapping("/roaster/add-roaster")
  @Operation(summary = "로스터 둥록", description = "이미 등록한 로스터는 등록이 불가능합니다.")
  public ResponseEntity<Void> addRoaster(@RequestHeader("AUTH-TOKEN") String token,
      @RequestBody @Valid RoasterDto roasterDto) {

    roasterService.addRoaster(token, roasterDto);

    return ResponseEntity.ok().build();
  }

  @PatchMapping("/roaster/me")
  @Operation(summary = "로스터 수정", description = "수정하지 않는 필드는 \"null\" 값으로 넘기면 해당 필드는 수정되지 않습니다. 또한 수정 시 비밀번호 입력은 필수입니다.")
  public ResponseEntity<Void> updateRoaster(@RequestHeader("AUTH-TOKEN") String token,
      @RequestBody @Valid RoasterUpdateDto roasterUpdateDto) {

    roasterService.updateRoaster(token, roasterUpdateDto);

    return ResponseEntity.noContent().build();
  }
}
