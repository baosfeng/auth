package xyz.bsfeng.auth.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import xyz.bsfeng.auth.exception.AuthException;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

/**
 * @author bsfeng
 * @date 2021/10/15 14:57
 */
public class MessageUtils {

	private static final ObjectMapper objectMapper = new ObjectMapper();

	public static void sendErrorMessage(HttpServletResponse servletResponse, AuthException e) throws IOException {
		if (servletResponse.isCommitted()) return;
		servletResponse.setContentType("application/json; charset=UTF-8");
		servletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
		HashMap<String, Object> map = new HashMap<>(4);
		map.put("code", e.getCode());
		map.put("message", e.getMessage());
		PrintWriter writer = servletResponse.getWriter();
		writer.write(objectMapper.writeValueAsString(map));
		writer.flush();
		writer.close();
	}
}
