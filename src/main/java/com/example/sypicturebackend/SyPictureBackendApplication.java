package com.example.sypicturebackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@MapperScan("com.example.sypicturebackend.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)
public class SyPictureBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(SyPictureBackendApplication.class, args);
	}

}
