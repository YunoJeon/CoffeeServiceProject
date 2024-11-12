package com.coffee.coffeeserviceproject.configuration;

import com.coffee.coffeeserviceproject.common.model.ListWrapper;
import com.coffee.coffeeserviceproject.order.cart.dto.CartListDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

  @Value("${spring.data.redis.host}")
  private String host;

  @Value("${spring.data.redis.port}")
  private int port;

  @Bean
  public RedisConnectionFactory getConnectionFactory() {
    return new LettuceConnectionFactory(host, port);
  }

  @Bean
  public RedisTemplate<String, ListWrapper<CartListDto>> redisTemplateForCart() {

    RedisTemplate<String, ListWrapper<CartListDto>> redisTemplate = new RedisTemplate<>();

    redisTemplate.setConnectionFactory(getConnectionFactory());

    redisTemplate.setKeySerializer(new StringRedisSerializer());

    redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());

    return redisTemplate;
  }

  @Bean
  public RedisTemplate<String, String> redisTemplateForCount() {

    RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();

    redisTemplate.setConnectionFactory(getConnectionFactory());

    redisTemplate.setKeySerializer(new StringRedisSerializer());

    redisTemplate.setValueSerializer(new StringRedisSerializer());

    return redisTemplate;
  }
}
