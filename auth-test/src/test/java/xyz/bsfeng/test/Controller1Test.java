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
@ActiveProfiles("1")
@AutoConfigureMockMvc
class Controller1Test {

	@Autowired
	private MockMvc mockMvc;


	@Test
	public void testLogin() throws Exception {
		String token = mockMvc.perform(get("/login?username=admin&password=abc123"))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();
		System.out.println(token);
		assertTrue(StringUtils.isNotEmpty(token));
	}

	@Test
	public void testLoginWithErrorUsername() throws Exception {
		String token = mockMvc.perform(get("/login?username=hello&password=abc123"))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();
		assertTrue(StringUtils.isEmpty(token));
	}

	@Test
	public void testWithoutToken() throws Exception {
		String result = mockMvc.perform(get("/needLogin"))
				.andExpect(status().isForbidden())
				.andReturn()
				.getResponse()
				.getContentAsString();
		AuthPojo authPojo = JSON.parseObject(result, AuthPojo.class);
		assertEquals(409, authPojo.getCode());
	}

	@Test
	public void testWithToken() throws Exception {
		String token = mockMvc.perform(get("/login?username=admin&password=abc123"))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();
		System.out.println(token);
		assertTrue(StringUtils.isNotEmpty(token));
		String result = mockMvc.perform(get("/needLogin").header("token", token))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();
		assertEquals("hello", result);
	}
}
