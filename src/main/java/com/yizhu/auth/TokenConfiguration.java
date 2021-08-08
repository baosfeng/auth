package com.yizhu.auth;

import com.yizhu.auth.config.AuthConfig;
import com.yizhu.auth.dao.RedisTokenDaoImpl;
import com.yizhu.auth.dao.TokenDao;
import com.yizhu.auth.dao.TokenDaoDefaultImpl;
import com.yizhu.auth.exception.AuthExceptionHandler;
import com.yizhu.auth.interceptor.AuthInterceptor;
import com.yizhu.auth.running.AuthEnvironmentAware;
import com.yizhu.auth.utils.SpringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author bsfeng
 * @date 2021/8/7-10:02
 * @since 1.0
 */
@Configuration
public class TokenConfiguration implements WebMvcConfigurer {

	@Bean
	@Primary
	@ConditionalOnClass(RedisTemplate.class)
	public TokenDao redisTokenDao() {
		return new RedisTokenDaoImpl();
	}


	@Bean
	@ConditionalOnMissingClass("org.springframework.data.redis.core.RedisTemplate")
	public TokenDao defaultTokenDao() {
		return new TokenDaoDefaultImpl();
	}

	@Bean
	@ConfigurationProperties("auth")
	public AuthConfig authConfig() {
		return new AuthConfig();
	}

	@Bean
	public SpringUtils springUtils() {
		return new SpringUtils();
	}

	@Bean
	public AuthEnvironmentAware authEnvironmentProcessor() {
		return new AuthEnvironmentAware();
	}

	@Bean
	@Order
	public AuthInterceptor authInterceptor() {
		return new AuthInterceptor();
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(authInterceptor());
	}

	@Bean
	public AuthExceptionHandler authExceptionHandler() {
		return new AuthExceptionHandler();
	}

}
