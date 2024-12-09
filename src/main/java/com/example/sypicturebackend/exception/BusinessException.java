package com.example.sypicturebackend.exception;

import lombok.Getter;

/**
 * @author sy
 *  业务异常
 * @version: 1.0
 */

@Getter
public class BusinessException extends RuntimeException{
	/**
	 * 错误码
	 */
	private final int code;


	public BusinessException(int code, String message) {
		super(message);
		this.code = code;
	}

	public BusinessException(ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.code = errorCode.getCode();
	}

	public BusinessException(ErrorCode errorCode, String message) {
		super(message);
		this.code = errorCode.getCode();
	}

	public int getCode() {
		return code;
	}
}
