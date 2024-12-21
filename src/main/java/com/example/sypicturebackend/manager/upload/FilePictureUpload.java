package com.example.sypicturebackend.manager.upload;

import cn.hutool.core.io.FileUtil;
import com.example.sypicturebackend.exception.ErrorCode;
import com.example.sypicturebackend.exception.ThrowUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * @Author: sy
 * @CreateTime: 2024-12-21
 * @Description: 文件上传
 * @Version: 1.0
 */

@Service
public class FilePictureUpload extends PictureUploadTemplate {

	@Override
	protected void validPicture(Object inputSource) {
		MultipartFile multipartFile = (MultipartFile) inputSource;
		ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR,"文件不能为空");
		//校验文件大小
		long fileSize = multipartFile.getSize();
		final long ONE_M = 1024 * 1024;
		ThrowUtils.throwIf(fileSize > 2 * ONE_M, ErrorCode.PARAMS_ERROR,"文件大小不能超过2MB");
		//校验文件后缀
		String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
		//允许上传的文件后缀列表的集合
		final List<String> ALLOW_FORMAT_LIST = Arrays.asList("jpeg", "png", "jpg", "webp");
		ThrowUtils.throwIf(!ALLOW_FORMAT_LIST.contains(fileSuffix), ErrorCode.PARAMS_ERROR,"文件类型错误");
	}

	@Override
	protected String getOriginFilename(Object inputSource) {
		MultipartFile multipartFile = (MultipartFile) inputSource;

		return multipartFile.getOriginalFilename();
	}

	@Override
	protected void processFile(Object inputSource, File file) throws Exception {
		MultipartFile multipartFile = (MultipartFile) inputSource;
       multipartFile.transferTo(file);
	}
}
