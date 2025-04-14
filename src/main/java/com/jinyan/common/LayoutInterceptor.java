package com.jinyan.common;

import javax.servlet.http.HttpServletRequest;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;

/**
 * 本项目采用�?�JFinal 俱乐部授权协议�?�，保护知识产权，就是在保护我们自己身处的行业�??
 * 
 * Copyright (c) 2011-2021, jfinal.com.jinyan.common.jfinal.admin.common;

import javax.servlet.http.HttpServletRequest;
import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;

/**
 * LayoutInterceptor
 * 
 * 极其重要：如果后台页面不使用 _admin_layout.html 时，�?要使�? @Clear(LayoutInterceptor.class) 去除本拦截器
 *          示例：ArticleAdminController 中的 add()、edit()
 *          示例：LoginController 用到 @Clear �? action
 * 
 * 
 * 主要功能是识别是否为 ajax 请求，是则去调用 action，否则直接调�?
 * render(_admin_layout.html);
 * 
 * 后续可能会添加一些后台�?�用拦截功能，处理一些后台全�?都要依赖的事�?
 * 
 * 
 * 后台管理 UI 采用 enjoy + ajax 方案实现前后分离，无�?使用 vue
 * react angular �?术栈，降低成本�?�提升效率：
 *  1：非 ajax 请求 enjoy 渲染布局模板 _admin_layout.html
 *  2：ajax 请求 enjoy 渲染 html 片段
 *  3：enjoy 渲染�? html 片段通过 js 插入相应的位�?
 *  4：一个页面可以使用多�? ajax 请求插入不同�? html 片段来组合整个页面内�? 
 * 
 */
public class LayoutInterceptor implements Interceptor {
	
	public void intercept(Invocation inv) {
		Controller c = inv.getController();
		
		
		// "/" 重定向到 "/admin"，正确显�? tab 标签 title
		if ("/".equals(inv.getActionKey())) {
			c.redirect("/admin");
			return ;
		}
		
		
		// ajax 请求放行去调�? action
		if (isAjaxRequest(c.getRequest())) {
			inv.invoke();
		}
		// �? ajax 请求直接渲染 layout 模板
		else {
			c.render("/_view/admin/common/_layout.html");
		}
	}
	
	/**
	 * 判断是否�? ajax 请求
	 */
	boolean isAjaxRequest(HttpServletRequest req) {
		return "XMLHttpRequest".equalsIgnoreCase(req.getHeader("X-Requested-With"));
	}
}


