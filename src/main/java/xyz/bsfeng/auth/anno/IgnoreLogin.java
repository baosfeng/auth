package xyz.bsfeng.auth.anno;

import org.springframework.core.annotation.Order;

import java.lang.annotation.*;

/**
 * 当前接口可不登录进行访问
 *
 * @author bsfeng
 * @date 2021/9/22 14:34
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Order
public @interface IgnoreLogin {
}
