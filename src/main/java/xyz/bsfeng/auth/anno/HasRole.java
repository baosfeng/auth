package xyz.bsfeng.auth.anno;

import java.lang.annotation.*;

/**
 * 检查是否存在指定的权限，如果没有就会抛出异常
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HasRole {

	/**
	 * 拥有的权限集合列表
	 * @return
	 */
	String[] value() default "";
}
