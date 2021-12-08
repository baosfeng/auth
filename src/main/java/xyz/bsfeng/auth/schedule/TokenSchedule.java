package xyz.bsfeng.auth.schedule;

import org.springframework.scheduling.annotation.Scheduled;
import xyz.bsfeng.auth.dao.TokenDao;
import xyz.bsfeng.auth.utils.AuthSpringUtils;

import java.util.concurrent.ExecutionException;

/**
 * @author bsfeng
 * @date 2021/12/8 14:07
 */
public class TokenSchedule {

	@Scheduled(cron = "0 0/30 * * * ? ")
	public void refreshToken() throws ExecutionException {
		TokenDao tokenDao = AuthSpringUtils.getClass(TokenDao.class);
		tokenDao.refreshTokenListById();
	}
}
