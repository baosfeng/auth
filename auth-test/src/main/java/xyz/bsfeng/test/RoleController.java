package xyz.bsfeng.test;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xyz.bsfeng.auth.anno.IgnoreLogin;
import xyz.bsfeng.auth.anno.PreAuthorize;
import xyz.bsfeng.auth.pojo.AuthUser;
import xyz.bsfeng.auth.pojo.AuthUserBuilder;
import xyz.bsfeng.auth.utils.TokenUtils;

import java.util.Collections;

/**
 * @author bsfeng
 * @date 2022/8/2-14:26
 */
@RestController
public class RoleController {

	@IgnoreLogin
	@GetMapping("/roleLogin")
	public String login(@RequestParam String username, @RequestParam String password) {
		System.out.println("username = " + username);
		System.out.println("password = " + password);
		if (username.equals("admin") && password.equals("abc123")) {
			AuthUser authUser = new AuthUserBuilder().id(1L).roles(Collections.singleton("admin")).build();
			return TokenUtils.login(authUser);
		}
		return null;
	}

	@PreAuthorize(hasRole = "admin")
	@GetMapping("/role")
	public String role() {
		return "role";
	}
}
