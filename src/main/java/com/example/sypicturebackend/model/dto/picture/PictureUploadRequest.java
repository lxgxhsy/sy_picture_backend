package com.example.sypicturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: sy
 * @CreateTime: 2024-12-19
 * @Description:
 * @Version: 1.0
 */

@Data
public class PictureUploadRequest implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 图片id 用于修改
	 */
	private Long id;
}