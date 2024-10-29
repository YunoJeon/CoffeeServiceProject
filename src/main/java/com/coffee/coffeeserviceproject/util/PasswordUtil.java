package com.coffee.coffeeserviceproject.util;

import lombok.experimental.UtilityClass;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@UtilityClass
public class PasswordUtil {

  private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

  public static String hashPassword(String password) {
    return encoder.encode(password);
  }

  public static boolean matches(String password, String hashedPassword) {
    return encoder.matches(password, hashedPassword);
  }
}