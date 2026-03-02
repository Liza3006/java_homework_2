package org.example.spring_hw.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import java.util.Arrays;

/**
 * Аспект для логирования вызовов методов
 * */

@Aspect
@Component
public class LoggingAspect {

  @Around("execution(* org.example.spring_hw.service.*.*(..))")
  public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
    String methodName = joinPoint.getSignature().getName();
    String className = joinPoint.getTarget().getClass().getSimpleName();
    Object[] args = joinPoint.getArgs();

    System.out.println("=== AOP @Around: начало метода " + className + "." + methodName +
      " с аргументами: " + Arrays.toString(args));

    long start = System.currentTimeMillis();
    Object result;
    try {
      result = joinPoint.proceed();
    } catch (Throwable t) {
      System.out.println("=== AOP @Around: исключение в методе " + methodName + ": " + t.getMessage());
      throw t;
    }
    long duration = System.currentTimeMillis() - start;

    System.out.println("=== AOP @Around: конец метода " + methodName +
      ", результат: " + result + ", время выполнения: " + duration + " ms");
    return result;
  }
}