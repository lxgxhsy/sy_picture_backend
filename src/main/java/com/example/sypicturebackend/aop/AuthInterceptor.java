package com.example.sypicturebackend.aop;

import com.example.sypicturebackend.annotation.AuthCheck;
import com.example.sypicturebackend.exception.BusinessException;
import com.example.sypicturebackend.exception.ErrorCode;
import com.example.sypicturebackend.model.entity.User;
import com.example.sypicturebackend.model.enums.UserRoleEnum;
import com.example.sypicturebackend.service.UserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @Author: shiyong
 * @CreateTime: 2024-12-19
 * @Description:
 * @Version: 1.0
 */


@Aspect
@Component
public class AuthInterceptor {

	@Resource
	private UserService userService;

	/**
	 * 执行拦截
	 * @param joinPoint
	 * @param authCheck
	 * @return
	 * @throws Throwable
	 */
	@Around("@annotation(authCheck)")
	public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
		String mustRole = authCheck.mustRole();
		RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
		HttpServletRequest request = ((ServletRequestAttributes)requestAttributes).getRequest();
		//当前登录用户
		User loginUser = userService.getLoginUser(request);
		UserRoleEnum mustRoleEnum  = UserRoleEnum.getEnumByValue(mustRole);
		//不需要权限 放行
		if (mustRoleEnum == null) {
			return joinPoint.proceed();
		}
		// 以下为：必须有该权限才通过
		// 获取当前用户具有的权限
		UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(loginUser.getUserRole());
		//没有权限 拒绝
		if(userRoleEnum == null){
			throw  new BusinessException(ErrorCode.NO_AUTH_ERROR);
		}
		// 要求必须有管理员权限，但用户没有管理员权限，拒绝
		if (UserRoleEnum.ADMIN.equals(mustRoleEnum) && !UserRoleEnum.ADMIN.equals(userRoleEnum)) {
			throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
		}
		// 通过权限校验，放行

       return joinPoint.proceed();
	}
}
