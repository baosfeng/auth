package xyz.bsfeng.auth.listener;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import xyz.bsfeng.auth.TokenManager;
import xyz.bsfeng.auth.anno.IgnoreLogin;
import xyz.bsfeng.auth.anno.MustLogin;
import xyz.bsfeng.auth.config.AuthConfig;
import xyz.bsfeng.auth.interceptor.AuthFilter;
import xyz.bsfeng.auth.utils.BooleanUtils;
import xyz.bsfeng.auth.utils.CollectionUtils;
import xyz.bsfeng.auth.utils.StringUtils;

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
public class ApplicationStartListener implements ApplicationListener<ApplicationReadyEvent> {

	private final Logger log = LoggerFactory.getLogger(ApplicationStartListener.class);
	private static final Pattern PATTERN = Pattern.compile("(\\{.*?})");


	@Override
	public void onApplicationEvent(@Nonnull ApplicationReadyEvent event) {
		AuthConfig config = TokenManager.getConfig();
		if (BooleanUtils.isFalse(config.getEnable())) return;
		ConfigurableApplicationContext applicationContext = event.getApplicationContext();
		String[] restControllerAnnotations = applicationContext.getBeanNamesForAnnotation(RestController.class);
		String[] controllerAnnotations = applicationContext.getBeanNamesForAnnotation(Controller.class);
		Set<String> strings = Arrays.stream(restControllerAnnotations).collect(Collectors.toSet());
		strings.addAll(Arrays.stream(controllerAnnotations).collect(Collectors.toSet()));
		Set<String> whiteUrlSet = Sets.newHashSet();
		Set<String> blackUrlSet = Sets.newHashSet();
		for (String annotation : strings) {
			Class<?> type = AopUtils.getTargetClass(applicationContext.getBean(annotation));
			doWhiteUrl(whiteUrlSet, type);
			doBlackUrl(blackUrlSet, type);
			Method[] methods = type.getDeclaredMethods();
			for (Method method : methods) {
				whiteUrlSet.addAll(doWhiteList(type, method));
				blackUrlSet.addAll(doBlackList(type, method));
			}
		}
		if (config.getLog()) {
			if (CollectionUtils.isNotEmpty(whiteUrlSet)) log.info("白名单列表:{}", whiteUrlSet);
			if (CollectionUtils.isNotEmpty(blackUrlSet)) log.info("黑名单列表:{}", blackUrlSet);
		}
		String whiteUrls = Joiner.on(",").join(whiteUrlSet);
		String whiteUrlList = config.getWhiteUrlList();
		if (StringUtils.isEmpty(whiteUrlList)) {
			config.setWhiteUrlList(whiteUrls);
		} else {
			config.setWhiteUrlList(whiteUrlList + "," + whiteUrls);
		}

		String blackUrls = Joiner.on(",").join(blackUrlSet);
		String blackUrlList = config.getBlackUrlList();
		if (StringUtils.isEmpty(blackUrlList)) {
			config.setBlackUrlList(blackUrls);
		} else {
			config.setBlackUrlList(blackUrlList + "," + blackUrls);
		}

		AuthFilter authFilter = applicationContext.getBean(AuthFilter.class);
		authFilter.init();
	}

	private void doWhiteUrl(Set<String> whiteUrlSet, Class<?> type) {
		IgnoreLogin ignoreLogin = type.getAnnotation(IgnoreLogin.class);
		if (ignoreLogin != null) {
			doUrl(whiteUrlSet, type);
		}
	}

	private void doUrl(Set<String> whiteUrlSet, Class<?> type) {
		RequestMapping mapping = type.getAnnotation(RequestMapping.class);
		if (mapping != null) {
			for (String value : mapping.value()) {
				if (!value.endsWith("/")) {
					value = value + "/";
				}
				whiteUrlSet.add(value + "**");
			}
		}
	}

	private void doBlackUrl(Set<String> blackUrlSet, Class<?> type) {
		MustLogin mustLogin = type.getAnnotation(MustLogin.class);
		if (mustLogin != null) {
			doUrl(blackUrlSet, type);
		}
	}

	private Set<String> doWhiteList(Class<?> type, Method method) {
		IgnoreLogin ignoreLogin = method.getAnnotation(IgnoreLogin.class);
		if (ignoreLogin != null) {
			return getUrlList(type, method);
		}
		return Sets.newHashSet();
	}

	private Set<String> doBlackList(Class<?> type, Method method) {
		MustLogin mustLogin = method.getAnnotation(MustLogin.class);
		if (mustLogin != null) {
			return getUrlList(type, method);
		}
		return Sets.newHashSet();
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


