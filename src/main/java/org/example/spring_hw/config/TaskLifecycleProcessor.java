package org.example.spring_hw.config;

import org.example.spring_hw.repository.TaskRepository;
import org.example.spring_hw.service.TaskService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 * BeanPostProcessor для отслеживания жизненного цикла
 */
@Component
public class TaskLifecycleProcessor implements BeanPostProcessor {

  @Override
  public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
    if (bean instanceof TaskService || bean instanceof TaskRepository) {
      System.out.println("BeanPostProcessor BEFORE init: " + beanName + " (" + bean.getClass().getSimpleName() + ")");
    }
    return bean;
  }

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    if (bean instanceof TaskService || bean instanceof TaskRepository) {
      System.out.println("BeanPostProcessor AFTER init: " + beanName + " (" + bean.getClass().getSimpleName() + ")");
    }
    return bean;
  }
}