package com.example;

import com.yizhu.auth.utils.TokenUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

	Logger log = LoggerFactory.getLogger(HelloController.class);
	@Autowired
	private TokenUtils tokenUtils;

	@GetMapping("/login")
	public String login(Long id) {
		User user = new User(id);
		return tokenUtils.login(user);
	}

	@GetMapping("/info")
	public Long getUserId() {
		return tokenUtils.getId();
	}

	@GetMapping("/logout")
	public String logout() {
		tokenUtils.logout();
		return "退出成功";
	}
}
