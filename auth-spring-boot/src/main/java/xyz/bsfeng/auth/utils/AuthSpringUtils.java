package xyz.bsfeng.auth.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;

import javax.annotation.Nonnull;

public class AuthSpringUtils implements ApplicationContextAware {

	private static ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(@Nonnull ApplicationContext context) throws BeansException {
		applicationContext = context;
	}

	public static <T> T getClass(Class<T> clazz) {
		return applicationContext.getBean(clazz);
	}


	public static void publishEvent(ApplicationEvent applicationEvent) {
		applicationContext.publishEvent(applicationEvent);
	}

}
