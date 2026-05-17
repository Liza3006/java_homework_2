package org.example.spring_hw.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
      .info(new Info()
        .title("To-Do List API")
        .version("2.0.0")
        .description("API for managing tasks with attachments, favorites, and user preferences")
        .contact(new Contact()
          .name("Developer")
          .email("developer@example.com"))
        .license(new License()
          .name("Apache 2.0")
          .url("http://springdoc.org")));
  }
}