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
import com.example.sypicturebackend.manager.sharding.DynamicShardingManager;
import com.example.sypicturebackend.model.dto.space.SpaceAddRequest;
import com.example.sypicturebackend.model.dto.space.SpaceQueryRequest;
import com.example.sypicturebackend.model.entity.Picture;
import com.example.sypicturebackend.model.entity.Space;
import com.example.sypicturebackend.model.entity.SpaceUser;
import com.example.sypicturebackend.model.entity.User;
import com.example.sypicturebackend.model.enums.SpaceLevelEnum;
import com.example.sypicturebackend.model.enums.SpaceRoleEnum;
import com.example.sypicturebackend.model.enums.SpaceTypeEnum;
import com.example.sypicturebackend.model.vo.SpaceVO;
import com.example.sypicturebackend.model.vo.UserVO;
import com.example.sypicturebackend.service.SpaceService;
import com.example.sypicturebackend.mapper.SpaceMapper;
import com.example.sypicturebackend.service.SpaceUserService;
import com.example.sypicturebackend.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
* @author 诺诺
* @description 针对表【space(空间)】的数据库操作Service实现
* @createDate 2024-12-23 00:01:00
*/
@Service
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space>
    implements SpaceService{

	@Resource
	private UserService userService;

	@Resource
	private SpaceUserService spaceUserService;

	// 为了方便部署 暂时不使用
//	@Resource
//	@Lazy
//	private DynamicShardingManager dynamicShardingManager;

	/**
	* 编程式事务处理器
	 */
	@Resource
	private TransactionTemplate transactionTemplate;

	Map<Long, Object> lockMap = new ConcurrentHashMap<>();


	/**
	 * 创建空间 用户最多只能创建一个
	 *
	 * @param spaceAddRequest
	 * @param loginUser
	 * @return
	 */
	@Override
	public long addSpace(SpaceAddRequest spaceAddRequest, User loginUser) {
		// 1. 填充参数默认值
		// 转换实体类和 DTO
		Space space = new Space();
		BeanUtils.copyProperties(spaceAddRequest, space);
		if (StrUtil.isBlank(space.getSpaceName())) {
			space.setSpaceName("默认空间");
		}
		if (space.getSpaceLevel() == null) {
			space.setSpaceLevel(SpaceLevelEnum.COMMON.getValue());
		}
		if(space.getSpaceType() == null){
			space.setSpaceType(SpaceTypeEnum.PRIVATE.getValue());
		}
		// 填充容量和大小
		this.fillSpaceBySpaceLevel(space);
		// 2. 校验参数
		this.validSpace(space, true);
		// 3. 校验权限，非管理员只能创建普通级别的空间
		Long userId = loginUser.getId();
		space.setUserId(userId);
		if (SpaceLevelEnum.COMMON.getValue() != space.getSpaceLevel() && !userService.isAdmin(loginUser)) {
			throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限创建指定级别的空间");
		}
		// 4. 控制同一用户只能创建一个私有空间 以及一个团队空间
		// 字符串常量池 相同的存储空间 为了让锁对象获取不同对象的同一个值
		String lock = String.valueOf(userId).intern();
		//Object lock = lockMap.computeIfAbsent(userId, k -> new Object());
		synchronized (lock) {
			try {
				//封装到事务中 避免重复读
				Long newSpaceId = transactionTemplate.execute(status -> {
					// 判断是否已有空间
					boolean exists = this.lambdaQuery()
							.eq(Space::getUserId, userId)
							.eq(Space::getSpaceType, spaceAddRequest.getSpaceType())
							.exists();
					// 如果已有空间，就不能再创建
					ThrowUtils.throwIf(exists, ErrorCode.OPERATION_ERROR, "每个用户每类空间只能创建一个空间");
					// 写入数据库
					boolean result = this.save(space);
					ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "保存空间到数据库失败");
					// 创建成功后 如果是团队空间 关联新增团队成员记录
					if(SpaceTypeEnum.TEAM.getValue() == space.getSpaceType()){
						SpaceUser spaceUser = new SpaceUser();
						spaceUser.setSpaceId(space.getId());
						spaceUser.setUserId(userId);
						spaceUser.setSpaceRole(SpaceRoleEnum.ADMIN.getValue());
						result = spaceUserService.save(spaceUser);
						ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "创建团队成员记录失败");
					}
				             // 创建分表（仅对团队空间生效）为方便部署，暂时不使用
//                  dynamicShardingManager.createSpacePictureTable(space);

					// 返回新写入的数据 id
					return space.getId();
				});
				return Optional.ofNullable(newSpaceId).orElse(-1L);
			}finally {
				lockMap.remove(userId);
			}
			}

 	}

	@Override
	public void validSpace(Space space, boolean add) {
		//校验参数
		ThrowUtils.throwIf(space == null, ErrorCode.PARAMS_ERROR);
		//从对象中取值
		String spaceName = space.getSpaceName();
		Integer spaceLevel = space.getSpaceLevel();
		SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(spaceLevel);
		Integer spaceType = space.getSpaceType();
		SpaceTypeEnum spaceTypeEnum = SpaceTypeEnum.getEnumByValue(spaceType);
		//创建时校验
		if(add){
			if (StrUtil.isBlank(spaceName)){
				throw new BusinessException(ErrorCode.PARAMS_ERROR,"空间名称不能为空");
			}
			if(spaceLevel == null){
				throw new BusinessException(ErrorCode.PARAMS_ERROR,"空间级别不能为空");

			}
			if(spaceTypeEnum == null){
				throw new BusinessException(ErrorCode.PARAMS_ERROR,"空间类型不能为空");
			}
		}
		//修改数据时 空间名称进行校验
		if(StrUtil.isBlank(spaceName) && spaceName.length() > 30){
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间名称过长");
		}
		//修改数据时 空间级别进行校验
		if (spaceLevel != null && spaceLevelEnum == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间级别不存在");
		}
		//修改数据时 空间类别进行校验
		if (spaceType != null && spaceTypeEnum == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间类别不存在");
		}
	}

	@Override
	public SpaceVO getSpaceVO(Space space, HttpServletRequest request) {
		// 对象转封装类
		SpaceVO spaceVO = SpaceVO.objToVo(space);
		// 关联查询用户信息
		Long userId = space.getUserId();
		if (userId != null && userId > 0) {
			User user = userService.getById(userId);
			UserVO userVO = userService.getUserVO(user);
			spaceVO.setUser(userVO);
		}
		return spaceVO;
	}

	/**
	 * 分页获取空间封装
	 */
	@Override
	public Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request) {
		List<Space> spaceList = spacePage.getRecords();
		Page<SpaceVO> spaceVOPage = new Page<>(spacePage.getCurrent(), spacePage.getSize(), spacePage.getTotal());
		if (CollUtil.isEmpty(spaceList)) {
			return spaceVOPage;
		}
		// 对象列表 => 封装对象列表
		List<SpaceVO> spaceVOList = spaceList.stream()
				.map(SpaceVO::objToVo)
				.collect(Collectors.toList());
		// 1. 关联查询用户信息
		Set<Long> userIdSet = spaceList.stream().map(Space::getUserId).collect(Collectors.toSet());
		// 1 => user1 2 => user2
		Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
				.collect(Collectors.groupingBy(User::getId));
		// 2. 填充信息
		spaceVOList.forEach(spaceVO -> {
			Long userId = spaceVO.getUserId();
			User user = null;
			if (userIdUserListMap.containsKey(userId)) {
				user = userIdUserListMap.get(userId).get(0);
			}
			spaceVO.setUser(userService.getUserVO(user));
		});
		spaceVOPage.setRecords(spaceVOList);
		return spaceVOPage;
	}

	@Override
	public QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest) {
		QueryWrapper<Space> queryWrapper = new QueryWrapper<>();
		if(spaceQueryRequest != null){
			return queryWrapper;
		}
		Long id = spaceQueryRequest.getId();
		Long userId = spaceQueryRequest.getUserId();
		String spaceName = spaceQueryRequest.getSpaceName();
		Integer spaceLevel = spaceQueryRequest.getSpaceLevel();
		String sortField = spaceQueryRequest.getSortField();
		String sortOrder = spaceQueryRequest.getSortOrder();
		Integer spaceType = spaceQueryRequest.getSpaceType();
		//拼接查询条件
		queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
		queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
		queryWrapper.like(StrUtil.isNotBlank(spaceName), "spaceName", spaceName);
		queryWrapper.eq(ObjUtil.isNotEmpty(spaceLevel), "spaceLevel", spaceLevel);
		queryWrapper.eq(ObjUtil.isNotEmpty(spaceType), "spaceType", spaceType);
		// 排序
		queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
		return queryWrapper;
	}

	@Override
	public void fillSpaceBySpaceLevel(Space space) {
		SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(space.getSpaceLevel());
        if(spaceLevelEnum != null){
	        long maxSize = spaceLevelEnum.getMaxSize();
	        if (space.getMaxSize() == null) {
		        space.setMaxSize(maxSize);
	        }
	        long maxCount = spaceLevelEnum.getMaxCount();
	        if (space.getMaxCount() == null) {
		        space.setMaxCount(maxCount);
	        }
        }
	}

	@Override
	public void checkSpaceAuth(User loginUser, Space space) {
		if(!space.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)){
			throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
		}
	}


}




