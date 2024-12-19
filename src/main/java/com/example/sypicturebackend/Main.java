//package com.example.sypicturebackend;
//
//import cn.hutool.core.util.IdUtil;
//import cn.hutool.core.util.StrUtil;
//import cn.hutool.http.HttpUtil;
//
///**
// * @Author: sy
// * @CreateTime: 2024-12-08
// * @Description: 测试hutool
// * @Version: 1.0
// */
//
//
//public class Main {
//	public static void main(String[] args) {
//		long l = System.currentTimeMillis();
//		String str = "abcdefgh";
//		String strSub1 = StrUtil.sub(str, 2, 3); //strSub1 -> c
//		String strSub2 = StrUtil.sub(str, 2, -3); //strSub2 -> cde
//		String strSub3 = StrUtil.sub(str, 3, 2); //strSub2 -> c
//
//		String uuid = IdUtil.randomUUID();
//
//		StringBuilder a = new StringBuilder("123");
//         a.append(strSub1);
//         a.append(strSub2);
//         a.append(strSub3);
//		long l1 = System.currentTimeMillis();
//
//		String s = IdUtil.fastSimpleUUID();
//
//		HttpUtil
//
//		System.out.println(l1-l);
//		System.out.println(a);
//	}
//}
