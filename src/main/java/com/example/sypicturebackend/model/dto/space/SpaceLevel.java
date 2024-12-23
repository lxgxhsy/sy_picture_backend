package com.example.sypicturebackend.model.dto.space;

import lombok.Data;
import lombok.AllArgsConstructor;

/**
 * @Author: sy
 * @CreateTime: 2024-12-23
 * @Description: 空间级别
 * @Version: 1.0
 */

@Data
@AllArgsConstructor
public class SpaceLevel {

	/**
	 * 值
	 */
	private int value;

	/**
	 * 中文
	 */
	private String text;


	/**
	 * 空间图片的最大总大小
	 */
	private Long maxSize;

	/**
	 * 空间图片的最大数量
	 */
	private Long maxCount;


}
