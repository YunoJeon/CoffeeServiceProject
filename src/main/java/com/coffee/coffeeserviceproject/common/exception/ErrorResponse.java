package com.coffee.coffeeserviceproject.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponse {

  private String code;
  private String message;
}