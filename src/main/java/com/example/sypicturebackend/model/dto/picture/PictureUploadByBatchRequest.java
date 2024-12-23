package com.example.sypicturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: sy
 * @CreateTime: 2024-12-21
 * @Description:
 * @Version: 1.0
 */

@Data
public class PictureUploadByBatchRequest  implements Serializable {

	private static final long serialVersionUID = 1L;
	/**
	 * 搜索词
	 */
	private String searchText;

	/**
	 * 抓取数量
	 */
	private Integer count = 10;

	/**
	 * 名称前缀
	 */
	private String namePrefix;

}
