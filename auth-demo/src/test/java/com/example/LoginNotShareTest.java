package com.example;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author bsfeng
 * @date 2021/9/1 18:08
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("share")
@RunWith(SpringRunner.class)
public class LoginNotShareTest {
	@Autowired
	private MockMvc mvc;

	@Test
	public void testLoginWithDevice() throws Exception {
		String expireToken = mvc.perform(get("/login/device?id=1")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		mvc.perform(get("/login/device?id=1")).andExpect(status().isOk());
		mvc.perform(get("/info").header("token", expireToken)).andExpect(status().is(403));
	}


}
