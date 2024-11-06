package com.coffee.coffeeserviceproject.configuration;

import com.siot.IamportRestClient.IamportClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PortOneConfig {

  @Value("${imp.v1.api.key}")
  private String apiKey;

  @Value("${imp.v1.api.secret}")
  private String apiSecretKey;

  @Bean
  public IamportClient iamportClient() {
    return new IamportClient(apiKey, apiSecretKey);
  }
}
