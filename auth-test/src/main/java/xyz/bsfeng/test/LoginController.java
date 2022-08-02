package xyz.bsfeng.test;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xyz.bsfeng.auth.anno.IgnoreLogin;
import xyz.bsfeng.auth.pojo.AuthUser;
import xyz.bsfeng.auth.pojo.AuthUserBuilder;
import xyz.bsfeng.auth.utils.TokenUtils;

/**
 * @author bsfeng
 * @date 2022/8/2-11:45
 */
@RestController
@Slf4j
public class LoginController {


	@IgnoreLogin
	@GetMapping("/login")
	public String login(@RequestParam String username, @RequestParam String password) {
		System.out.println("username = " + username);
		System.out.println("password = " + password);
		if (username.equals("admin") && password.equals("abc123")) {
			AuthUser authUser = new AuthUserBuilder().id(1L).build();
			log.info("用户验证成功");
			return TokenUtils.login(authUser);
		}
		log.info("用户验证失败");
		return null;
	}

	@GetMapping("/needLogin")
	public String needLogin() {
		return "hello";
	}

	@GetMapping("/info")
	public String info() {
		return "info";
	}

	@GetMapping("/info1")
	public String info1() {
		return "info1";
	}
}
