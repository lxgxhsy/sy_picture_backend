package com.example.sypicturebackend.model.dto.spaceuser;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: shiyong
 * @CreateTime: 2025-01-04
 * @Description:
 * @Version: 1.0
 */

@Data
public class SpaceUserQueryRequest implements Serializable {

	/**
	 * ID
	 */
	private Long id;

	/**
	 * 空间ID
	 */
	private Long spaceId;

	/**
	 * 用户ID
	 */
	private Long userId;

	/**
	 * 空间角色：viewer/editor/admin
	 */
	private String spaceRole;


    private static final long serialVersionUID = 1L;
}
