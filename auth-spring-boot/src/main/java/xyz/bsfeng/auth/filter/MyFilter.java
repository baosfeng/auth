package xyz.bsfeng.auth.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import xyz.bsfeng.auth.config.AuthConfig;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author bsfeng
 * @date 2021/9/28 11:45
 */
@Order(value = Ordered.HIGHEST_PRECEDENCE)
public class MyFilter implements Filter {

	@Autowired
	private AuthConfig authConfig;


	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		AuthFilterChain authFilter = new AuthFilterChain(authConfig);
		boolean filterResult = authFilter.doFilter((HttpServletRequest) request, (HttpServletResponse) response);
		if (filterResult) chain.doFilter(request, response);
	}

}
