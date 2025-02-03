package com.example.sypicturebackend.model.dto.space;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: sy
 * @CreateTime: 2024-12-23
 * @Description: 编辑空间请求
 * @Version: 1.0
 */

@Data
public class SpaceEditRequest implements Serializable {

	/**
	 * 空间 id
	 */
	private Long id;
	/**
	 * 空间名称
	 */
	private String spaceName;
	private static final long serialVersionUID = 1L;

}
