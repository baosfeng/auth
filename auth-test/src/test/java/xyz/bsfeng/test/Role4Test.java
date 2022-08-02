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
 * @date 2022/8/2-14:28
 */
@SpringBootTest
@ActiveProfiles("4")
@AutoConfigureMockMvc
public class Role4Test {

	@Autowired
	private MockMvc mockMvc;

	@Test
	public void testLoginWithRole() throws Exception {
		// 尝试获取token
		String token = mockMvc.perform(get("/roleLogin?username=admin&password=abc123"))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();
		assertTrue(StringUtils.isNotEmpty(token));
		// 携带token进行权限访问
		String result = mockMvc.perform(get("/role").header("token", token))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();
		assertEquals("role", result);
	}

	@Test
	public void testLoginWithoutRole() throws Exception {
		// 尝试获取普通token
		String token = mockMvc.perform(get("/login?username=admin&password=abc123"))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();
		assertTrue(StringUtils.isNotEmpty(token));
		// 携带普通token进行权限访问
		String result = mockMvc.perform(get("/role").header("token", token))
				.andExpect(status().isForbidden())
				.andReturn()
				.getResponse()
				.getContentAsString();
		AuthPojo authPojo = JSON.parseObject(result, AuthPojo.class);
		assertEquals(404, authPojo.getCode());
	}
}
