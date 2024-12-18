package com.example.sypicturebackend.service;

import com.example.sypicturebackend.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 诺诺
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2024-12-18 10:35:27
*/
public interface UserService extends IService<User> {


	/**
	 * 加密密码
	 * @param userPassword 用户密码
	 * @return
	 */
	String getEncryptPassword(String userPassword);

	/**
	 * 用户注册
	 * @param userAccount 用户账号
	 * @param userPassword 用户密码
	 * @param checkPassword 检验密码
	 * @return 新用户id
	 */
	long userRegister(String userAccount, String userPassword, String checkPassword);
}
