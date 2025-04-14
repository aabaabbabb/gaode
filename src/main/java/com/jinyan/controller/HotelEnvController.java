package com.jinyan.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.aop.Inject;
import com.jfinal.core.Path;
import com.jfinal.kit.HttpKit;
import com.jfinal.kit.JsonKit;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.redis.Cache;
import com.jfinal.plugin.redis.Redis;
import com.jinyan.common.Constant;
import com.jinyan.common.GdlogInterceptor;
import com.jinyan.controller.base.BaseController;
import com.jinyan.model.Hotel;
import com.jinyan.service.HotelService;
import com.jinyan.utils.CheckSignUtils;
import com.jinyan.utils.RedisUtil;
import com.jinyan.utils.ResponseUtils;
import com.jinyan.utils.ResponseUtilsOutResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 这个类 用来处理，其它相关联环竟中，需要处理的一些接口，

@Clear
@Before(GdlogInterceptor.class)
@Path(value = "/hotelEnv", viewPath = "")
public class HotelEnvController extends BaseController {
	
	@Inject
	private HotelService hsrv;

	public static Map<String, String> headerMap = new HashMap<String, String>();
	
	

	/**
	 *   接口描述：获取艺龙详情 detail
	 *   艺龙对应 https://open.elong.com/doc/info/cn-api-search-hotel_detail#GuaranteeResult
	 */
	public void getElongHotelDetail() {



	}



}
