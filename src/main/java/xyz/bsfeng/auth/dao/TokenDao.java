package xyz.bsfeng.auth.dao;


import java.util.List;

/**
 * Sa-Token持久层接口
 *
 * @author bsfeng
 */
public interface TokenDao {

	/** 常量，表示一个key永不过期 (在一个key被标注为永远不过期时返回此值) */
	long EXPIRE_NEVER = -1;

	/** 常量，表示系统中不存在这个缓存 (在对不存在的key获取剩余存活时间时返回此值) */
	long EXPIRE_NOT_EXIST = -2;
	/** 常量,默认设置的保存时间为1天 */
	long EXPIRE_ONE_DAY = 3600 * 24;


	// --------------------- 对象读写 ---------------------

	/**
	 * 获取Object，如无返空
	 *
	 * @param key 键名称
	 * @return object
	 */
	Object getUserInfo(String key);

	/**
	 * 写入Object，并设定存活时间 (单位: 秒)
	 *
	 * @param key     键名称,即用户token
	 * @param userInfo  值
	 * @param timeout 存活时间 (值大于0时限时存储，值=-1时永久存储，值=0或小于-2时不存储)
	 */
	void setUserInfo(String key, UserInfo userInfo, long timeout);

	/**
	 * 更新Object (过期时间不变)
	 *
	 * @param key    键名称
	 * @param userInfo 值
	 */
	void updateUserInfo(String key, UserInfo userInfo);

	/**
	 * 删除Object
	 *
	 * @param key 键名称
	 */
	void deleteUserInfo(String key);

	/**
	 * 获取Object的剩余存活时间 (单位: 秒)
	 *
	 * @param key 指定key
	 * @return 这个key的剩余存活时间
	 */
	long getTimeout(String key);

	/**
	 * 修改Object的剩余存活时间 (单位: 秒)
	 *
	 * @param key     指定key
	 * @param timeout 过期时间
	 */
	void updateTimeout(String key, long timeout);

	// --------------------- 控制用户拥有的token ---------------------

	/**
	 * 根据用户的id去查询锁拥有的token数量
	 *
	 * @param id
	 * @return
	 */
	List<String> getTokenListById(Long id);

	/**
	 * 设置用户拥有的token集合
	 *
	 * @param id
	 * @param tokenList
	 */
	void setTokenListById(Long id, List<String> tokenList);

}
