package xyz.bsfeng.auth.running;

import xyz.bsfeng.auth.TokenManager;
import xyz.bsfeng.auth.config.AuthConfig;
import xyz.bsfeng.auth.dao.RedisTokenDaoImpl;
import xyz.bsfeng.auth.dao.TokenDao;
import xyz.bsfeng.auth.dao.TokenDaoDefaultImpl;
import xyz.bsfeng.auth.exception.AuthParamException;
import xyz.bsfeng.auth.interceptor.AuthInterceptor;
import xyz.bsfeng.auth.utils.TokenUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.util.StringUtils;

import java.util.Map;

public class AuthEnvironmentAware implements EnvironmentAware, ApplicationContextAware {

	@Override
	public void setEnvironment(Environment environment) {
		ConfigurableEnvironment c = (ConfigurableEnvironment) environment;
		MutablePropertySources sources = c.getPropertySources();
		for (PropertySource<?> source : sources) {
			if (source.getName().startsWith("applicationConfig")) {
				Map<String, Object> bootProp = (Map<String, Object>) source.getSource();
				for (String key : bootProp.keySet()) {
					if (key.startsWith("auth")) {
						Object o = bootProp.get(key);
						if (StringUtils.isEmpty(o.toString())) {
							throw new AuthParamException(key);
						}
					}
				}
				break;
			}
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		AuthConfig authConfig = applicationContext.getBean(AuthConfig.class);
		if (authConfig == null) {
			throw new AuthParamException("authConfig对象");
		}
		TokenManager.setConfig(authConfig);
		TokenDao tokenDao;
		try {
			tokenDao = applicationContext.getBean(RedisTokenDaoImpl.class);
		} catch (NoSuchBeanDefinitionException e) {
			tokenDao = applicationContext.getBean(TokenDaoDefaultImpl.class);
		}
		if (tokenDao == null) {
			throw new AuthParamException("tokenDao对象为空");
		}
		TokenManager.setTokenDao(tokenDao);
		TokenUtils.init();
		AuthInterceptor authInterceptor = applicationContext.getBean(AuthInterceptor.class);
		authInterceptor.init();
	}

}
