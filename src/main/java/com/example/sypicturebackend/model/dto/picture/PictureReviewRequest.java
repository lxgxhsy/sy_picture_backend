package com.example.sypicturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: sy
 * @CreateTime: 2024-12-20
 * @Description: 图片审核理由
 * @Version: 1.0
 */

@Data
public class PictureReviewRequest implements Serializable {

	private static final long serialVersionUID = 1L;


	/**
	 * id
	 */
	private Long id;

	/**
	 * 状态：0-待审核, 1-通过, 2-拒绝
	 */
	private Integer reviewStatus;

	/**
	 * 审核信息
	 */
	private String reviewMessage;
}
