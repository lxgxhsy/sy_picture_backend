package com.example.sypicturebackend.api.imagesearch.model;

import lombok.Data;

/**
 * @Author: sy
 * @CreateTime: 2024-12-24
 * @Description: 图片搜索结果
 * @Version: 1.0
 */

@Data
public class ImageSearchResult {

	/**
	 *  缩略图地址
	 */
	private String thumbUrl;

	/**
	 *  来源地址
	 */
	private String fromUrl;

}
