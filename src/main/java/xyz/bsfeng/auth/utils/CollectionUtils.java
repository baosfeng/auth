package xyz.bsfeng.auth.utils;

import java.util.Collection;

/**
 * @author bsfeng
 * @date 2021/9/1 16:28
 */
public class CollectionUtils {
	public static boolean isEmpty(Collection<?> collection) {
		return collection == null || collection.isEmpty();
	}

	public static boolean isNotEmpty(Collection<?> collection) {
		return !isEmpty(collection);
	}
}
