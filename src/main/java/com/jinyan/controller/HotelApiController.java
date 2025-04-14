package com.jinyan.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.google.common.base.Strings;
import com.jfinal.aop.Before;
import com.jfinal.kit.JsonKit;
import com.jfinal.plugin.activerecord.Model;
import com.jinyan.common.GdlogInterceptor;
import com.jinyan.model.HotelroomChg;
import com.jinyan.service.HotelStuffService;
import com.jinyan.service.HotelroomChgService;
import com.jinyan.utils.*;
import org.jsoup.helper.StringUtil;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Clear;
import com.jfinal.aop.Inject;
import com.jfinal.core.Path;
import com.jfinal.kit.HttpKit;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.druid.DruidPlugin;
import com.jfinal.plugin.redis.Cache;
import com.jfinal.plugin.redis.Redis;
import com.jinyan.common.Constant;
import com.jinyan.controller.base.BaseController;
import com.jinyan.model.Hotel;
import com.jinyan.service.HotelService;

import redis.clients.jedis.Jedis;

import javax.servlet.http.HttpServletRequest;

@Clear
@Before(GdlogInterceptor.class)
@Path(value = "/hotel", viewPath = "")
public class HotelApiController extends BaseController {

	@Inject
	private HotelService hsrv;

	@Inject
	private HotelStuffService hsrvStuff;

	@Inject
	private HotelroomChgService hotelroomChgStuff;

	public static Map<String, String> headerMap = new HashMap<String, String>();

	static {
		headerMap.put("Content-Type", "application/x-www-form-urlencoded");
	}

	/**
	 * 高德主动拉取酒店id列表信息 ,接口已在高德接通 【高德已连通】
	 */
	public void getPoiIdList() {

		Map<String, String> dataMap = new HashMap<String, String>();

		// 要改为高德传入参数
		String method = getPara("method");
		String utc_timestamp = getPara("utc_timestamp");
		String app_id = getPara("app_id");
		String version = getPara("version");
		String sign = getPara("sign");
		String sign_type = getPara("sign_type");
		String charset = getPara("charset");
		String biz_content = getPara("biz_content");

		dataMap.put("method", method);
		dataMap.put("utc_timestamp", utc_timestamp);
		dataMap.put("app_id", app_id);
		dataMap.put("version", version);
		dataMap.put("version", version);
		dataMap.put("sign_type", sign_type);
		dataMap.put("charset", charset);
		dataMap.put("biz_content", biz_content);
		dataMap.put("sign", sign);

		// 取高德业务参数
		JSONObject jsonObject = JSONObject.parseObject(biz_content);
		int currentPage = jsonObject.getIntValue("currentPage");
		int pageSize = jsonObject.getIntValue("pageSize");

		try {
			List<Long> hidList = new ArrayList<Long>();
			List<Hotel> hotelList = hsrv.getHotelList(currentPage, pageSize);
			for (Hotel hotel : hotelList) {
				hidList.add(hotel.getId());
			}
			boolean checkSignResult = CheckSignUtils.checkSign(dataMap, sign);
			ResponseUtils res = new ResponseUtils();
			if (checkSignResult) {
				res.setCode("10000");
				res.setMsg("success");
				res.setData(JSON.toJSON(hidList));
			} else {

				res.setCode("40002");
				res.setMsg("非法的参数,验签失败");
				res.setSub_code("isv.invalid-signature");

				res.setSub_msg("无效签名:");
//				res.setData(JSON.toJSON(hidList));
			}

			// 高德响应， 需要在外层加一个response
//			renderJson(res);

			ResponseUtilsOutResponse resOut = new ResponseUtilsOutResponse();
			resOut.setResponse(res);
			renderJson(resOut);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 高德主动拉取酒店详情信息 【高德已连通】 高德调用酒店信息，不是重要信息，不用验签，节省资源（需要验签的接口，可以参照 getPoiIdList
	 * 这个接口） 开发文档 https://y.amap.com/docs/hotel/getpoiinfolist
	 */
	public void getPoiInfoList() {

		// 取业务参数 biz_content 里边的 type 文档列出的有 multi，不知道有别的作用没，目前只取HotelIDList 来取 hotelid
		String biz_content = getPara("biz_content");
		JSONObject jsonObject = JSONObject.parseObject(biz_content);
		JSONArray hotelids = jsonObject.getJSONArray("HotelIDList");

		List<String> ids = new ArrayList<String>();
		for (Object h : hotelids) {

			ids.add((String) h);
		}

		List<Record> hotelList = hsrv.getHotelListById(ids);

		// 用这个方法 转换成高德字段
		List<Map<String, Object>> hotelListNew = hsrv.recordsGaode(hotelList);

		ResponseUtils res = new ResponseUtils();
		res.setCode("10000");
		res.setMsg("success");

//		res.setData(JSON.toJSON(hotelList ));
//		res.setData(JsonKit.toJson(hotelListNew));
		res.setData(hotelListNew);
		ResponseUtilsOutResponse resOut = new ResponseUtilsOutResponse();
		resOut.setResponse(res);
		renderJson(resOut);

	}

	public static void main(String[] args) throws Exception {
		Long HotelID = 21083128L;
		HotelService hsrv = new HotelService();
		Hotel mHotel = hsrv.getHotelInfo(HotelID);

		Map<String, String> dataMap = new HashMap<String, String>();
		dataMap.put("method", "amap.hotel.offline.pushPoiInfoList");
		dataMap.put("utc_timestamp", String.valueOf(System.currentTimeMillis()));
		dataMap.put("app_id", Constant.APP_ID);
		dataMap.put("version", "1.0");
		dataMap.put("sign_type", "RSA2");
		dataMap.put("charset", "UTF-8");

		JSONObject bizContent = new JSONObject();
		bizContent.put("HotelID", HotelID);
		bizContent.put("HotelName", mHotel.getHotelName());
		bizContent.put("City", mHotel.getCityName());
		bizContent.put("Address", mHotel.getAddress());
		bizContent.put("GDLat", mHotel.getGaodeLat());
		bizContent.put("GDLon", mHotel.getGaodeLon());
		bizContent.put("HotelType", mHotel.getHotelType());
		bizContent.put("Telephone", mHotel.getPhone());

		dataMap.put("biz_content", JSON.toJSONString(bizContent));
		try {
			dataMap.put("sign", GenerateSignUtils.generateSign(dataMap));
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		Map<String, String> headers = new HashMap<String, String>(16);
		headers.put("Content-Type", "application/json");

//		String ret = HttpKit.post(Constant.DEV_URL, null, dataMap.toString(),
//				headers);
//
//		System.out.println(ret);
		/*
		 * Jedis jedis = new Jedis("gd.tetuijiudian.cn",6380,3000);
		 * jedis.auth("wfor8890"); // 如果 Redis 服务设置了密码，需要下面这行，没有就不需要 //
		 * jedis.auth("123456"); System.out.println("连接成功"); //查看服务是否运行
		 * System.out.println("服务正在运行: "+jedis.ping());
		 * 
		 * //如些可以取出正确值， RedisPlugin 是 redis的二次封装，只用先版的jedis 就可以取了 String
		 * fi=jedis.get("el_hotel_data_rp_94421672" ); System.out.println(fi);
		 */

//		Jedis jedis = RedisUtil.getConn();
//		System.out.println("jedis"+jedis);
//		String value = jedis.get("el_hotel_data_rp_94421672");
//		System.out.println(value);
//		//使用之后记得关闭连接
//		jedis.close();

	}

	/***
	 * 高德拉取酒店/房型信息变化的酒店id 【高德已连通】 biz_content
	 * {"type":"page","currentPage":1,"pageSize":20} 这个 是根据 艺龙文档
	 * https://open.elong.com/doc/info/cn-api-meta-hotel_incr_sharding_state
	 *
	 * 高德开发文档 https://y.amap.com/docs/hotel/getincrid
	 */
	public void getIncrIdList() {

		//

		String biz_content = getPara("biz_content");
		JSONObject jsonObject = JSONObject.parseObject(biz_content);
		String type = jsonObject.getString("type");
		int currentPage = jsonObject.getIntValue("currentPage");
		int pageSize = jsonObject.getIntValue("pageSize");

//		List<Long> hList= hsrvStuff.getIncrStateIdList(type,currentPage,pageSize);

		List<Long> hidList = new ArrayList<Long>();
		List<HotelroomChg> hotelList = hotelroomChgStuff.getHotelroomChgList(currentPage, pageSize);
		for (HotelroomChg hotel : hotelList) {
			hidList.add(hotel.getId());
		}

		ResponseUtils res = new ResponseUtils();
		res.setCode("10000");
		res.setMsg("success");

		Map<String, Object> rData = new HashMap<>();

		rData.put("HotelID", hidList);
//		res.setData(JSON.toJSON(hotelList ));
		res.setData(rData);
		ResponseUtilsOutResponse resOut = new ResponseUtilsOutResponse();
		resOut.setResponse(res);
		renderJson(resOut);

	}

	/**
	 * 商家推送酒店详情信息 【高德已连通】
	 *
	 * 入参 HotelID，
	 */
	public void pushPoiInfoList() {

		Long HotelID = getParaToLong("HotelID", 0L);
		String elongIdStr = get("elongId");

		if(HotelID==0&&elongIdStr.equals("")){
			ResponseUtils res = new ResponseUtils();
			res.setCode("40001");
			res.setMsg("参数不对");
			renderJson(res);
			return;
		}

		// Hotel mHotel= hsrv.getHotelInfo(HotelID);

		Hotel mHotel = hsrv.getHotelInfo(HotelID, elongIdStr);
		if (mHotel == null) {
			mHotel = hsrv.addHotel(elongIdStr);
		}

		System.out.println(mHotel);
		
		Map<String, String> dataMap = new HashMap<String, String>();
		dataMap.put("method", "amap.hotel.offline.pushPoiInfoList");
		dataMap.put("utc_timestamp", String.valueOf(System.currentTimeMillis()));
		dataMap.put("app_id", Constant.APP_ID);
		dataMap.put("version", "1.0");
		dataMap.put("sign_type", "RSA2");
		dataMap.put("charset", "UTF-8");

		JSONObject bizContent = new JSONObject();
		bizContent.put("HotelID", mHotel.getId());
		bizContent.put("HotelName", mHotel.getHotelName());
		bizContent.put("City", mHotel.getCityName());
		bizContent.put("Address", mHotel.getAddress());

		BigDecimal getGaodeLon = new BigDecimal(mHotel.getGaodeLon());
		
		bizContent.put("GDLon", getGaodeLon);
		
		 BigDecimal getGaodeLat = new BigDecimal(mHotel.getGaodeLat());
		bizContent.put("GDLat", getGaodeLat);
		bizContent.put("HotelType", mHotel.getHotelType());
		bizContent.put("Telephone", mHotel.getPhone());

		// 在推送 GDLat 这个数据的时候，有些问题 ，高德提示类型不正确，不知道为啥 ，我把这个字段去掉了，又可以了

//        bizContent.put("GDLat", "34.559049701");

		System.out.println(bizContent);
		dataMap.put("biz_content", JSON.toJSONString(bizContent));
		try {
			dataMap.put("sign", GenerateSignUtils.generateSign(dataMap));
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		Map<String, String> headers = new HashMap<String, String>(16);
		headers.put("Content-Type", "application/x-www-form-urlencoded");

		String prestr = StringUtils.asUrlParams(dataMap);

		String ret = HttpKit.post(Constant.DEV_URL, null, prestr, headers);
		System.out.println(ret);
		ResponseUtils res = new ResponseUtils();
		res.setCode("10000");
		res.setMsg("success");
		Map<Object, Object> dataMap2=new HashMap<Object, Object>();
		dataMap2.put("gdId", mHotel.getId());
		res.setDataJson(JSONObject.parseObject(JsonKit.toJson(dataMap2)));

		renderJson(res);
	}

}
