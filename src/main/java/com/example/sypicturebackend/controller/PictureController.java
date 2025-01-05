package com.example.sypicturebackend.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.sypicturebackend.annotation.AuthCheck;
import com.example.sypicturebackend.api.aliyunai.AliYunAiApi;
import com.example.sypicturebackend.api.aliyunai.model.CreateOutPaintingTaskResponse;
import com.example.sypicturebackend.api.aliyunai.model.GetOutPaintingTaskResponse;
import com.example.sypicturebackend.api.imagesearch.ImageSearchApiFacade;
import com.example.sypicturebackend.api.imagesearch.model.ImageSearchResult;
import com.example.sypicturebackend.common.BaseResponse;
import com.example.sypicturebackend.common.DeleteRequest;
import com.example.sypicturebackend.common.ResultUtils;
import com.example.sypicturebackend.constant.UserConstant;
import com.example.sypicturebackend.exception.BusinessException;
import com.example.sypicturebackend.exception.ErrorCode;
import com.example.sypicturebackend.exception.ThrowUtils;
import com.example.sypicturebackend.manager.auth.SpaceUserAuthManager;
import com.example.sypicturebackend.manager.auth.StpKit;
import com.example.sypicturebackend.manager.auth.annotation.SaSpaceCheckPermission;
import com.example.sypicturebackend.manager.auth.model.SpaceUserPermissionConstant;
import com.example.sypicturebackend.model.dto.picture.*;
import com.example.sypicturebackend.model.entity.Picture;
import com.example.sypicturebackend.model.entity.Space;
import com.example.sypicturebackend.model.entity.User;
import com.example.sypicturebackend.model.enums.PictureReviewStatusEnum;
import com.example.sypicturebackend.model.vo.PictureTagCategory;
import com.example.sypicturebackend.model.vo.PictureVO;
import com.example.sypicturebackend.service.PictureService;
import com.example.sypicturebackend.service.SpaceService;
import com.example.sypicturebackend.service.UserService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Author: sy
 * @CreateTime: 2024-12-19
 * @Description: 改用 upload 包的服务
 * @Version: 1.0
 */

@Slf4j
@RestController
@RequestMapping("/picture")
public class PictureController {

	@Resource
	private UserService userService;

	@Resource
	private PictureService pictureService;

	@Resource
	private SpaceUserAuthManager spaceUserAuthManager;

	@Resource
	private SpaceService spaceService;

	@Resource
	private AliYunAiApi aliYunAiApi;

	@Resource
	private StringRedisTemplate stringRedisTemplate;

	/**
	 * 本地缓存 最大10000条 缓存五分钟后移除
	 */
	private final Cache<String, String> LOCAL_CACHE =
    Caffeine.newBuilder().initialCapacity(1024)
        .maximumSize(10000L).expireAfterWrite(5L, TimeUnit.MINUTES)
        .build();



	/**
	 * 上传图片（可重新上传）
	 */
	@PostMapping("/upload")
	@SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_UPLOAD)
	public BaseResponse<PictureVO> uploadPicture(
			@RequestPart("file") MultipartFile multipartFile,
			PictureUploadRequest pictureUploadRequest,
			HttpServletRequest request) {
		User loginUser = userService.getLoginUser(request);
		PictureVO pictureVO = pictureService.uploadPicture(multipartFile, pictureUploadRequest, loginUser);
		return ResultUtils.success(pictureVO);
	}

	/**
	 * 上传图片（可重新上传）
	 */
	@PostMapping("/upload/url")
	@SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_UPLOAD)
	public BaseResponse<PictureVO> uploadPictureByUrl(
			@RequestBody PictureUploadRequest pictureUploadRequest,
			HttpServletRequest request) {
		User loginUser = userService.getLoginUser(request);
		String fileUrl = pictureUploadRequest.getFileUrl();
		PictureVO pictureVO = pictureService.uploadPicture(fileUrl, pictureUploadRequest, loginUser);
		return ResultUtils.success(pictureVO);
	}

	/**
	 * 批量抓取图片
	 */
	@PostMapping("/upload/batch")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Integer> uploadPictureByBatch(
			@RequestBody PictureUploadByBatchRequest pictureUploadByBatchRequest,
			HttpServletRequest request) {
		ThrowUtils.throwIf(pictureUploadByBatchRequest == null, ErrorCode.PARAMS_ERROR);
		User loginUser = userService.getLoginUser(request);
		int uploadCount = pictureService.uploadPictureByBatch(pictureUploadByBatchRequest, loginUser);
		return ResultUtils.success(uploadCount);
	}

	/**
	 * 删除图片
	 */
	@PostMapping("/delete")
	@SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_DELETE)
	public BaseResponse<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
		if (deleteRequest == null || deleteRequest.getId() <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		User loginUser = userService.getLoginUser(request);
		pictureService.deletePicture(deleteRequest.getId(), loginUser);
		return ResultUtils.success(true);
	}

	/**
	 * 更新图片（仅管理员可用）
	 */
	@PostMapping("/update")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Boolean> updatePicture(@RequestBody PictureUpdateRequest pictureUpdateRequest, HttpServletRequest request) {
		if (pictureUpdateRequest == null || pictureUpdateRequest.getId() <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		// 将实体类和 DTO 进行转换
		Picture picture = new Picture();
		BeanUtils.copyProperties(pictureUpdateRequest, picture);
		// 注意将 list 转为 string
		picture.setTags(JSONUtil.toJsonStr(pictureUpdateRequest.getTags()));
		// 数据校验
		pictureService.validPicture(picture);
		// 判断是否存在
		long id = pictureUpdateRequest.getId();
		Picture oldPicture = pictureService.getById(id);
		ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
		User loginUser = userService.getLoginUser(request);
		//添加审核参数
		pictureService.fillReviewParams(oldPicture, loginUser);
		// 操作数据库
		boolean result = pictureService.updateById(picture);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
		return ResultUtils.success(true);
	}

	/**
	 * 根据 id 获取图片（仅管理员可用）
	 */
	@GetMapping("/get")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Picture> getPictureById(long id, HttpServletRequest request) {
		ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
		// 查询数据库
		Picture picture = pictureService.getById(id);
		ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
		// 获取封装类
		return ResultUtils.success(picture);
	}

	/**
	 * 根据 id 获取图片（封装类）
	 */
	@GetMapping("/get/vo")
	public BaseResponse<PictureVO> getPictureVOById(long id, HttpServletRequest request) {
		ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
		// 查询数据库
		// 查询数据库
		Picture picture = pictureService.getById(id);
		ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
           // 空间权限校验
		Space space = null;
		Long spaceId = picture.getSpaceId();
		if (spaceId != null) {
			boolean hasPermission = StpKit.SPACE.hasPermission(SpaceUserPermissionConstant.PICTURE_VIEW);
			ThrowUtils.throwIf(!hasPermission, ErrorCode.NO_AUTH_ERROR);
			space = spaceService.getById(spaceId);
			ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
		}
		// 获取权限列表
		User loginUser = userService.getLoginUser(request);
		List<String> permissionList = spaceUserAuthManager.getPermissionList(space, loginUser);
		PictureVO pictureVO = pictureService.getPictureVO(picture, request);
		pictureVO.setPermissionList(permissionList);
		// 获取封装类
		return ResultUtils.success(pictureService.getPictureVO(picture, request));
	}

	/**
	 * 分页获取图片列表（仅管理员可用）
	 */
	@PostMapping("/list/page")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Page<Picture>> listPictureByPage(@RequestBody PictureQueryRequest pictureQueryRequest) {
		long current = pictureQueryRequest.getCurrent();
		long size = pictureQueryRequest.getPageSize();
		// 查询数据库
		Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
				pictureService.getQueryWrapper(pictureQueryRequest));
		return ResultUtils.success(picturePage);
	}

	/**
	 * 分页获取图片列表（封装类）
	 */
	@PostMapping("/list/page/vo")
	public BaseResponse<Page<PictureVO>> listPictureVOByPage(@RequestBody PictureQueryRequest pictureQueryRequest,
	                                                         HttpServletRequest request) {
		long current = pictureQueryRequest.getCurrent();
		long size = pictureQueryRequest.getPageSize();
		// 限制爬虫
		ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
		// 普通用户只能看审核通过的图片
		pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
		// 空间权限校验
		Long spaceId = pictureQueryRequest.getSpaceId();

      // 公开图库
		if (spaceId == null) {
			// 普通用户默认只能查看已过审的公开数据
			pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
			pictureQueryRequest.setNullSpaceId(true);
		} else {
			boolean hasPermission = StpKit.SPACE.hasPermission(SpaceUserPermissionConstant.PICTURE_VIEW);
			ThrowUtils.throwIf(!hasPermission, ErrorCode.NO_AUTH_ERROR);
//			// 私有空间
//			User loginUser = userService.getLoginUser(request);
//			Space space = spaceService.getById(spaceId);
//			ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
//			if (!loginUser.getId().equals(space.getUserId())) {
//				throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有空间权限");
//			}
		}

		// 查询数据库
		Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
				pictureService.getQueryWrapper(pictureQueryRequest));
		// 获取封装类
		return ResultUtils.success(pictureService.getPictureVOPage(picturePage, request));
	}

	/**
	 * 分页获取图片列表（封装类）
	 */
	@Deprecated
	@PostMapping("/list/page/vo/cache")
	public BaseResponse<Page<PictureVO>> listPictureVOByPageWithCache(@RequestBody PictureQueryRequest pictureQueryRequest,
	                                                         HttpServletRequest request) {
		long current = pictureQueryRequest.getCurrent();
		long size = pictureQueryRequest.getPageSize();
		// 限制爬虫
		ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
		// 普通用户只能看审核通过的图片
		pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());

		// 查询缓存，缓存中没有，再查询数据库
		//构建缓存 key
		String queryCondition = JSONUtil.toJsonStr(pictureQueryRequest);
		String hashKey = DigestUtils.md5DigestAsHex(queryCondition.getBytes());
		String cacheKey = String.format("sypicture:listPictureVOByPage:%s", hashKey);

		//从本地缓存中查询
		String cachedValue  = LOCAL_CACHE.getIfPresent(cacheKey);
		if(cachedValue != null) {
			// 如果缓存命中 返回结果
			Page<PictureVO> cachedPage = JSONUtil.toBean(cachedValue, Page.class);
			return ResultUtils.success(cachedPage);
		}
		// 本地缓存未命中 查询 Redis 分布式缓存
		ValueOperations<String, String> opsForValue   = stringRedisTemplate.opsForValue();
		 cachedValue = opsForValue.get(cacheKey);
		 if(cachedValue != null){
		 	//缓存命中 更新本地缓存 返回结果
			 LOCAL_CACHE.put(cacheKey, cachedValue);
			 Page<PictureVO> cachedPage = JSONUtil.toBean(cachedValue, Page.class);
			 return ResultUtils.success(cachedPage);
		 }
		// 如果都没有命中 查询数据库
		Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
				pictureService.getQueryWrapper(pictureQueryRequest));
		// 获取封装类
		Page<PictureVO> pictureVOPage = pictureService.getPictureVOPage(picturePage, request);
        // 更新缓存
		// 存入 Redis 缓存
		String cacheValue = JSONUtil.toJsonStr(pictureVOPage);
		// 5 - 10 分钟随机过期，防止雪崩
		int cacheExpireTime = 300 +  RandomUtil.randomInt(0, 300);
		//存储空值 解决缓存穿透
		opsForValue.set(cacheKey, cacheValue, cacheExpireTime, TimeUnit.SECONDS);

		// 写入本地缓存
		LOCAL_CACHE.put(cacheKey, cacheValue);

		// 返回结果
		return ResultUtils.success(pictureVOPage);
	}


	/**
	 * 编辑图片（给用户使用）
	 */
	@PostMapping("/edit")
	@SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_EDIT)
	public BaseResponse<Boolean> editPicture(@RequestBody PictureEditRequest pictureEditRequest, HttpServletRequest request) {
		if (pictureEditRequest == null || pictureEditRequest.getId() <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		User loginUser = userService.getLoginUser(request);
		pictureService.editPicture(pictureEditRequest, loginUser);
		return ResultUtils.success(true);
	}

	@GetMapping("/tag_category")
	public BaseResponse<PictureTagCategory> listPictureTagCategory() {
		PictureTagCategory pictureTagCategory = new PictureTagCategory();
		List<String> tagList = Arrays.asList("热门", "搞笑", "生活", "高清", "艺术", "校园", "背景", "简历", "创意");
		List<String> categoryList = Arrays.asList("模板", "电商", "表情包", "素材", "海报");
		pictureTagCategory.setTagList(tagList);
		pictureTagCategory.setCategoryList(categoryList);

		return ResultUtils.success(pictureTagCategory);
	}

	/**
	 * 审核图片（仅管理员可用）
	 */
	@PostMapping("/review")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Boolean> doPictureReview(@RequestBody PictureReviewRequest pictureReviewRequest,
	                                                   HttpServletRequest request) {
		ThrowUtils.throwIf(pictureReviewRequest == null, ErrorCode.PARAMS_ERROR);
		User loginUser = userService.getLoginUser(request);
		pictureService.doPictureReview(pictureReviewRequest, loginUser);

		return ResultUtils.success(true);
	}

	/**
	 * 以图搜图
	 */
	@PostMapping("/search/picture")
	public BaseResponse<List<ImageSearchResult>> searchPictureByPicture(@RequestBody SearchPictureByPictureRequest searchPictureByPictureRequest) {
		ThrowUtils.throwIf(searchPictureByPictureRequest == null, ErrorCode.PARAMS_ERROR);
		Long pictureId = searchPictureByPictureRequest.getPictureId();
		ThrowUtils.throwIf(pictureId == null || pictureId <= 0, ErrorCode.PARAMS_ERROR);
		Picture picture = pictureService.getById(pictureId);
		ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
		// 因为只接受png的图片，所以加油吧
		List<ImageSearchResult> resultList = ImageSearchApiFacade.searchImage(picture.getThumbnailUrl());
		return ResultUtils.success(resultList);
	}

	/**
	 * 按照颜色搜图
	 */
	@PostMapping("/search/color")
	@SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_VIEW)
	public BaseResponse<List<PictureVO>> searchPictureByPicture(@RequestBody SearchPictureByColorRequest  searchPictureByColorRequest, HttpServletRequest request) {
		ThrowUtils.throwIf(searchPictureByColorRequest == null, ErrorCode.PARAMS_ERROR);
		Long spaceId = searchPictureByColorRequest.getSpaceId();
		String picColor = searchPictureByColorRequest.getPicColor();
        User loginUser = userService.getLoginUser(request);

		List<PictureVO> result = pictureService.searchPictureByColor(spaceId, picColor, loginUser);
		return ResultUtils.success(result);
	}

	/**
	 *      批量编辑图片
	 */
	@PostMapping("/edit/batch")
	@SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_EDIT)
	public BaseResponse<Boolean> editPictureByBatch(@RequestBody PictureEditByBatchRequest  pictureEditByBatchRequest, HttpServletRequest request) {
		ThrowUtils.throwIf(pictureEditByBatchRequest == null, ErrorCode.PARAMS_ERROR);
		User loginUser = userService.getLoginUser(request);
		pictureService.editPictureByBatch(pictureEditByBatchRequest, loginUser);
		return ResultUtils.success(true);
	}


	/**
	 * 创建 AI 扩图任务
	 */
	@PostMapping("/out_painting/create_task")
	@SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_EDIT)
	public BaseResponse<CreateOutPaintingTaskResponse> createPictureOutPaintingTask(@RequestBody CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest,
	                                                                                HttpServletRequest request) {
		if (createPictureOutPaintingTaskRequest == null || createPictureOutPaintingTaskRequest.getPictureId() == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		User loginUser = userService.getLoginUser(request);
		CreateOutPaintingTaskResponse response = pictureService.createPictureOutPaintingTask(createPictureOutPaintingTaskRequest, loginUser);
		return ResultUtils.success(response);
	}
	/**
	 * 查询 AI 扩图任务
	 */
	@GetMapping("/out_painting/get_task")
	public BaseResponse<GetOutPaintingTaskResponse> getPictureOutPaintingTask(String taskId) {
		ThrowUtils.throwIf(StrUtil.isBlank(taskId), ErrorCode.PARAMS_ERROR);
		GetOutPaintingTaskResponse task = aliYunAiApi.getOutPaintingTask(taskId);
		return ResultUtils.success(task);
	}

}