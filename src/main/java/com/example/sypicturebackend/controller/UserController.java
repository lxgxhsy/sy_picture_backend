package com.example.sypicturebackend.controller;

import com.example.sypicturebackend.common.BaseResponse;
import com.example.sypicturebackend.common.ResultUtils;
import com.example.sypicturebackend.exception.ErrorCode;
import com.example.sypicturebackend.exception.ThrowUtils;
import com.example.sypicturebackend.model.dto.user.UserRegisterRequest;
import com.example.sypicturebackend.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @Author: sy
 * @CreateTime: 2024-12-18
 * @Description: 用户相关接口
 * @Version: 1.0
 */

@RestController
@RequestMapping("/user")
public class UserController {

	@Resource
	private UserService userService;

	@PostMapping("/register")
	public BaseResponse<Long> userRegister(@RequestBody  UserRegisterRequest userRegisterRequest) {
		ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);
		String userAccount = userRegisterRequest.getUserAccount();
		String userPassword = userRegisterRequest.getUserPassword();
		String checkPassword = userRegisterRequest.getCheckPassword();
		long result = userService.userRegister(userAccount, userPassword, checkPassword);
		return ResultUtils.success(result);
	}

}
