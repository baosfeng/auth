package xyz.bsfeng.test;

import xyz.bsfeng.auth.utils.AuthStringUtils;

/**
 * @author bsfeng
 * @date 2022/8/2-13:33
 */
public class StringUtils {

	public static boolean isEmpty(String str) {
		return AuthStringUtils.isEmpty(str) || "null".equals(str);
	}

	public static boolean isNotEmpty(String str) {
		return !isEmpty(str);
	}
}
