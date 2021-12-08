package com.example;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.bsfeng.auth.anno.PreAuthorize;
import xyz.bsfeng.auth.pojo.AuthTempUser;
import xyz.bsfeng.auth.pojo.AuthUser;
import xyz.bsfeng.auth.utils.TokenUtils;

@RestController
public class HelloController {

	@GetMapping("/login")
	public String login(Long id) {
		User user = new User(id);
		user.setRoles("admin", "read", "write");
		AuthUser authUser = new AuthUser(id);
		return TokenUtils.login(authUser);
	}

	@GetMapping("/login/device")
	public String loginDevice(Long id) {
		AuthUser authUser = new AuthUser(id);
		return TokenUtils.login(authUser);
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
		TokenUtils.lock(TokenUtils.getId(), 100L);
		return "封禁成功";
	}

	@PreAuthorize(hasRole = "all")
	@GetMapping("/role/check")
	public Long getRole() {
		return TokenUtils.getId();
	}

	@GetMapping("/temp")
	public String tempLogin() {
		AuthTempUser authTempUser = new AuthTempUser(1L);
		return TokenUtils.loginTemp(authTempUser, 5L);
	}

	@GetMapping("/logout")
	public String logout() {
		TokenUtils.logout();
		return "退出成功";
	}
}
