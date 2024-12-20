package com.example.sypicturebackend.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sypicturebackend.exception.BusinessException;
import com.example.sypicturebackend.exception.ErrorCode;
import com.example.sypicturebackend.exception.ThrowUtils;
import com.example.sypicturebackend.manager.FileManager;
import com.example.sypicturebackend.model.dto.file.UploadPictureResult;
import com.example.sypicturebackend.model.dto.picture.PictureQueryRequest;
import com.example.sypicturebackend.model.dto.picture.PictureReviewRequest;
import com.example.sypicturebackend.model.dto.picture.PictureUploadRequest;
import com.example.sypicturebackend.model.entity.Picture;
import com.example.sypicturebackend.model.entity.User;
import com.example.sypicturebackend.model.enums.PictureReviewStatusEnum;
import com.example.sypicturebackend.model.vo.PictureVO;
import com.example.sypicturebackend.model.vo.UserVO;
import com.example.sypicturebackend.service.PictureService;
import com.example.sypicturebackend.mapper.PictureMapper;
import com.example.sypicturebackend.service.UserService;
import org.springframework.scripting.bsh.BshScriptEvaluator;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @author 诺诺
* @description 针对表【picture(图片)】的数据库操作Service实现
* @createDate 2024-12-19 15:54:34
*/
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
    implements PictureService{

	@Resource
	private FileManager fileManager;

	@Resource
	private UserService userService;

	@Override
    public void validPicture(Picture picture) {
		ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR);
		//从对象中取值
		Long id = picture.getId();
		String url = picture.getUrl();
		String introduction = picture.getIntroduction();
		//修改数据时 id不能为空 有参数则校验
		ThrowUtils.throwIf(ObjUtil.isNull(id), ErrorCode.PARAMS_ERROR,"id不能为空");
		//传递了url  就校验
		if(StrUtil.isNotBlank(url)){
			ThrowUtils.throwIf(url.length() > 1024, ErrorCode.PARAMS_ERROR, "url 过长");
		}
		if (StrUtil.isNotBlank(introduction)) {
			ThrowUtils.throwIf(introduction.length() > 800, ErrorCode.PARAMS_ERROR, "简介过长");
		}

	}

	/**
	 * 分页获取图片封装
	 */
	@Override
	public Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request) {
		List<Picture> pictureList = picturePage.getRecords();
		Page<PictureVO> pictureVOPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());
		if (CollUtil.isEmpty(pictureList)) {
			return pictureVOPage;
		}
		// 对象列表 => 封装对象列表
		List<PictureVO> pictureVOList = pictureList.stream().map(PictureVO::objToVo).collect(Collectors.toList());
		// 1. 关联查询用户信息
		Set<Long> userIdSet = pictureList.stream().map(Picture::getUserId).collect(Collectors.toSet());
		Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
				.collect(Collectors.groupingBy(User::getId));
		// 2. 填充信息
		pictureVOList.forEach(pictureVO -> {
			Long userId = pictureVO.getUserId();
			User user = null;
			if (userIdUserListMap.containsKey(userId)) {
				user = userIdUserListMap.get(userId).get(0);
			}
			pictureVO.setUser(userService.getUserVO(user));
		});
		pictureVOPage.setRecords(pictureVOList);
		return pictureVOPage;
	}

	@Override
	public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
		QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
		if (pictureQueryRequest == null) {
			return queryWrapper;
		}
		// 从对象中取值
		Long id = pictureQueryRequest.getId();
		String name = pictureQueryRequest.getName();
		String introduction = pictureQueryRequest.getIntroduction();
		String category = pictureQueryRequest.getCategory();
		List<String> tags = pictureQueryRequest.getTags();
		Long picSize = pictureQueryRequest.getPicSize();
		Integer picWidth = pictureQueryRequest.getPicWidth();
		Integer picHeight = pictureQueryRequest.getPicHeight();
		Double picScale = pictureQueryRequest.getPicScale();
		String picFormat = pictureQueryRequest.getPicFormat();
		String searchText = pictureQueryRequest.getSearchText();
		Long userId = pictureQueryRequest.getUserId();
		String sortField = pictureQueryRequest.getSortField();
		String sortOrder = pictureQueryRequest.getSortOrder();

		Integer reviewStatus = pictureQueryRequest.getReviewStatus();
		String reviewMessage = pictureQueryRequest.getReviewMessage();
		Long reviewerId = pictureQueryRequest.getReviewerId();

		// 从多字段中搜索
		if (StrUtil.isNotBlank(searchText)) {
			// 需要拼接查询条件
			// and (name like "%xxx%" or introduction like "%xxx%")
			queryWrapper.and(
					qw -> qw.like("name", searchText)
							.or()
							.like("introduction", searchText)
			);
		}
		queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
		queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
		queryWrapper.like(StrUtil.isNotBlank(name), "name", name);
		queryWrapper.like(StrUtil.isNotBlank(introduction), "introduction", introduction);
		queryWrapper.like(StrUtil.isNotBlank(picFormat), "picFormat", picFormat);
		queryWrapper.like(StrUtil.isNotBlank(reviewMessage), "reviewMessage", reviewMessage);
		queryWrapper.eq(StrUtil.isNotBlank(category), "category", category);
		queryWrapper.eq(ObjUtil.isNotEmpty(picWidth), "picWidth", picWidth);
		queryWrapper.eq(ObjUtil.isNotEmpty(picHeight), "picHeight", picHeight);
		queryWrapper.eq(ObjUtil.isNotEmpty(picSize), "picSize", picSize);
		queryWrapper.eq(ObjUtil.isNotEmpty(picScale), "picScale", picScale);
		queryWrapper.eq(ObjUtil.isNotEmpty(reviewStatus), "reviewStatus", reviewStatus);
		queryWrapper.eq(ObjUtil.isNotEmpty(reviewerId), "reviewerId", reviewerId);

		// JSON 数组查询
		if (CollUtil.isNotEmpty(tags)) {
			/* and (tag like "%\"Java\"%" and like "%\"Python\"%") */
			for (String tag : tags) {
				queryWrapper.like("tags", "\"" + tag + "\"");
			}
		}
		// 排序
		queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
		return queryWrapper;
	}




	@Override
	public PictureVO uploadPicture(MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest, User loginUser) {
		// 校验参数
			ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
		//判断是新增还是删除
		Long pictureId = null;
		if(pictureUploadRequest != null){
			pictureId = pictureUploadRequest.getId();
		}
		//如果是更新 判断是否存在
		if(pictureId != null){
			Picture oldPicture = this.getById(pictureId);
			ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
			//只能本人还有管理员才能编辑
			if(!oldPicture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)){
				throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
			}
		}
		//上传图片 得到图片信息
		//按照用户id划分目录
		String uploadPathPrefix  = String.format("public/%s", loginUser.getId());
		UploadPictureResult uploadPictureResult = fileManager.uploadPicture(multipartFile, uploadPathPrefix);
		// 构造要入库的图片信息
		Picture picture = new Picture();
		picture.setUrl(uploadPictureResult.getUrl());
		picture.setName(uploadPictureResult.getPicName());
		picture.setPicSize(uploadPictureResult.getPicSize());
		picture.setPicWidth(uploadPictureResult.getPicWidth());
		picture.setPicHeight(uploadPictureResult.getPicHeight());
		picture.setPicScale(uploadPictureResult.getPicScale());
		picture.setPicFormat(uploadPictureResult.getPicFormat());
		picture.setUserId(loginUser.getId());

		//补充审核参数
		this.fillReviewParams(picture,loginUser);

		//操作数据库
        //如果pictureId不为空 那就是更新 否则就是新增
		if(pictureId != null){
			//如果是更新 那么更新 id  还有编辑时间
			picture.setId(pictureId);
			picture.setEditTime(new Date());

		}

		boolean result  = this.saveOrUpdate(picture);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "图片上传失败，数据库操作失败");

		return PictureVO.objToVo(picture);
	}

	@Override
	public PictureVO getPictureVO(Picture picture, HttpServletRequest request) {
		// 对象转封装类
		PictureVO pictureVO = PictureVO.objToVo(picture);
		// 关联查询用户信息
		Long userId = picture.getUserId();
		if (userId != null && userId > 0) {
			User user = userService.getById(userId);
			UserVO userVO = userService.getUserVO(user);
			pictureVO.setUser(userVO);
		}
		return pictureVO;
	}

	@Override
	public void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser) {
         //校验参数
		ThrowUtils.throwIf(pictureReviewRequest == null, ErrorCode.PARAMS_ERROR );
		Long id = pictureReviewRequest.getId();
		Integer reviewStatus = pictureReviewRequest.getReviewStatus();
		PictureReviewStatusEnum reviewStatusEnum = PictureReviewStatusEnum.getEnumByValue(reviewStatus);
		String reviewMessage = pictureReviewRequest.getReviewMessage();
		if(reviewStatusEnum == null || id == null || PictureReviewStatusEnum.REVIEWING.equals(reviewStatusEnum)){
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		//判断图片是否存在
		Picture oldPicture = getById(id);
	    ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
		//判断审核状态是否重复 已经是该状态
		if(oldPicture.getReviewStatus().equals(reviewStatus)){
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "请勿重复审核");
		}
		//数据库操作
		Picture updatePicture = new Picture();
		BeanUtil.copyProperties(pictureReviewRequest, updatePicture);
		updatePicture.setReviewTime(new Date());
		updatePicture.setReviewerId(loginUser.getId());
		boolean result = this.updateById(updatePicture);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
	}

	/**
	 * 填充审核参数
	 * @param picture
	 * @param loginUser
	 */
	@Override
	public void fillReviewParams(Picture picture, User loginUser) {
		if(userService.isAdmin(loginUser)){
			//管理员过审
			picture.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
			picture.setReviewerId(loginUser.getId());
			picture.setReviewMessage("管理员自动过审");
			picture.setReviewTime(new Date());
		}else{
			//非管理员， 创建 编辑默认都是待审核
			picture.setReviewStatus(PictureReviewStatusEnum.REVIEWING.getValue());
		}
	}
}




