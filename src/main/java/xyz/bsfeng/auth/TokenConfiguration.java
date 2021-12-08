package xyz.bsfeng.auth;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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
import xyz.bsfeng.auth.dao.RedisTokenDaoImpl;
import xyz.bsfeng.auth.dao.TokenDao;
import xyz.bsfeng.auth.dao.TokenDaoDefaultImpl;
import xyz.bsfeng.auth.filter.MyFilter;
import xyz.bsfeng.auth.listener.UrlMethodListener;
import xyz.bsfeng.auth.listener.UserLoginListener;
import xyz.bsfeng.auth.running.AuthEnvironmentAware;
import xyz.bsfeng.auth.schedule.TokenSchedule;
import xyz.bsfeng.auth.utils.AuthSpringUtils;

import javax.annotation.Nonnull;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
	@ConfigurationProperties("auth")
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
	public UrlMethodListener urlMethodListener() {
		return new UrlMethodListener();
	}

	@Bean
	public UserLoginListener userLoginListener() {
		return new UserLoginListener();
	}

	@Bean
	public TokenSchedule tokenSchedule() {
		return new TokenSchedule();
	}

	@Bean("authThreadPool")
	@Qualifier("authThreadPool")
	@ConditionalOnMissingBean(ThreadPoolExecutor.class)
	public ThreadPoolExecutor authThreadPool() {
		return new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),
				Runtime.getRuntime().availableProcessors(),
				0L, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<>(),
				threadFactory());
	}

	public ThreadFactory threadFactory() {
		return new ThreadFactory() {
			private final AtomicInteger atomicInteger = new AtomicInteger();

			@Override
			public Thread newThread(@Nonnull Runnable r) {
				String threadName = "auth_" + atomicInteger.getAndIncrement() + "";
				return new Thread(null, r, threadName, 0);
			}
		};
	}

}
