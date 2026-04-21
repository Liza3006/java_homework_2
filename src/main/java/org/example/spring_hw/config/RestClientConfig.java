package org.example.spring_hw.config;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

  @Bean
  RestClient externalRestClient(RestClient.Builder builder,
                                @Value("${app.external-api.base-url}") String baseUrl,
                                @Value("${app.external-api.connect-timeout}") Duration connectTimeout,
                                @Value("${app.external-api.read-timeout}") Duration readTimeout) {
    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setConnectTimeout(connectTimeout);
    requestFactory.setReadTimeout(readTimeout);

    return builder
        .baseUrl(baseUrl)
        .requestFactory(requestFactory)
        .defaultHeader("User-Agent", "spring-hw-gateway/1.0")
        .build();
  }
}
