package com.jinyan.common;

import com.jfinal.server.undertow.UndertowServer;

/**
 * 鏈」鐩噰鐢ㄣ?奐Final 淇变箰閮ㄦ巿鏉冨崗璁?嬶紝淇濇姢鐭ヨ瘑浜ф潈锛屽氨鏄湪淇濇姢鎴戜滑鑷繁韬鐨勮涓氥??
 * 
 * Copyright (c) 2011-2021, jfinal.com.jinyan.commonage com.jfinal.admin;

import com.jfinal.admin.common.AppConfig;
import com.jfinal.server.undertow.UndertowServer;

/**
 * 鍚姩鍏ュ彛
 */
public class JFinalAdmin {
	public static void main(String[] args) {
		UndertowServer.start(AppConfig.class);
	}
}





