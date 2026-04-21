package org.example.spring_hw.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(2)
public class AccessLogFilter extends OncePerRequestFilter {
  private static final Logger log = LoggerFactory.getLogger(AccessLogFilter.class);

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    long started = System.currentTimeMillis();
    try {
      filterChain.doFilter(request, response);
    } finally {
      long timeMs = System.currentTimeMillis() - started;
      log.info("HTTP {} {} -> status={} timeMs={} trace={}",
          request.getMethod(),
          request.getRequestURI(),
          response.getStatus(),
          timeMs,
          MDC.get(TraceIdFilter.TRACE_ID));
    }
  }
}
