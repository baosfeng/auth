package xyz.bsfeng.auth.utils;


import java.util.Random;

public class AuthStringUtils {

	private static final String randomStr = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
	private static final Random random = new Random();

	public static boolean isEmpty(String string) {
		return string == null || string.trim().isEmpty();
	}

	public static boolean isNotEmpty(String string) {
		return !isEmpty(string);
	}

	public static String randomString(int length) {
		if (length < 0) {
			throw new IllegalArgumentException("生成的随机字符串不能小于0!");
		}
		StringBuilder builder = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			builder.append(randomStr.charAt(random.nextInt(randomStr.length())));
		}
		return builder.toString();
	}

	public static String mixStr(int length) {
		if (length <= 0) {
			return "";
		}
		StringBuilder builder = new StringBuilder(length);
		while (length-- > 0) {
			builder.append("*");
		}
		return builder.toString();
	}

}
