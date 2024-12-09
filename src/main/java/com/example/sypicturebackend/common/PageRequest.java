package com.example.sypicturebackend.common;


import com.example.sypicturebackend.constant.CommonConstant;
import lombok.Data;

/**
 * @author 诺诺
 */
@Data
public class PageRequest {

	/**
	 * 当前页号
	 */
	private int current = 1;

	/**
	 * 页面大小
	 */
	private int pageSize = 1;


	/**
	 * 排序字段
	 */
	private String sortField;

	/**
	 * 排序顺序（默认升序）
	 */
	private String sortOrder = CommonConstant.SORT_ORDER_ASC;
}
