package xyz.bsfeng.auth;

import xyz.bsfeng.auth.dao.RedisTokenDaoImpl;
import xyz.bsfeng.auth.dao.TokenDao;
import xyz.bsfeng.auth.dao.TokenDaoDefaultImpl;
import xyz.bsfeng.auth.filter.MyFilter;
import xyz.bsfeng.auth.listener.AuthBeanPostProcessor;
import xyz.bsfeng.auth.running.AuthEnvironmentAware;
import xyz.bsfeng.auth.utils.AuthSpringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import xyz.bsfeng.auth.config.AuthConfig;

/**
 * @author bsfeng
 * @date 2021/8/7-10:02
 * @since 1.0
 */
@Configuration
public class TokenConfiguration implements WebMvcConfigurer {

	@Bean
	@Primary
	@ConditionalOnBean(RedisConnectionFactory.class)
	public TokenDao redisTokenDao(RedisConnectionFactory connectionFactory) {
		StringRedisSerializer keySerializer = new StringRedisSerializer();
		GenericJackson2JsonRedisSerializer valueSerializer = new GenericJackson2JsonRedisSerializer();
		// 构建RedisTemplate
		RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(connectionFactory);
		redisTemplate.setKeySerializer(keySerializer);
		redisTemplate.setHashKeySerializer(keySerializer);
		redisTemplate.setValueSerializer(valueSerializer);
		redisTemplate.setHashValueSerializer(valueSerializer);
		redisTemplate.afterPropertiesSet();
		return new RedisTokenDaoImpl(redisTemplate, authConfig());
	}


	@Bean
	@ConditionalOnMissingClass("org.springframework.data.redis.core.RedisTemplate")
	public TokenDao defaultTokenDao() {
		return new TokenDaoDefaultImpl();
	}

	@Bean
	@ConfigurationProperties("xyz/bsfeng/auth")
	public AuthConfig authConfig() {
		return new AuthConfig();
	}

	@Bean
	public AuthSpringUtils springUtils() {
		return new AuthSpringUtils();
	}

	@Bean
	public AuthEnvironmentAware authEnvironmentProcessor() {
		return new AuthEnvironmentAware();
	}

	@Bean
	public MyFilter authFilter() {
		return new MyFilter();
	}

	@Bean
	public AuthBeanPostProcessor authBeanPostProcessor() {
		return new AuthBeanPostProcessor();
	}

}
