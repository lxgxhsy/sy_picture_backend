package com.example.sypicturebackend.exception;

/**
 * @Author: sy
 * @CreateTime: 2024-12-09
 * @Description: 抛异常工具类
 * @Version: 1.0
 */


public class ThrowUtils {
	/**
	 * 条件成立抛出异常
	 * @param condition 条件
	 * @param runtimeException 运行时错误
	 */
	public static void throwIf(boolean condition, RuntimeException runtimeException){
		if(condition){
			throw runtimeException;
		}
	}

	/**
	 * 条件成立抛出异常
	 * @param condition 异常情况
	 * @param errorCode 错误码
	 */
	public static void throwIf(boolean condition, ErrorCode errorCode){
		throwIf(condition, new BusinessException(errorCode));
	}


	/**
	 * 条件成立抛出异常
	 * @param condition 条件
	 * @param errorCode 错误码
	 * @param message 错误消息
	 */
	public static void throwIf(boolean condition, ErrorCode errorCode,String message){
		throwIf(condition, new BusinessException(errorCode, message));
	}
}
