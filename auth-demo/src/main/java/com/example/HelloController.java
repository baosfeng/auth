package com.example;

import xyz.bsfeng.auth.TokenManager;
import xyz.bsfeng.auth.anno.PreAuthorize;
import xyz.bsfeng.auth.utils.TokenUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

	@GetMapping("/login")
	public String login(Long id) {
		System.out.println(TokenManager.getConfig());
		System.out.println(TokenManager.getTokenDao());
		User user = new User(id);
		user.setPassword("abc123");
		user.setRoles("admin", "read", "write");
		return TokenUtils.login(user);
	}

	@GetMapping("/info")
	public Long getUserId() {
		return TokenUtils.getId();
	}

	@GetMapping("/temp/info")
	public Long getTempInfo() {
		Long id = TokenUtils.getId();
		UserTemp temp = new UserTemp();
		temp.setId(id);
		TokenUtils.checkTempUser(temp);
		return id;
	}

	@GetMapping("/kickOut")
	public String kickOut() {
		TokenUtils.kickOut();
		return "踢出成功";
	}

	@GetMapping("/lock")
	public String lock() {
		TokenUtils.lock(100L);
		return "封禁成功";
	}

	@PreAuthorize(hasRole = "all")
	@GetMapping("/role/check")
	public Long getRole() {
		return TokenUtils.getId();
	}

	@GetMapping("/temp")
	public String tempLogin() {
		UserTemp userTemp = new UserTemp();
		return TokenUtils.loginTemp(userTemp, 100L);
	}

	@GetMapping("/logout")
	public String logout() {
		TokenUtils.logout();
		return "退出成功";
	}
}
