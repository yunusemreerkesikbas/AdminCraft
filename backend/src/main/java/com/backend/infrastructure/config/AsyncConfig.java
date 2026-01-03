package com.backend.infrastructure.config;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.slf4j.MDC;

import com.backend.infrastructure.tenant.TenantContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.Executor;

@Configuration
@EnableAsync
@Slf4j
@RequiredArgsConstructor
public class AsyncConfig implements AsyncConfigurer {

  private final TenantContext tenantContext;

  @Bean(name = "taskExecutor")
  @Override
  public Executor getAsyncExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(5);
    executor.setMaxPoolSize(10);
    executor.setQueueCapacity(100);
    executor.setThreadNamePrefix("async-executor-");
    executor.setWaitForTasksToCompleteOnShutdown(true);
    executor.setAwaitTerminationSeconds(60);
    executor.setTaskDecorator(new TenantContextTaskDecorator());
    executor.initialize();

    log.info("Async executor configured with TenantContext propagation: corePoolSize=5, maxPoolSize=10");
    return executor;
  }

  @Override
  public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
    return (throwable, method, params) -> {
      log.error("Async execution error in method: {} with params: {}",
          method.getName(), params, throwable);
    };
  }

  /**
   * TaskDecorator that propagates TenantContext and MDC to async threads.
   * This ensures multi-tenant database routing works correctly in @Async methods.
   */
  private class TenantContextTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
      // Capture context from calling thread
      String tenantId = tenantContext.getTenantId();
      String tenantDbName = tenantContext.getTenantDbName();
      String subdomain = tenantContext.getSubdomain();
      Map<String, String> mdcContext = MDC.getCopyOfContextMap();

      return () -> {
        try {
          // Restore context in async thread
          if (tenantId != null) {
            tenantContext.setTenantId(tenantId);
          }
          if (tenantDbName != null) {
            tenantContext.setTenantDbName(tenantDbName);
          }
          if (subdomain != null) {
            tenantContext.setSubdomain(subdomain);
          }
          if (mdcContext != null) {
            MDC.setContextMap(mdcContext);
          }

          log.debug("Async task started with tenant context: tenantId={}, dbName={}",
              tenantId, tenantDbName);

          runnable.run();

        } finally {
          // Clean up after task completes
          tenantContext.clear();
          MDC.clear();
        }
      };
    }
  }
}
