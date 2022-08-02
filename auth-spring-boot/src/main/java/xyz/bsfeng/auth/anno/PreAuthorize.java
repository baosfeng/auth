package xyz.bsfeng.auth.anno;

import org.springframework.core.annotation.Order;

import java.lang.annotation.*;

/**
 * 检查是否存在指定的权限，如果没有就会抛出异常
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Order
public @interface PreAuthorize {

	/**
	 * 拥有的角色集合列表
	 * @return
	 */
	String[] hasRole() default "";

	/**
	 * 检查是否拥有对应的资源权限
	 * @return
	 */
	String[] hasAuth() default "";
}
