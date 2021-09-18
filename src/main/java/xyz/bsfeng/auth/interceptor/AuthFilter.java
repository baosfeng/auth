package xyz.bsfeng.auth.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import xyz.bsfeng.auth.exception.AuthException;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

/**
 * @author bsfeng
 * @date 2021/9/18 16:35
 */
public class AuthFilter implements Filter {

	private final ObjectMapper obj = new ObjectMapper();

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		try {
			chain.doFilter(request, response);
		} catch (AuthException e) {
			HttpServletResponse resp = (HttpServletResponse) response;
			resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
			resp.setContentType("application/json; charset=UTF-8");
			HashMap<String, Object> map = new HashMap<>(4);
			map.put("code", e.getCode());
			map.put("message", e.getMessage());
			PrintWriter writer = resp.getWriter();
			writer.write(obj.writeValueAsString(map));
			writer.flush();
		}
	}
}
