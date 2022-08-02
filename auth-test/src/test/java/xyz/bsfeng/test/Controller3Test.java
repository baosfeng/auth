package xyz.bsfeng.test;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author bsfeng
 * @date 2022/8/2-11:53
 */
@SpringBootTest
@ActiveProfiles("3")
@AutoConfigureMockMvc
class Controller3Test {

	@Autowired
	private MockMvc mockMvc;


	@Test
	public void testLoginExpire() throws Exception {
		// 尝试获取token
		String token = mockMvc.perform(get("/login?username=admin&password=abc123"))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();
		assertTrue(StringUtils.isNotEmpty(token));
		// 携带token进行登录
		String result = mockMvc.perform(get("/needLogin").header("token", token))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();
		assertEquals("hello", result);
		Thread.sleep(500);
		// 1秒之后再次访问
		result = mockMvc.perform(get("/info1").header("token", token))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();
		assertEquals("info1", result);
		Thread.sleep(500);
		// token不会续签,token过期之后继续访问
		String content = mockMvc.perform(get("/info").header("token", token))
				.andExpect(status().isForbidden())
				.andReturn()
				.getResponse()
				.getContentAsString();
		AuthPojo authPojo = JSON.parseObject(content, AuthPojo.class);
		assertEquals(401, authPojo.getCode());
	}
}
