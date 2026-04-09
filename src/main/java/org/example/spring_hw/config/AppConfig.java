package org.example.spring_hw.config;

import org.example.spring_hw.model.Task;
import org.example.spring_hw.repository.StubTaskRepository;
import org.example.spring_hw.repository.TaskRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Optional;

/**
 * Резервный конфиг, оставлен для будущих инфраструктурных бинов.
 */
@Configuration
public class AppConfig {

  @Bean("stubTaskRepository")
  public TaskRepository stubTaskRepository() {
	StubTaskRepository delegate = new StubTaskRepository();
	InvocationHandler handler = (Object proxy, Method method, Object[] args) ->
	  switch (method.getName()) {
		case "findAll" -> delegate.findAll();
		case "findById" -> Optional.ofNullable(delegate.findById((Long) args[0]));
		case "save" -> delegate.save((Task) args[0]);
		case "deleteById" -> {
		  delegate.deleteById((Long) args[0]);
		  yield null;
		}
		case "existsById" -> delegate.existsById((Long) args[0]);
		case "count" -> (long) delegate.findAll().size();
		case "toString" -> "stubTaskRepository";
		case "hashCode" -> System.identityHashCode(proxy);
		case "equals" -> proxy == (args == null ? null : args[0]);
		default -> throw new UnsupportedOperationException("Method not supported: " + method);
	  };
	return (TaskRepository) Proxy.newProxyInstance(
	  TaskRepository.class.getClassLoader(),
	  new Class<?>[]{TaskRepository.class},
	  handler
	);
  }
}