package xyz.bsfeng.auth.dao;

/**
 * 必须实现此接口,且用户的id必须全局唯一
 * 交给用户控制
 *
 * @author bsfeng
 * @date 2021/8/7-9:08
 * @since 1.0
 */
public interface UserInfo {

	Long getId();
}
