package com.example.sypicturebackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: sy
 * @CreateTime: 2024-12-19
 * @Description: 用户更新信息
 * @Version: 1.0
 */

@Data
public class UserUpdateRequest implements Serializable {

	/**
	 * id
	 */
	private Long id;

	/**
	 * 用户昵称
	 */
	private String userName;

	/**
	 * 用户头像
	 */
	private String userAvatar;

	/**
	 * 简介
	 */
	private String userProfile;

	/**
	 * 用户角色：user/admin
	 */
	private String userRole;

	private static final long serialVersionUID = 1L;
}
