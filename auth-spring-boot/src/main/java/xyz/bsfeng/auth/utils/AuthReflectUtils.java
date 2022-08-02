package xyz.bsfeng.auth.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * @author bsfeng
 * @date 2021/8/26 14:31
 */
public class AuthReflectUtils {

	public static <T> HashSet<Field> getFields(Class<T> clazz) {
		Field[] declaredFields = clazz.getDeclaredFields();
		HashSet<Field> fields = Arrays.stream(declaredFields).collect(Collectors.toCollection(HashSet::new));
		Class<?> aClass = (Class<?>) clazz.getGenericSuperclass();
		while (aClass != null) {
			declaredFields = aClass.getDeclaredFields();
			fields.addAll(Arrays.stream(declaredFields).collect(Collectors.toList()));
			Class<?> newClazz = (Class<?>) clazz.getGenericSuperclass();
			if (newClazz == aClass) {
				break;
			}
			aClass = newClazz;
		}
		return fields;
	}

	public static Method getMethodByField(Class<?> clazz, Field field) throws NoSuchMethodException {
		String name = field.getName();
		String methodName = "get" + name.substring(0, 1).toUpperCase() + name.substring(1);
		return clazz.getDeclaredMethod(methodName);
	}
}
