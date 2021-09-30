package xyz.bsfeng.auth.anno;

import org.springframework.core.annotation.Order;

import java.lang.annotation.*;

/**
 * 当前接口可不登录进行访问
 * 使用在类上表示当前类的所有接口全部可不登录进行访问
 *
 * @author bsfeng
 * @date 2021/9/22 14:34
 */
@Order
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface IgnoreLogin {
}
