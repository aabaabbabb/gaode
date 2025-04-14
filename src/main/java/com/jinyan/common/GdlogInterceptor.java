package com.jinyan.common;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import com.jfinal.kit.StrKit;
import com.jfinal.render.JsonRender;
import com.jfinal.render.Render;
import com.jinyan.controller.HotelMallController;
import com.jinyan.controller.base.BaseController;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**

 */
public class GdlogInterceptor implements Interceptor {
	private static final Logger log = Logger.getLogger(HotelMallController.class);
	public void intercept(Invocation inv) {
		Controller c = inv.getController();
		
		
//		// "/" 重定向到 "/admin"，正确显�? tab 标签 title
//		if ("/".equals(inv.getActionKey())) {
//			c.redirect("/admin");
//			return ;
//		}
		String actionKey = inv.getActionKey();
//		logger.error("output ==> method name = " + actionKey + "; return value = " + controller.getRender());
//		log.warn("input ==> method name = " + actionKey + "; params = " );
		long start = System.currentTimeMillis();
		StringBuilder sb = new StringBuilder();

		try {
			inv.invoke();
		} finally {
			long usedTime = System.currentTimeMillis() - start;
			//遍历出所有参数，数组的话用逗号分开显示所有参数
			String args = "";
			Map<String, String[]> parameterMap = (((BaseController) inv.getTarget()).getRequest()).getParameterMap();
			for (String s : parameterMap.keySet()) {
				args += s + "=";
				for (String value : parameterMap.get(s)) {
					args += value + ",";
				}
				args = args.substring(0, args.length() - 1) + ";";
			}
			if (StrKit.notBlank(args)) {
				args = args.substring(0, args.length() - 1);
			}
			sb.append("--------------------------------------------------------------------------------");
			sb.append("\r\n");
			// 请求URL
			sb.append("Url           : " + inv.getActionKey());
			sb.append("\r\n");
			// 请求控制层全名
			sb.append("Controller    : " + inv.getController());
			sb.append("\r\n");
			// 请求方法名
			sb.append("Method        : " + inv.getMethodName());
			sb.append("\r\n");
			// 参数非空则显示
			if (StrKit.notBlank(args)) {
				sb.append("Parameter     : " + args);
			}
			// 获取到请求方法返回值，并记录日志，便于排查问题
			Render r = c.getRender();
			if (r instanceof JsonRender) {
				String JsonText = ((JsonRender)r).getJsonText();
				sb.append("\r\n");
				sb.append("\r\n");
				sb.append("output ==> method name = " + actionKey + "; return value = " + JsonText);
 				// JsonText 处理
			}
			sb.append("\r\n");
			// 方法执行耗时
			sb.append("TimeConsuming : " + usedTime + "ms");
			sb.append("\r\n");
			sb.append("--------------------------------------------------------------------------------");
			sb.append("\r\n");
			//log.warn(sb.toString());
		}



	}
	
	/**
	 * 判断是否�? ajax 请求
	 */
	boolean isAjaxRequest(HttpServletRequest req) {
		return "XMLHttpRequest".equalsIgnoreCase(req.getHeader("X-Requested-With"));
	}
}


