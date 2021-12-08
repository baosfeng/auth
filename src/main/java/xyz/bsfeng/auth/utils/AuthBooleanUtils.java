package xyz.bsfeng.auth.utils;

/**
 * @author bsfeng
 * @date 2021/9/2 10:08
 */
public class AuthBooleanUtils {
	private AuthBooleanUtils() {
	}

	public static boolean isTrue(Boolean boolValue) {
		return boolValue != null && boolValue;
	}

	public static boolean isFalse(Boolean boolValue) {
		return !isTrue(boolValue);
	}

}
