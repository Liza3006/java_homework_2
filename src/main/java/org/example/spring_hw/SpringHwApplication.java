package org.example.spring_hw;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class SpringHwApplication {
  public static void main(String[] args) {
    SpringApplication.run(SpringHwApplication.class, args);
  }
}
