package com.example.sypicturebackend.manager.auth.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author: shiyong
 * @CreateTime: 2025-01-05
 * @Description: 空间成员角色
 * @Version: 1.0
 */

@Data
public class SpaceUserRole implements Serializable {

	/**
	 * 角色键
	 */
	private String key;

	/**
	 * 角色名称
	 */
	private String name;

	/**
	 * 权限键列表
	 */
	private List<String> permissions;

	/**
	 * 角色描述
	 */
	private String description;

	private static final long serialVersionUID = 1L;
}
