package com.example.sypicturebackend.model.vo;

import lombok.Data;

import java.util.Date;

/**
 * @Author: sy
 * @CreateTime: 2024-12-19
 * @Description: 用户视图 脱敏
 * @Version: 1.0
 */

@Data
public class UserVO {



	private Long id;

	/**
	 * 账号
	 */
	private String userAccount;


	/**
	 * 用户昵称
	 */
	private String userName;

	/**
	 * 用户头像
	 */
	private String userAvatar;

	/**
	 * 用户简介
	 */
	private String userProfile;

	/**
	 * 用户角色：user/admin
	 */
	private String userRole;

	/**
	 * 编辑时间
	 */
	private Date editTime;


	/**
	 * 创建时间
	 */
	private Date createTime;

	private static final long serialVersionUID = 1L;

}
