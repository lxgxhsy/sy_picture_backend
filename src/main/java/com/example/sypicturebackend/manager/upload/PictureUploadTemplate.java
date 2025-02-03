package com.example.sypicturebackend.manager.upload;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.example.sypicturebackend.config.CosClientConfig;
import com.example.sypicturebackend.exception.BusinessException;
import com.example.sypicturebackend.exception.ErrorCode;
import com.example.sypicturebackend.manager.CosManager;
import com.example.sypicturebackend.model.dto.file.UploadPictureResult;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.CIObject;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.model.ciModel.persistence.ProcessResults;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.io.File;
import java.util.Date;
import java.util.List;


/**
 * 上传图片 模板模式
 * @author 诺诺
 */

@Slf4j
public abstract class PictureUploadTemplate {
	@Resource
	private CosClientConfig cosClientConfig;
	@Resource
	private CosManager cosManager;


	/**
	 * 模板方法，定义上传流程
	 */
	public UploadPictureResult uploadPicture(Object inputSource, String uploadPathPrefix){
		// 校验图片
		validPicture(inputSource);

		// 图片上传地址
		String uuid = RandomUtil.randomString(16);
		String originFilename = getOriginFilename(inputSource);


		// 自己拼接文件上传路径，而不是使用原始文件名称，可以增强安全性
		String uploadFilename = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid,
				FileUtil.getSuffix(originFilename));
		//判断上传路径后面有没有格式

		;
		String uploadPath = String.format("/%s/%s", uploadPathPrefix, uploadFilename);
		File file = null;
		try {
			// 上传文件
			file = File.createTempFile(uploadPath, null);

			// 处理文件来源 本地或者URL
			processFile(inputSource, file);
			//上传图片到对象存储
			PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, file);
            //获取图片信息对象 封装返回结果
			ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            //获取到图片处理结果
			ProcessResults processResults = putObjectResult.getCiUploadResult().getProcessResults();
			List<CIObject> objectList = processResults.getObjectList();
			if(CollUtil.isNotEmpty(objectList)){
				//获取压缩以后得到的文件信息
				CIObject compressedCiObject = objectList.get(0);
				// 缩略图默认等于压缩图
				CIObject thumbnailCiObject = compressedCiObject;
				//有生成缩略图 才获取压缩图
				if(objectList.size() > 1){
					thumbnailCiObject = objectList.get(1);
				}
				//封装压缩图的结果
				return buildResult(originFilename, compressedCiObject , thumbnailCiObject, imageInfo);
			}
			//封装返回结果
			return buildResult(originFilename, file, uploadPath, imageInfo);
		} catch (Exception e) {
			log.error("图片上传到对象存储失败", e);
			throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
		} finally {
			// 临时文件清理
			this.deleteTempFile(file);
		}
	}

	/**
	 * 封装返回结果
	 * @param originFilename 原始文件名
	 * @param compressedCiObject 压缩后的对象
	 * @param thumbnailCiObject 缩略图对象
	 * @param imageInfo 数据万象结果
	 * @return
	 */
	private UploadPictureResult  buildResult(String originFilename, CIObject compressedCiObject, CIObject thumbnailCiObject, ImageInfo imageInfo){
		UploadPictureResult uploadPictureResult = new UploadPictureResult();
		//计算宽高
		int picWidth = compressedCiObject.getWidth();
		int picHeight = compressedCiObject.getHeight();
		double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
		// 封装返回结果
		uploadPictureResult.setPicName(FileUtil.mainName(originFilename));
		uploadPictureResult.setPicWidth(picWidth);
		uploadPictureResult.setPicHeight(picHeight);
		uploadPictureResult.setPicScale(picScale);
		uploadPictureResult.setPicFormat(compressedCiObject.getFormat());
		uploadPictureResult.setPicSize(compressedCiObject.getSize().longValue());
		uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + compressedCiObject.getKey());
		// 设置缩略图
		uploadPictureResult.setThumbnailUrl(cosClientConfig.getHost() + "/" + thumbnailCiObject.getKey());
		return uploadPictureResult;
	};


	/**
	 * 封装返回结果
	 * @param originFilename
	 * @param file
	 * @param uploadPath
	 * @param imageInfo
	 * @return
	 */
	private UploadPictureResult buildResult(String originFilename, File file, String uploadPath, ImageInfo imageInfo) {
		UploadPictureResult uploadPictureResult = new UploadPictureResult();
		//计算宽高
		int picWidth = imageInfo.getWidth();
		int picHeight = imageInfo.getHeight();
		double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
		// 封装返回结果
		uploadPictureResult.setPicName(FileUtil.mainName(originFilename));
		uploadPictureResult.setPicWidth(picWidth);
		uploadPictureResult.setPicHeight(picHeight);
		uploadPictureResult.setPicScale(picScale);
		uploadPictureResult.setPicColor(imageInfo.getAve());
		uploadPictureResult.setPicFormat(imageInfo.getFormat());
		uploadPictureResult.setPicSize(FileUtil.size(file));
		uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + uploadPath);
		return uploadPictureResult;
	}

	/**
	 * 校验输入源（本地文件或 URL）
	 * @param inputSource 文件来源
	 */
	protected abstract void validPicture(Object inputSource);


	/**
	 * 获取输入源的原始文件名
	 * @param inputSource 文件来源
	 * @return
	 */
	protected abstract String getOriginFilename(Object inputSource);


	/**
	 * 处理输入源并生成本地临时文件
	 * @param inputSource 文件来源
	 * @param file 文件
	 * @throws Exception 抛出异常
	 */
	protected abstract void processFile(Object inputSource, File file) throws Exception;


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
