package xyz.bsfeng.auth.dao;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Token持久层接口 [默认实现类, 基于内存Map]
 *
 * @author bsfeng
 */
public class TokenDaoDefaultImpl implements TokenDao {

	/**
	 * 数据集合
	 */
	public Map<String, UserInfo> dataMap = new ConcurrentHashMap<>();

	/**
	 * 过期时间集合 (单位: 毫秒) , 记录所有key的到期时间 [注意不是剩余存活时间]
	 */
	public Map<String, Long> expireMap = new ConcurrentHashMap<>();



	// ------------------------ Object 读写操作 

	@Override
	public UserInfo getUserInfo(String key) {
		clearKeyByTimeout(key);
		return dataMap.get(key);
	}

	@Override
	public void setUserInfo(String key, UserInfo userInfo, long timeout) {
		if (timeout == 0 || timeout <= EXPIRE_NOT_EXIST) {
			return;
		}
		dataMap.put(key, userInfo);
		expireMap.put(key, (timeout == EXPIRE_NEVER) ? (EXPIRE_NEVER) : (System.currentTimeMillis() + timeout * 1000));
	}

	@Override
	public void updateUserInfo(String key, UserInfo userInfo) {
		long expireTime = getKeyTimeout(key);
		if (expireTime == EXPIRE_NOT_EXIST || expireTime == EXPIRE_NEVER) {
			return;
		}
		dataMap.put(key, userInfo);
		updateTimeout(key, EXPIRE_ONE_DAY);
	}

	@Override
	public void deleteUserInfo(String key) {
		dataMap.remove(key);
		expireMap.remove(key);
	}
	// ------------------------ 过期时间相关操作

	@Override
	public long getTimeout(String key) {
		return getKeyTimeout(key);
	}

	@Override
	public void updateTimeout(String key, long timeout) {
		expireMap.put(key, System.currentTimeMillis() + timeout * 1000);
	}


	// --------------------- 定时清理过期数据  

	/**
	 * 如果指定key已经过期，则立即清除它
	 *
	 * @param key 指定key
	 */
	void clearKeyByTimeout(String key) {
		Long expirationTime = expireMap.get(key);
		// 清除条件：如果不为空 && 不是[永不过期] && 已经超过过期时间
		if (expirationTime != null && expirationTime != EXPIRE_NEVER && expirationTime < System.currentTimeMillis()) {
			dataMap.remove(key);
			expireMap.remove(key);
		}
	}

	/**
	 * 获取指定key的剩余存活时间 (单位：秒)
	 */
	long getKeyTimeout(String key) {
		// 先检查是否已经过期
		clearKeyByTimeout(key);
		// 获取过期时间
		Long expire = expireMap.get(key);
		// 如果根本没有这个值
		if (expire == null) {
			return EXPIRE_NOT_EXIST;
		}
		// 如果被标注为永不过期
		if (expire == EXPIRE_NEVER) {
			return EXPIRE_NEVER;
		}
		// ---- 计算剩余时间并返回
		long timeout = (expire - System.currentTimeMillis()) / 1000;
		// 小于零时，视为不存在
		if (timeout < 0) {
			dataMap.remove(key);
			expireMap.remove(key);
			return EXPIRE_NOT_EXIST;
		}
		return timeout;
	}

	/**
	 * 清理所有已经过期的key
	 */
	public void refreshDataMap() {
		for (String s : expireMap.keySet()) {
			clearKeyByTimeout(s);
		}
	}

}
