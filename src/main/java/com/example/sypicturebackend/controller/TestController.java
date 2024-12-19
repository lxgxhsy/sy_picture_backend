package com.example.sypicturebackend.controller;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: sy
 * @CreateTime: 2024-12-17
 * @Description: 测试u
 * @Version: 1.0
 */

@Slf4j
@RestController
public class TestController {

	@GetMapping("/test")
	public void test(){
		System.out.println("dsd");
	}
}
