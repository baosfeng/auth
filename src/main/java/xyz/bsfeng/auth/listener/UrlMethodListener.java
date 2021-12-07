package xyz.bsfeng.auth.listener;

import com.google.common.cache.Cache;
import com.google.common.collect.Sets;
import org.springframework.aop.support.AopUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import xyz.bsfeng.auth.TokenManager;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author bsfeng
 * @date 2021/9/28 14:12
 */
public class UrlMethodListener implements ApplicationListener<ApplicationReadyEvent> {

	private static final Pattern PATTERN = Pattern.compile("(\\{.*?})");
	private final Cache<String, Method> cache = TokenManager.cache;


	@Override
	public void onApplicationEvent(@Nonnull ApplicationReadyEvent event) {
		ConfigurableApplicationContext applicationContext = event.getApplicationContext();
		String[] restControllerAnnotations = applicationContext.getBeanNamesForAnnotation(RestController.class);
		String[] controllerAnnotations = applicationContext.getBeanNamesForAnnotation(Controller.class);
		Set<String> strings = Arrays.stream(restControllerAnnotations).collect(Collectors.toSet());
		strings.addAll(Arrays.stream(controllerAnnotations).collect(Collectors.toSet()));
		for (String annotation : strings) {
			Class<?> type = AopUtils.getTargetClass(applicationContext.getBean(annotation));
			Method[] methods = type.getDeclaredMethods();
			for (Method method : methods) {
				Set<String> urlList = getUrlList(type, method);
				urlList.forEach(itm -> cache.put(itm, method));
			}
		}
	}

	private Set<String> getUrlList(Class<?> type, Method method) {
		Set<String> set = Sets.newHashSet();
		RequestMapping requestMapping = type.getAnnotation(RequestMapping.class);
		String[] values = requestMapping.value();
		RequestMapping mapping = method.getAnnotation(RequestMapping.class);
		if (mapping != null) {
			return getUrl(values, mapping.value());
		}
		GetMapping getMapping = method.getAnnotation(GetMapping.class);
		if (getMapping != null) {
			return getUrl(values, getMapping.value());
		}
		PostMapping postMapping = method.getAnnotation(PostMapping.class);
		if (postMapping != null) {
			return getUrl(values, postMapping.value());
		}
		PutMapping putMapping = method.getAnnotation(PutMapping.class);
		if (putMapping != null) {
			return getUrl(values, putMapping.value());
		}
		DeleteMapping deleteMapping = method.getAnnotation(DeleteMapping.class);
		if (deleteMapping != null) {
			return getUrl(values, deleteMapping.value());
		}
		PatchMapping patchMapping = method.getAnnotation(PatchMapping.class);
		if (patchMapping != null) {
			return getUrl(values, patchMapping.value());
		}
		return set;
	}

	private Set<String> getUrl(String[] values, String[] urls) {
		Set<String> set = Sets.newHashSet();
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


