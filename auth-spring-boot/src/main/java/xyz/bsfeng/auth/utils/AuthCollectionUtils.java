package xyz.bsfeng.auth.utils;

import java.util.Collection;
import java.util.Map;

/**
 * @author bsfeng
 * @date 2021/9/1 16:28
 */
public class AuthCollectionUtils {
	public static boolean isEmpty(Collection<?> collection) {
		return collection == null || collection.isEmpty();
	}

	public static boolean isNotEmpty(Collection<?> collection) {
		return !isEmpty(collection);
	}

	public static boolean isEmpty(Map<?, ?> map) {
		return map == null || map.isEmpty();
	}

	public static boolean isNotEmpty(Map<?, ?> map) {
		return !isEmpty(map);
	}
}
