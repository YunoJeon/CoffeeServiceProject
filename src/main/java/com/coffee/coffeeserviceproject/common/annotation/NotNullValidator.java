package com.coffee.coffeeserviceproject.common.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NotNullValidator implements ConstraintValidator<ValidNotNull, Object> {

  @Override
  public boolean isValid(Object o, ConstraintValidatorContext constraintValidatorContext) {

    if (o == null || o.toString().isEmpty()) {

      constraintValidatorContext.disableDefaultConstraintViolation();
      constraintValidatorContext.buildConstraintViolationWithTemplate(
              constraintValidatorContext.getDefaultConstraintMessageTemplate())
          .addConstraintViolation();

      return false;
    }
    return true;
  }
}
