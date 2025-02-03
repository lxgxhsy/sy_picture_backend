package com.example.sypicturebackend.controller;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.sypicturebackend.annotation.AuthCheck;
import com.example.sypicturebackend.common.BaseResponse;
import com.example.sypicturebackend.common.DeleteRequest;
import com.example.sypicturebackend.common.ResultUtils;
import com.example.sypicturebackend.constant.UserConstant;
import com.example.sypicturebackend.exception.BusinessException;
import com.example.sypicturebackend.exception.ErrorCode;
import com.example.sypicturebackend.exception.ThrowUtils;
import com.example.sypicturebackend.manager.auth.annotation.SaSpaceCheckPermission;
import com.example.sypicturebackend.manager.auth.model.SpaceUserPermissionConstant;
import com.example.sypicturebackend.model.dto.space.*;
import com.example.sypicturebackend.model.dto.spaceuser.SpaceUserAddRequest;
import com.example.sypicturebackend.model.dto.spaceuser.SpaceUserEditRequest;
import com.example.sypicturebackend.model.dto.spaceuser.SpaceUserQueryRequest;
import com.example.sypicturebackend.model.entity.Space;
import com.example.sypicturebackend.model.entity.SpaceUser;
import com.example.sypicturebackend.model.entity.User;
import com.example.sypicturebackend.model.enums.SpaceLevelEnum;
import com.example.sypicturebackend.model.vo.SpaceUserVO;
import com.example.sypicturebackend.model.vo.SpaceVO;
import com.example.sypicturebackend.service.SpaceService;
import com.example.sypicturebackend.service.SpaceUserService;
import com.example.sypicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: shiyong
 * @CreateTime: 2025-01-04
 * @Description:
 * @Version: 1.0
 */

@RestController
@Slf4j
@RequestMapping("/spaceUser")
public class SpaceUserController {
	@Resource
	private UserService userService;

	@Resource
	private SpaceUserService spaceUserService;


	/**
	 * 添加成员到空间
	 */
	@PostMapping("/add")
	@SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
	public BaseResponse<Long> addSpaceUser(@RequestBody SpaceUserAddRequest spaceUserAddRequest, HttpServletRequest request) {
		ThrowUtils.throwIf(spaceUserAddRequest == null, ErrorCode.PARAMS_ERROR);
		long id = spaceUserService.addSpaceUser(spaceUserAddRequest);
		return ResultUtils.success(id);
	}

	/**
	 * 从空间移除成员
	 */
	@PostMapping("/delete")
	@SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
	public BaseResponse<Boolean> deleteSpaceUser(@RequestBody DeleteRequest deleteRequest,
	                                             HttpServletRequest request) {
		if (deleteRequest == null || deleteRequest.getId() <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		long id = deleteRequest.getId();
		// 判断是否存在
		SpaceUser oldSpaceUser = spaceUserService.getById(id);
		ThrowUtils.throwIf(oldSpaceUser == null, ErrorCode.NOT_FOUND_ERROR);
		// 操作数据库
		boolean result = spaceUserService.removeById(id);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
		return ResultUtils.success(true);
	}

	/**
	 * 查询某个成员在某个空间的信息
	 */
	@PostMapping("/get")
	@SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
	public BaseResponse<SpaceUser> getSpaceUser(@RequestBody SpaceUserQueryRequest spaceUserQueryRequest) {
		// 参数校验
		ThrowUtils.throwIf(spaceUserQueryRequest == null, ErrorCode.PARAMS_ERROR);
		Long spaceId = spaceUserQueryRequest.getSpaceId();
		Long userId = spaceUserQueryRequest.getUserId();
		ThrowUtils.throwIf(ObjectUtil.hasEmpty(spaceId, userId), ErrorCode.PARAMS_ERROR);
		// 查询数据库
		SpaceUser spaceUser = spaceUserService.getOne(spaceUserService.getQueryWrapper(spaceUserQueryRequest));
		ThrowUtils.throwIf(spaceUser == null, ErrorCode.NOT_FOUND_ERROR);
		return ResultUtils.success(spaceUser);
	}

	/**
	 * 查询成员信息列表
	 */
	@PostMapping("/list")
	@SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
	public BaseResponse<List<SpaceUserVO>> listSpaceUser(@RequestBody SpaceUserQueryRequest spaceUserQueryRequest,
	                                                     HttpServletRequest request) {
		ThrowUtils.throwIf(spaceUserQueryRequest == null, ErrorCode.PARAMS_ERROR);
		List<SpaceUser> spaceUserList = spaceUserService.list(
				spaceUserService.getQueryWrapper(spaceUserQueryRequest)
		);
		return ResultUtils.success(spaceUserService.getSpaceUserVOList(spaceUserList));
	}

	/**
	 * 编辑成员信息（设置权限）
	 */
	@PostMapping("/edit")
	@SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
	public BaseResponse<Boolean> editSpaceUser(@RequestBody SpaceUserEditRequest spaceUserEditRequest,
	                                           HttpServletRequest request) {
		if (spaceUserEditRequest == null || spaceUserEditRequest.getId() <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		// 将实体类和 DTO 进行转换
		SpaceUser spaceUser = new SpaceUser();
		BeanUtils.copyProperties(spaceUserEditRequest, spaceUser);
		// 数据校验
		spaceUserService.validSpaceUser(spaceUser, false);
		// 判断是否存在
		long id = spaceUserEditRequest.getId();
		SpaceUser oldSpaceUser = spaceUserService.getById(id);
		ThrowUtils.throwIf(oldSpaceUser == null, ErrorCode.NOT_FOUND_ERROR);
		// 操作数据库
		boolean result = spaceUserService.updateById(spaceUser);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
		return ResultUtils.success(true);
	}

	/**
	 * 查询我加入的团队空间列表
	 */
	@PostMapping("/list/my")
	public BaseResponse<List<SpaceUserVO>> listMyTeamSpace(HttpServletRequest request) {
		User loginUser = userService.getLoginUser(request);
		SpaceUserQueryRequest spaceUserQueryRequest = new SpaceUserQueryRequest();
		spaceUserQueryRequest.setUserId(loginUser.getId());
		List<SpaceUser> spaceUserList = spaceUserService.list(
				spaceUserService.getQueryWrapper(spaceUserQueryRequest)
		);
		return ResultUtils.success(spaceUserService.getSpaceUserVOList(spaceUserList));
	}
}
