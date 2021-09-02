package com.example;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author bsfeng
 * @date 2021/9/1 18:08
 */
@SpringBootTest
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
public class LoginTest {
	@Autowired
	private MockMvc mvc;
	private String token;

	@Before
	public void testLogin() throws Exception {
		token = mvc.perform(get("/login?id=1")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
	}

	@Test
	public void testInfo() throws Exception {
		mvc.perform(get("/info").header("token", token))
				.andExpect(content().string(1 + ""));
	}

	@Test
	public void testLogout() throws Exception {
		mvc.perform(get("/logout").header("token", token))
				.andExpect(status().isOk());
		mvc.perform(get("/info").header("token", token))
				.andExpect(status().is(403));
	}

	@Test
	public void testLockTempUser() throws Exception {
		testLogin();
		mvc.perform(get("/lock").header("token", token))
				.andExpect(status().isOk());
		mvc.perform(get("/info").header("token", token))
				.andExpect(status().is(403));
		mvc.perform(get("/login?id=1")).andExpect(status().is(403));
	}

}
