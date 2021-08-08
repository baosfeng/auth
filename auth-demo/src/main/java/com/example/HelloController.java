package com.example;

import com.yizhu.auth.TokenManager;
import com.yizhu.auth.utils.TokenUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

	@GetMapping("/login")
	public String login(Long id) {
		System.out.println(TokenManager.getConfig());
		System.out.println(TokenManager.getTokenDao());
		User user = new User(id);
		return TokenUtils.login(user);
	}

	@GetMapping("/info")
	public Long getUserId() {
		return TokenUtils.getId();
	}

	@GetMapping("/logout")
	public String logout() {
		TokenUtils.logout();
		return "退出成功";
	}
}
