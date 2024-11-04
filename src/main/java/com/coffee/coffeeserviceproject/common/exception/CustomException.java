package com.coffee.coffeeserviceproject.common.exception;

import com.coffee.coffeeserviceproject.common.type.ErrorCode;
import lombok.Getter;

@Getter
public class CustomException extends RuntimeException{

  private final ErrorCode errorCode;
  private final String addMessage;

  public CustomException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
    this.addMessage = errorCode.getMessage();
  }

  public CustomException(ErrorCode errorCode, String addMessage) {
    super(errorCode.getMessage() + (addMessage != null ? "-> " + addMessage : ""));
    this.errorCode = errorCode;
    this.addMessage = addMessage;
  }
}