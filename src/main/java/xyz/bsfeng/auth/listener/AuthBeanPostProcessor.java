package xyz.bsfeng.auth.listener;

import com.google.common.cache.Cache;
import com.google.common.collect.Sets;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import xyz.bsfeng.auth.TokenManager;
import xyz.bsfeng.auth.utils.AuthCollectionUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class AuthBeanPostProcessor implements BeanPostProcessor {

	private final Cache<String, Method> cache = TokenManager.cache;
	private final Cache<String, Method> urlMethodCache = TokenManager.urlMethodCache;

	@Override
	public Object postProcessAfterInitialization(@NonNull Object bean,
	                                             @NonNull String beanName) throws BeansException {
		Class<?> beanClass = bean.getClass();
		if (isControllerBean(beanClass)) {
			Method[] methods = ReflectionUtils.getAllDeclaredMethods(beanClass);
			Set<String> controllerPathSet = getAnnoValue(AnnotatedElementUtils.getAllMergedAnnotations(beanClass, RequestMapping.class));
			for (Method method : methods) {
				Set<String> methodPathSet = getAnnoValue(AnnotatedElementUtils.getAllMergedAnnotations(method, RequestMapping.class));
				if (AuthCollectionUtils.isEmpty(methodPathSet)) continue;
				Set<String> urlList = getUrl(controllerPathSet, methodPathSet);
				for (String url : urlList) {
					if (url.contains("*")) {
						cache.put(url, method);
						continue;
					}
					urlMethodCache.put(url, method);
				}
			}
		}
		return bean;
	}

	private Set<String> getAnnoValue(Set<RequestMapping> method) {
		return method
				.stream()
				.map(RequestMapping::value)
				.flatMap(Arrays::stream)
				.collect(Collectors.toSet());
	}

	private boolean isControllerBean(Class<?> beanClass) {
		return AnnotatedElementUtils.hasAnnotation(beanClass, Controller.class);
	}

	private static final Pattern PATTERN = Pattern.compile("(\\{.*?})");

	private Set<String> getUrl(Set<String> values, Set<String> urls) {
		Set<String> set = Sets.newHashSet();
		if (AuthCollectionUtils.isEmpty(values)) values = Sets.newHashSet("");
		for (String value : values) {
			for (String url : urls) {
				if (url.startsWith("/") || value.endsWith("/")) {
					String s = value + url;
					// 设置pathVariable参数
					Matcher matcher = PATTERN.matcher(s);
					if (matcher.find()) {
						set.add(matcher.replaceAll("*"));
						continue;
					}
					set.add(s);
					continue;
				}
				set.add(value + "/" + url);
			}
		}
		return set;
	}
}
