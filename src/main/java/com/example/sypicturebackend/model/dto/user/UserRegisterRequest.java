package com.example.sypicturebackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author 诺诺
 * @CreateTime: 2024-12-18
 * @Description: 用户注册请求
 * @Version: 1.0
 */

@Data
public class UserRegisterRequest implements Serializable {
	private static final long serialVersionUID = -6927281234425067355L;

	/**
	 * 账号
	 */
	private String userAccount;

	/**
	 * 密码
	 */
	private String userPassword;

	/**
	 * 确认密码
	 */
	private String checkPassword;

}
