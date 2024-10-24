package com.coffee.coffeeserviceproject.common.exception;

import com.coffee.coffeeserviceproject.common.type.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CustomException extends RuntimeException{

  private final ErrorCode errorCode;
}