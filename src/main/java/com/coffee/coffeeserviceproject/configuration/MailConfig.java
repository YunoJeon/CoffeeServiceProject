package com.coffee.coffeeserviceproject.configuration;

import java.util.Properties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
@Data
@ConfigurationProperties(prefix = "spring.mail")
public class MailConfig {

  private String host;
  private String username;
  private String password;
  private int port;
  private Properties properties;

  @Bean
  public JavaMailSender javaMailSender() {
    JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
    mailSender.setHost(host);
    mailSender.setPort(port);
    mailSender.setUsername(username);
    mailSender.setPassword(password);
    mailSender.setJavaMailProperties(properties);

    mailSender.setDefaultEncoding("UTF-8");

    return mailSender;
  }
}