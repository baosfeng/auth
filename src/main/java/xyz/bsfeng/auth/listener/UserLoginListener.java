package xyz.bsfeng.auth.listener;

import org.springframework.context.ApplicationListener;
import xyz.bsfeng.auth.TokenManager;
import xyz.bsfeng.auth.dao.TempUser;
import xyz.bsfeng.auth.dao.UserInfo;
import xyz.bsfeng.auth.event.UserLoginEvent;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * @author bsfeng
 * @date 2021/12/8 14:35
 */
public class UserLoginListener implements ApplicationListener<UserLoginEvent> {

	@Override
	public void onApplicationEvent(@Nonnull UserLoginEvent event) {
		UserInfo userInfo = (UserInfo) event.getSource();
		if (userInfo instanceof TempUser) return;
		String token = event.getToken();
		Set<String> tokenSet = TokenManager.listById(userInfo.getId());
		tokenSet.add(token);
	}
}
