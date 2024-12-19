package com.example.sypicturebackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: sy
 * @CreateTime: 2024-12-19
 * @Description: 用户登录请求
 * @Version: 1.0
 */

@Data
public class UserLoginRequest implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 账号
	 */
	private String userAccount;

	/**
	 * 密码
	 */
	private String userPassword;
}
