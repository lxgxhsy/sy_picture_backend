package com.example.sypicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.sypicturebackend.model.dto.user.UserQueryRequest;
import com.example.sypicturebackend.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.sypicturebackend.model.vo.LoginUserVO;
import com.example.sypicturebackend.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author 诺诺
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2024-12-18 10:35:27
*/
public interface UserService extends IService<User> {


	/**
	 * 是否为管理员
	 *
	 * @param user
	 * @return
	 */
	boolean isAdmin(User user);


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


	/**
	 * 获取当前登录用户
	 *
	 * @param request
	 * @return
	 */
	User getLoginUser(HttpServletRequest request);

	/**
	 * 用户登录状态
	 * @param userAccount 账号
	 * @param userPassword 密码
	 * @param request request请求
	 * @return
	 */
	LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);


	/**
	 * 获取脱敏后的已登录用户的信息
	 * @param user
	 * @return
	 */
	LoginUserVO  getLoginUserVO(User user);

	/**
	 * 获取脱敏后的已登录用户的信息
	 * @param user
	 * @return
	 */
	UserVO getUserVO(User user);

	/**
	 * 获取脱敏后的已登录用户列表的信息
	 * @param userList
	 * @return
	 */
	List<UserVO> getUserVOList(List<User> userList);

	/**
	 * 用户注销
	 * @param request request 请求
	 * @return
	 */
	boolean userLogout(HttpServletRequest request);

	/**
	 * 分页查询列表
	 * @param userQueryRequest 分页查询
	 * @return
	 */
	QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);
}
