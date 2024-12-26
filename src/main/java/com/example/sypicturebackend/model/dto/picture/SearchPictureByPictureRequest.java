package com.example.sypicturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: sy
 * @CreateTime: 2024-12-26
 * @Description:
 * @Version: 1.0
 */

@Data
public class SearchPictureByPictureRequest implements Serializable {



	/**
	 * 图片 id
	 */
	private Long pictureId;

	private static final long serialVersionUID = 1L;
}
