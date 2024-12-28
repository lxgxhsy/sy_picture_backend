package com.example.sypicturebackend.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sypicturebackend.exception.BusinessException;
import com.example.sypicturebackend.exception.ErrorCode;
import com.example.sypicturebackend.exception.ThrowUtils;
import com.example.sypicturebackend.mapper.SpaceMapper;
import com.example.sypicturebackend.model.dto.space.analyze.SpaceAnalyzeRequest;
import com.example.sypicturebackend.model.entity.Picture;
import com.example.sypicturebackend.model.entity.Space;
import com.example.sypicturebackend.model.entity.User;
import com.example.sypicturebackend.service.PictureService;
import com.example.sypicturebackend.service.SpaceAnalyzeService;
import com.example.sypicturebackend.service.SpaceService;
import com.example.sypicturebackend.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @Author: shiyong
 * @CreateTime: 2024-12-28
 * @Description:
 * @Version: 1.0
 */

@Service
public class SpaceAnalyzeServiceImpl extends ServiceImpl<SpaceMapper, Space>
        implements SpaceAnalyzeService {

	@Resource
	private UserService userService;

	@Resource
	private SpaceService spaceService;

	@Resource
	private PictureService pictureService;


	/**
	 *  校验空间分析权限
	 * @param spaceAnalyzeRequest
	 * @param loginUser
	 */
	private void checkSpaceAnalyzeAuth(SpaceAnalyzeRequest spaceAnalyzeRequest, User loginUser){
		boolean queryPublic = spaceAnalyzeRequest.isQueryPublic();
		boolean queryAll = spaceAnalyzeRequest.isQueryAll();
        // 全空间分析或者公共图库权限校验 仅管理员可访问
		if(queryAll || queryPublic){
			ThrowUtils.throwIf(!userService.isAdmin(loginUser), ErrorCode.NO_AUTH_ERROR);
		}else{
			//分析特定空间 本人和管理员可以访问
			Long spaceId = spaceAnalyzeRequest.getSpaceId();
			ThrowUtils.throwIf(spaceId == null, ErrorCode.PARAMS_ERROR);
			Space space = spaceService.getById(spaceId);
			ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
		    spaceService.checkSpaceAuth(loginUser, space);
		}
	}

	/**
	 * 根据请求对象封装查询条件
	 *
	 * @param spaceAnalyzeRequest
	 * @param queryWrapper
	 */
	private void findAnalyzeQueryWrapper(SpaceAnalyzeRequest spaceAnalyzeRequest, QueryWrapper<Picture> queryWrapper){
		// 全空间分析
		boolean queryAll = spaceAnalyzeRequest.isQueryAll();
		if (queryAll) {
			return;
		}
		// 公共图库
		boolean queryPublic = spaceAnalyzeRequest.isQueryPublic();
		if (queryPublic) {
			queryWrapper.isNull("spaceId");
			return;
		}
		// 分析特定空间
		Long spaceId = spaceAnalyzeRequest.getSpaceId();
		if (spaceId != null) {
			queryWrapper.eq("spaceId", spaceId);
			return;
		}
		throw new BusinessException(ErrorCode.PARAMS_ERROR, "未指定查询范围");
	}
}
