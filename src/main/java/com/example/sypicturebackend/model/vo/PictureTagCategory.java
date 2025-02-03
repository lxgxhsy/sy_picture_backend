package com.example.sypicturebackend.model.vo;

import lombok.Data;

import java.util.List;

/**
 * @Author: sy
 * @CreateTime: 2024-12-19
 * @Description:
 * @Version: 1.0
 */


@Data
public class PictureTagCategory {

	/**
	 * 标签列表
	 */
	private List<String> tagList;

	/**
	 * 分类列表
	 */
	private List<String> categoryList;
 }
