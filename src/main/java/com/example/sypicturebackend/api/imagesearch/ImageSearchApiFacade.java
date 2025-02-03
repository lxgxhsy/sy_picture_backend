package com.example.sypicturebackend.api.imagesearch;

import com.example.sypicturebackend.api.imagesearch.model.ImageSearchResult;
import com.example.sypicturebackend.api.imagesearch.sub.GetImageFirstUrlApi;
import com.example.sypicturebackend.api.imagesearch.sub.GetImageListApi;
import com.example.sypicturebackend.api.imagesearch.sub.GetImagePageUrlApi;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @Author: sy
 * @CreateTime: 2024-12-26
 * @Description: 门面模式封装一下搜索请求
 * @Version: 1.0
 */

@Slf4j
public class ImageSearchApiFacade {

	/**
	 * 搜索图片
	 * @param imageUrl 图片url地址
	 * @return imageList
	 */
	public static List<ImageSearchResult> searchImage(String imageUrl){
		String imagePageUrl = GetImagePageUrlApi.getImagePageUrl(imageUrl);
		String imageFirstUrl = GetImageFirstUrlApi.getImageFirstUrl(imagePageUrl);
		List<ImageSearchResult> imageList = GetImageListApi.getImageList(imageFirstUrl);
		return imageList;
	}
	public static void main(String[] args) {
		List<ImageSearchResult> imageList = searchImage("https://www.codefather.cn/logo.png");
		System.out.println("结果列表" + imageList);
	}
}
