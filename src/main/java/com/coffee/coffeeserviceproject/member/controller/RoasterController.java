package com.coffee.coffeeserviceproject.member.controller;

import com.coffee.coffeeserviceproject.member.dto.RoasterDto;
import com.coffee.coffeeserviceproject.member.dto.RoasterUpdateDto;
import com.coffee.coffeeserviceproject.member.service.RoasterService;
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
public class RoasterController {

  private final RoasterService roasterService;

  @PostMapping("/roaster/add-roaster")
  public ResponseEntity<Void> addRoaster(@RequestHeader("AUTH-TOKEN") String token,
      @RequestBody @Valid RoasterDto roasterDto) {

    roasterService.addRoaster(token, roasterDto);

    return ResponseEntity.ok().build();
  }

  @PatchMapping("/roaster/update")
  public ResponseEntity<Void> updateRoaster(@RequestHeader("AUTH-TOKEN") String token,
      @RequestBody @Valid RoasterUpdateDto roasterUpdateDto) {

    roasterService.updateRoaster(token, roasterUpdateDto);

    return ResponseEntity.noContent().build();
  }
}
