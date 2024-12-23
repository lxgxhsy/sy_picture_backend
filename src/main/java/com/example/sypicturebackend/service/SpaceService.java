package com.example.sypicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.sypicturebackend.model.dto.picture.PictureQueryRequest;
import com.example.sypicturebackend.model.dto.space.SpaceAddRequest;
import com.example.sypicturebackend.model.dto.space.SpaceQueryRequest;
import com.example.sypicturebackend.model.entity.Picture;
import com.example.sypicturebackend.model.entity.Space;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.sypicturebackend.model.entity.User;
import com.example.sypicturebackend.model.vo.PictureVO;
import com.example.sypicturebackend.model.vo.SpaceVO;

import javax.servlet.http.HttpServletRequest;

/**
* @author 诺诺
* @description 针对表【space(空间)】的数据库操作Service
* @createDate 2024-12-23 00:01:00
*/
public interface SpaceService extends IService<Space> {

	/**
	 * 创建空间
	 * @param spaceAddRequest
	 * @param loginUser
	 * @return
	 */
	long addSpace(SpaceAddRequest spaceAddRequest, User loginUser);

	/**
	 * 校验空间
	 * @param space
	 * @param add 是否为创建时校验
	 */
	void validSpace(Space space, boolean add);

	/**
	 * 获取空间包装类（单条）
	 *
	 * @param space
	 * @param request
	 * @return
	 */
	SpaceVO getSpaceVO(Space space, HttpServletRequest request);

	/**
	 * 获取图片包装类（分页）
	 *
	 * @param spacePage
	 * @param request
	 * @return
	 */
	Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request);

	/**
	 * 获取查询对象
	 *
	 * @param spaceQueryRequest
	 * @return
	 */
	QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);

	/**
	 *  根据空间级别填充空间对象
	 * @param space
	 */
	void fillSpaceBySpaceLevel(Space space);


}