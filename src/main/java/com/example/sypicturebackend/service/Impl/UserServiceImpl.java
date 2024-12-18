package com.example.sypicturebackend.service.Impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sypicturebackend.exception.BusinessException;
import com.example.sypicturebackend.exception.ErrorCode;
import com.example.sypicturebackend.model.enums.UserRoleEnum;
import com.example.sypicturebackend.model.entity.User;
import com.example.sypicturebackend.service.UserService;
import com.example.sypicturebackend.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.HashMap;

/**
* @author 诺诺
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2024-12-18 10:35:27
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService {

	/**
	 * 用户注册
	 * @param userAccount 用户账号
	 * @param userPassword 用户密码
	 * @param checkPassword 检验密码
	 * @return 新用户id
	 */
	@Override
	public long userRegister(String userAccount, String userPassword, String checkPassword) {
		//校验
		if (StrUtil.hasBlank(userAccount, userPassword, checkPassword)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
		}
		if (userAccount.length() < 4) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
		}
		if (userPassword.length() < 8 || checkPassword.length() < 8) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
		}
		if (!userPassword.equals(checkPassword)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
		}
		//检查是否重复
		QueryWrapper<User> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("userAccount", userAccount); ;
		long count = this.baseMapper.selectCount(queryWrapper);
		if (count > 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
		}
		//加密
		String encryptPassword = getEncryptPassword(userPassword);
		//插入数据
		User user = new User();
		user.setUserAccount(userAccount);
		user.setUserPassword(encryptPassword);
		user.setUserName("无名");
		user.setUserRole(UserRoleEnum.USER.getValue());
		boolean saveResult = this.save(user);
        if(!saveResult){
        	throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
        }

		return user.getId();
	}

	/**
	 * 加密密码
	 * @param userPassword 用户密码
	 * @return
	 */
	@Override
	public String getEncryptPassword(String userPassword) {
		// 盐值，混淆密码
		final String SALT = "sy";
		return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
	}
}




