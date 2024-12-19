package com.example.sypicturebackend.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: sy
 * @CreateTime: 2024-12-19
 * @Description: 分页插件
 * @Version: 1.0
 */


@Configuration
@MapperScan("com.example.sypicturebackend.mapper")
public class MyBatisPlusConfig {
	/**
	 * 拦截器配置
	 *
	 * @return {@link MybatisPlusInterceptor}
	 */
	@Bean
	public MybatisPlusInterceptor mybatisPlusInterceptor() {
		MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
		// 分页插件
		interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
		return interceptor;
	}

}
