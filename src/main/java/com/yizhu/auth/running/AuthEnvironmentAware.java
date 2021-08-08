package com.yizhu.auth.running;

import com.yizhu.auth.exception.AuthParamException;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.util.StringUtils;

import java.util.Map;

public class AuthEnvironmentAware implements EnvironmentAware {

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
}
