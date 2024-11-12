package com.coffee.coffeeserviceproject.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

  @Bean
  public OpenAPI openAPI() {

    return new OpenAPI()
        .components(new Components())
        .info(apiInfo());
  }

  private Info apiInfo() {

    return new Info()
        .title("커피 레시피 공유 및 원두 구매 서비스")
        .description("커피 애호가들이 특정 원두의 레시피를 공유하여 맛있게 즐기는 것을 목적으로 하며, 로스터는 본인이 로스팅한 원두를 판매할 수 있습니다.")
        .version("1.0.0");
  }
}
