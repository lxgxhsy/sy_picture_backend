package com.example.sypicturebackend.api.imagesearch.sub;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSONUtil;
import com.example.sypicturebackend.exception.BusinessException;
import com.example.sypicturebackend.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: sy
 * @CreateTime: 2024-12-24
 * @Description: 获取以图搜图的页面地址 step 1
 * @Version: 1.0
 */

@Slf4j
public class GetImagePageUrlApi {

	/**
	 * 获取以图搜图页面地址
	 *
	 * @param imageUrl
	 * @return
	 */
	public static String getImagePageUrl(String imageUrl) {
		// image: https%3A%2F%2Fwww.codefather.cn%2Flogo.png
		//tn: pc
		//from: pc
		//image_source: PC_UPLOAD_URL
		//sdkParams:
		// 1. 准备请求参数
		Map<String, Object> formData = new HashMap<>();
		formData.put("image", imageUrl);
		formData.put("tn", "pc");
		formData.put("from", "pc");
		formData.put("image_source", "PC_UPLOAD_URL");
//		String token = "1739853027605_1739882595697_rSglEBplOTq7XCTtEYtkiC/3FyCL+oABKXZck+RePr16Ds3HcdisGl87qv/HtipW4EEetarW9UC1ERU56E39a1qH5DREuNuMmVVT1WEW6QnJ5D4QNJy7GLSeLo99sV7o2RnCDzI1G7G/oUfiNDx1lnkGdmTGgJH0Y6bopJ+PB9Bc9zxLW+wPo7KjxwQ4MLKYSk6p6xJcHgfMQr/ERVeFYermEcw++Y0WCs2hbukz64mMMHldAlx9jxBKBcWrFHoKDQNkSm6ND1SBQhwKPGhGZ4gaKOJu4prdfCd4VuAsyFxDSSgPgG4qqxyvb5VGLPoK+n248y5Y4tgZkgPO9KT/Yz/IqwrbtD2mTOIpxNk6ZrUmo0GUi/grXOD9wRYEsxZAohKHSHnWV62Szx9kXBj5AEaxzwWYlu9Y/QjFpTVJ0bk=";
		// 获取当前时间戳
		long uptime = System.currentTimeMillis();
		// 请求地址
		String url = "https://graph.baidu.com/upload?uptime=" + uptime ;
		try {
			// 2. 发送请求
			HttpResponse httpResponse = HttpRequest.post(url)
					.timeout(5000)
					.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:135.0) Gecko/20100101 Firefox/135.0")
					.header("Accept", "*/*")
					.header("Accept-Language", "zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2")
					.header("Accept-Encoding", "gzip, deflate, br, zstd")
					.header("X-Requested-With", "XMLHttpRequest")
					.header("Content-Type", "multipart/form-data")
					.header("Origin", "https://graph.baidu.com")
					.header("Referer", "https://graph.baidu.com/pcpage/index")
					.header("Acs-token","")
					.form(formData)
					.execute();
			if (httpResponse.getStatus() != HttpStatus.HTTP_OK) {
				throw new BusinessException(ErrorCode.OPERATION_ERROR, "接口调用失败");
			}
			// 解析响应
			// {"status":0,"msg":"Success","data":{"url":"https://graph.baidu.com/sc","sign":"1262fe97cd54acd88139901734784257"}}
			String body = httpResponse.body();
			Map<String, Object> result = JSONUtil.toBean(body, Map.class);
			// 3. 处理响应结果
			if (result == null || !Integer.valueOf(0).equals(result.get("status"))) {
				throw new BusinessException(ErrorCode.OPERATION_ERROR, "接口调用失败");
			}
			Map<String, Object> data = (Map<String, Object>) result.get("data");
			// 对 URL 进行解码
			String rawUrl = (String) data.get("url");
			String searchResultUrl = URLUtil.decode(rawUrl, StandardCharsets.UTF_8);
			// 如果 URL 为空
			if (StrUtil.isBlank(searchResultUrl)) {
				throw new BusinessException(ErrorCode.OPERATION_ERROR, "未返回有效的结果地址");
			}
			return searchResultUrl;
		} catch (Exception e) {
			log.error("调用百度以图搜图接口失败", e);
			throw new BusinessException(ErrorCode.OPERATION_ERROR, "搜索失败");
		}
	}

	public static void main(String[] args) {
		// 测试以图搜图功能
		String imageUrl = "https://sy-1317828101.cos.ap-nanjing.myqcloud.com//public/1869399700336652289/2024-12-19_D66y5yulzXfOIQYj.png";
		String searchResultUrl = getImagePageUrl(imageUrl);
		System.out.println("搜索成功，结果 URL：" + searchResultUrl);
		//https://graph.baidu.com/s?card_key=&entrance=GENERAL&extUiData%5BisLogoShow%5D=1&f=all&isLogoShow=1&session_id=10190978132341691604&sign=126314bd7657f237d822501739882597&tpl_from=pc
	}
}
