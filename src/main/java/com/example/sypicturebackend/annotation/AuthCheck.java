package com.example.sypicturebackend.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author: shiyong
 * @CreateTime: 2024-12-19
 * @Description:
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthCheck {

	/**
	 * 必须有一个角色
	 * @return
	 */
	String mustRole() default "";

}
