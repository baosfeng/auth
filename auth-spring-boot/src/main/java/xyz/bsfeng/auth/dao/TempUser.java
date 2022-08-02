package xyz.bsfeng.auth.dao;

import xyz.bsfeng.auth.exception.AuthException;

import java.util.Objects;

/**
 * 用于标记是否为临时身份用户,如果需要实现临时身份登录,只需要实现此类
 * 必须保存一个原始的权限对象和新的待校验的权限对象
 *
 * @author bsfeng
 * @date 2021/8/11 17:44
 */
public abstract class TempUser extends UserInfo {
	/**
	 *
	 * 如果不需要校验,那么默认不需要实现
	 * 需要自定义的验证方法
	 * 默认根据对象是否一致判断权限是否满足
	 * @param tempUser
	 * @return
	 */
	public boolean check(TempUser tempUser) throws AuthException {
		return Objects.equals(this, tempUser);
	};
}
