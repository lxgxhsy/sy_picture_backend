package com.example.sypicturebackend.manager;


import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.example.sypicturebackend.config.CosClientConfig;
import com.example.sypicturebackend.exception.BusinessException;
import com.example.sypicturebackend.exception.ErrorCode;
import com.example.sypicturebackend.exception.ThrowUtils;
import com.example.sypicturebackend.model.dto.file.UploadPictureResult;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @Author: sy
 * @CreateTime: 2024-12-19
 * @Description:
 * @Version: 1.0
 */

@Service
@Slf4j
@Deprecated()
public class FileManager {

	@Resource
	private CosClientConfig cosClientConfig;
	@Resource
	private CosManager cosManager;

	/**
	 * 上传图片
	 * @param fileUrl 文件
	 * @param uploadPathPrefix 上传路径前缀
	 * @return
	 */
	public UploadPictureResult  uploadPicture(String fileUrl, String uploadPathPrefix){
		// 校验图片
//		validPicture(multipartFile);
		validPicture(fileUrl);
		// 图片上传地址
		String uuid = RandomUtil.randomString(16);
//		String originalFilename = multipartFile.getOriginalFilename();
		String originalFilename = FileUtil.mainName(fileUrl);
		// 自己拼接文件上传路径，而不是使用原始文件名称，可以增强安全性
		String uploadFilename = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid,
				FileUtil.getSuffix(originalFilename));
		String uploadPath = String.format("/%s/%s", uploadPathPrefix, uploadFilename);
		File file = null;
		try {
			// 上传文件
			file = File.createTempFile(uploadPath, null);
			HttpUtil.downloadFile(fileUrl,file);
//			multipartFile.transferTo(file);
			PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, file);
			//获取图片信息对象
			ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
			//计算宽高
			int picWidth = imageInfo.getWidth();
			int picHeight = imageInfo.getHeight();
			double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
			// 封装返回结果
			UploadPictureResult uploadPictureResult = new UploadPictureResult();
			uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + uploadPath);
			uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
			uploadPictureResult.setPicSize(FileUtil.size(file));
			uploadPictureResult.setPicWidth(picWidth);
			uploadPictureResult.setPicHeight(picHeight);
			uploadPictureResult.setPicScale(picScale);
			uploadPictureResult.setPicFormat(imageInfo.getFormat());
			
			return uploadPictureResult;
		} catch (Exception e) {
			log.error("图片上传到对象存储失败", e);
			throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
		} finally {
			// 临时文件清理
			this.deleteTempFile(file);
		}
	}

	/**
	 * 校验 url 格式
	 * @param fileUrl
	 */
	private void validPicture(String fileUrl) {
		ThrowUtils.throwIf(StrUtil.isBlank(fileUrl), ErrorCode.PARAMS_ERROR, "文件地址不能为空");

		try {
			// 1. 验证 URL 格式
			new URL(fileUrl); // 验证是否是合法的 URL
		} catch (MalformedURLException e) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件地址格式不正确");
		}

		// 2. 校验 URL 协议
		ThrowUtils.throwIf(!(fileUrl.startsWith("http://") || fileUrl.startsWith("https://")),
				ErrorCode.PARAMS_ERROR, "仅支持 HTTP 或 HTTPS 协议的文件地址");

		// 3. 发送 HEAD 请求以验证文件是否存在
		HttpResponse response = null;
		try {
			response = HttpUtil.createRequest(Method.HEAD, fileUrl).execute();
			// 未正常返回，无需执行其他判断
			if (response.getStatus() != HttpStatus.HTTP_OK) {
				return;
			}
			// 4. 校验文件类型
			String contentType = response.header("Content-Type");
			if (StrUtil.isNotBlank(contentType)) {
				// 允许的图片类型
				final List<String> ALLOW_CONTENT_TYPES = Arrays.asList("image/jpeg", "image/jpg", "image/png", "image/webp");
				ThrowUtils.throwIf(!ALLOW_CONTENT_TYPES.contains(contentType.toLowerCase()),
						ErrorCode.PARAMS_ERROR, "文件类型错误");
			}
			// 5. 校验文件大小
			String contentLengthStr = response.header("Content-Length");
			if (StrUtil.isNotBlank(contentLengthStr)) {
				try {
					long contentLength = Long.parseLong(contentLengthStr);
					final long TWO_MB = 2 * 1024 * 1024L; // 限制文件大小为 2MB
					ThrowUtils.throwIf(contentLength > TWO_MB, ErrorCode.PARAMS_ERROR, "文件大小不能超过 2M");
				} catch (NumberFormatException e) {
					throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小格式错误");
				}
			}
		} finally {
			if (response != null) {
				response.close();
			}
		}
	}


	/**
	 * 校验文件
	 * @param multipartFile
	 */
	private void validPicture(MultipartFile multipartFile){
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

	/**
	 * 清理临时文件
	 *
	 * @param file
	 */
	public void deleteTempFile(File file){
		if (file == null){
			return ;
		}
		//删除临时文件
		boolean deleteResult = file.delete();
		if(!deleteResult){
			log.error("file delete error, filepath = {}",file.getAbsolutePath());
		}
	}
}
