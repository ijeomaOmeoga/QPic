package com.qpic.common.config;

import com.qpic.common.error.GlobalExceptionHandler;
import com.qpic.common.web.CurrentUserResolver;
import com.qpic.common.web.InternalApiFilter;
import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class CommonAutoConfiguration {

    @Bean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }

    @Bean
    public WebMvcConfigurer currentUserWebMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
                resolvers.add(new CurrentUserResolver());
            }
        };
    }

    @Bean
    public FilterRegistrationBean<InternalApiFilter> internalApiFilter(@Value("${app.internal.secret}") String secret) {
        FilterRegistrationBean<InternalApiFilter> reg = new FilterRegistrationBean<>(new InternalApiFilter(secret));
        reg.addUrlPatterns("/internal/*");
        reg.setOrder(1);
        return reg;
    }

    /** Adds the shared secret to every Feign call (only active when Feign is on the classpath). */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = "feign.RequestInterceptor")
    static class InternalFeignConfig {
        @Bean
        RequestInterceptor internalSecretInterceptor(@Value("${app.internal.secret}") String secret) {
            return template -> template.header(InternalApiFilter.HEADER, secret);
        }
    }
}
