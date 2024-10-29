package com.coffee.coffeeserviceproject.common.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PhoneValidator implements ConstraintValidator<ValidPhone, String> {

  @Override
  public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {

    if (s == null || s.isEmpty()) {
      return true;
    }
    return s.matches("01(?:0|1|[6-9])[.-]?(\\d{3}|\\d{4})[.-]?(\\d{4})$");
  }
}
