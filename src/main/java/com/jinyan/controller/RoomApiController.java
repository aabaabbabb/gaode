package com.jinyan.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.JSONLexer;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.base.Strings;
import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.aop.Inject;
import com.jfinal.core.ActionKey;
import com.jfinal.core.Path;
import com.jfinal.kit.HttpKit;
import com.jfinal.kit.JsonKit;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.ehcache.CacheKit;
import com.jinyan.common.Constant;
import com.jinyan.common.GdlogInterceptor;
import com.jinyan.controller.base.BaseController;
import com.jinyan.model.EbookingRoomType;
import com.jinyan.model.Hotel;
import com.jinyan.model.Pic;
import com.jinyan.service.*;
import com.jinyan.utils.*;

import redis.clients.jedis.Jedis;

//日志拦截器，影响不大
@Clear
@Before(GdlogInterceptor.class)
@Path(value = "/hotel/mall", viewPath = "")
public class RoomApiController extends BaseController {

	@Inject
	private HotelService hsrv;

	@Inject
	private RoomTypeDbService  serverRoomTypeDbService;

//	static Jedis jedis = RedisUtil.getConn();
	@Inject
	private PriceCache priceCache;
	/**
	 * 高德主动拉取酒店房型信息
	 */
//	@ActionKey("/hotel/mall/getRoomInfo")
	public void getRoomInfo() {

		boolean userdb= Constant.USEDB;
		if(userdb){
			getRoomInfoDB();
			return;
		}


//
//		String s="{\n" +
//				"\n" +
//				"  \"response\": {\n" +
//				"\n" +
//				"    \"code\": \"10000\",\n" +
//				"\n" +
//				"    \"msg\": \"success\",\n" +
//				"\n" +
//				"    \"data\": {\n" +
//				"\n" +
//				"      \"HotelID\": \"132589\",\n" +
//				"\n" +
//				"      \"RoomDetail\": [\n" +
//				"\n" +
//				"        {\n" +
//				"\n" +
//				"          \"RoomID\": \"77126789\",\n" +
//				"\n" +
//				"          \"RoomName\": \"大床房\",\n" +
//				"\n" +
//				"          \"RoomTypeStd\": \"1\",\n" +
//				"\n" +
//				"          \"Description\": \"\",\n" +
//				"\n" +
//				"          \"RoomQuantity\": 5,\n" +
//				"\n" +
//				"          \"AddBed\": \"1\",\n" +
//				"\n" +
//				"          \"AddBedFee\": 0,\n" +
//				"\n" +
//				"          \"AreaRange\": 60,\n" +
//				"\n" +
//				"          \"RoomPerson\": 3,\n" +
//				"\n" +
//				"          \"FloorRange\": \"3-8\",\n" +
//				"\n" +
//				"          \"Images\": [\n" +
//				"\n" +
//				"            {\n" +
//				"\n" +
//				"              \"ImageName\": \"酒店外观\",\n" +
//				"\n" +
//				"              \"ImageType\": \"酒吧\",\n" +
//				"\n" +
//				"              \"ImageUrl\": \"https:www.pic.com/pic1\"\n" +
//				"\n" +
//				"            }\n" +
//				"\n" +
//				"          ],\n" +
//				"\n" +
//				"          \"HasWindow\": 1,\n" +
//				"\n" +
//				"          \"HasManyBeds\": \"\",\n" +
//				"\n" +
//				"          \"BedQuantityTotal\": 3,\n" +
//				"\n" +
//				"          \"BedDetail\": [\n" +
//				"\n" +
//				"            {\n" +
//				"\n" +
//				"              \"BedQuantity\": 1,\n" +
//				"\n" +
//				"              \"BedWidth\": 0,\n" +
//				"\n" +
//				"              \"ChildBedType\": \"双人床\"\n" +
//				"\n" +
//				"            }\n" +
//				"\n" +
//				"          ],\n" +
//				"\n" +
//				"          \"WirelessBroadnet\": 1,\n" +
//				"\n" +
//				"          \"WiredBroadnet\": 1,\n" +
//				"\n" +
//				"          \"Bath\": \"独立卫浴\",\n" +
//				"\n" +
//				"          \"RelationReference\": {\n" +
//				"\n" +
//				"            \"CtripRoomTypeID\": \"123123\"\n" +
//				"\n" +
//				"          },\n" +
//				"\n" +
//				"          \"Facilities\": [\n" +
//				"\n" +
//				"            {\n" +
//				"\n" +
//				"              \"Category\": \"\",\n" +
//				"\n" +
//				"              \"CategoryName\": \"卫浴设施\",\n" +
//				"\n" +
//				"              \"FacilityName\": \"吹风机\",\n" +
//				"\n" +
//				"              \"FacilityCode\": \"\"\n" +
//				"\n" +
//				"            }\n" +
//				"\n" +
//				"          ]\n" +
//				"\n" +
//				"        }\n" +
//				"\n" +
//				"      ]\n" +
//				"\n" +
//				"    }\n" +
//				"\n" +
//				"  }\n" +
//				"\n" +
//				"}";
//
//		// 初步判断  高德官网的 BedDetail 验证有问题
////		String url="http://api.tetuijiudian.com/shuia/test/test5.php";
////		s=	HttpKit.get(url);
//
////		JSONObject jso=JSON.parseObject(s);
////		renderJson(jso);
////
////		if(1==1){
////			return;
////		}
//
//		String dataMap = getRequest().getParameter("biz_content");
//		JSONObject req_json = JSONObject.parseObject(dataMap);
//		String HotelID = req_json.getString("HotelID");
//		Hotel hotel = hsrv.getHotelInfo(Long.parseLong(HotelID)); // 这里要是 长整型，高德id 长整型，如果用整形的经常报错
////		String value = jedis.get("el_hotel_static_info_" + hotel.getElongId());
//
//		JSONObject data =new JSONObject(); //高德需要data json
//		data.put("HotelID",HotelID);
//
//		List<Map<String, Object>> roomList = new ArrayList<Map<String, Object>>();
//
//		JSONObject json_obj = JSONObject.parseObject(value).getJSONObject("Result");
//
//		JSONArray json_arr = json_obj.getJSONArray("Rooms");
//		for (int i = 0; i < json_arr.size(); i++) {
//			JSONObject room_json = json_arr.getJSONObject(i);
//			Map<String, Object> roomMap = new HashMap<String, Object>();
//			roomMap.put("RoomID", room_json.getString("RoomID"));
//			roomMap.put("RoomName", room_json.getString("RoomName"));
//
//			String BedType=room_json.getString("BedType");
//			roomMap.put("RoomTypeStd", "1"); //1-标准间
// 			if(BedType.contains("双床")){
//				roomMap.put("RoomTypeStd", "2");
//			}
//			if(BedType.contains("大床")){
//				roomMap.put("RoomTypeStd", "6");
//			}
////			roomMap.put("RoomTypeStd", room_json.getString("BedType"));
//
//
//
//			roomMap.put("RoomQuantity", room_json.getInteger("Amount"));
//
//			Double AreaRange= room_json.getDoubleValue("Area");
//			AreaRange= CommonUtils.decimal2(AreaRange);
//			roomMap.put("AreaRange", AreaRange);
//
//			roomMap.put("RoomPerson", room_json.getInteger("Capacity"));
//			roomMap.put("FloorRange", room_json.getString("Floor"));
//			roomMap.put("BedQuantityTotal", 1);
//			roomMap.put("AddBed", "0");
//
//			List<Map<String, Object>> bedList = new ArrayList<Map<String, Object>>();
//			Map<String, Object> bedMap = new HashMap<String, Object>();
//			String bedType=room_json.getString("BedType");
//			if(bedType.contains("双床")) {
//				bedMap.put("BedQuantity",2);
//				bedMap.put("BedWidth", 1.2);
//				bedMap.put("ChildBedType", bedType);
//			}else {
//				bedMap.put("BedQuantity",1);
//				bedMap.put("BedWidth", 1.8);
//				bedMap.put("ChildBedType", bedType);
//			}
//			bedList.add(bedMap);
//
//
//			roomMap.put("WirelessBroadnet", 4);
//			roomMap.put("WiredBroadnet", 4);
//			roomMap.put("Bath", "独立卫浴");
//
//			roomMap.put("BedDetail", bedList);  //暂时先去掉
//
//			roomList.add(roomMap);
//
//			break;
//		}
//
//		data.put("RoomDetail",roomList);
//
//		ResponseUtils res = new ResponseUtils();
//		res.setCode("10000");
//		res.setMsg("success");
////		res.setData(JsonKit.toJson(roomList));
//		res.setData(data); //要放data
////		renderJson(res);
//
//		ResponseUtilsOutResponse resOut=new ResponseUtilsOutResponse();
//		resOut.setResponse(res);
//		renderJson(resOut);
	}

	public void getRoomInfoDB() {

		String dataMap = getRequest().getParameter("biz_content");
		JSONObject req_json = JSONObject.parseObject(dataMap);
		String HotelID = req_json.getString("HotelID");
		Hotel hotel = hsrv.getHotelInfo(Long.parseLong(HotelID)); // 这里要是 长整型，高德id 长整型，如果用整形的经常报错

		JSONObject data =new JSONObject(); //高德需要data json
		data.put("HotelID",HotelID);
		List<Map<String, Object>> roomList = new ArrayList<Map<String, Object>>();

		String ElongId = hotel.getElongIdStr();

		List<EbookingRoomType>  roomListdb= serverRoomTypeDbService.getEbookingRoomTypeList(ElongId);

 		for (EbookingRoomType room : roomListdb) {
			Map<String, Object> roomMap = new HashMap<String, Object>();
			roomMap.put("RoomID", Integer.toString(room.getId())) ;
			roomMap.put("RoomName", room.getName());
			roomMap.put("Status", "1"); //todo 数据加现在全是 0

			String bed=room.getBed();
			String RoomTypeStd= serverRoomTypeDbService.BedConvertGaode(bed) ;
			roomMap.put("RoomTypeStd", RoomTypeStd);
			roomMap.put("RoomQuantity", room.getTotalCount());
			roomMap.put("AreaRange", serverRoomTypeDbService.AreaConvertGaode(room.getArea()));
			roomMap.put("RoomPerson",room.getPerson());
			roomMap.put("AddBed", "0");

			List<Map<String, Object>> bedList = new ArrayList<Map<String, Object>>();
			Map<String, Object> bedMap = new HashMap<String, Object>();
			String bedType=bed;
			if(bedType.contains("双床")) {
				bedMap.put("BedQuantity",serverRoomTypeDbService.BedQuantityTotal(bed));
				bedMap.put("BedWidth", serverRoomTypeDbService.BedWidth(bed));
				bedMap.put("ChildBedType", serverRoomTypeDbService.BedType(bed));
			}else {
				bedMap.put("BedQuantity",serverRoomTypeDbService.BedQuantityTotal(bed));
				bedMap.put("BedWidth", serverRoomTypeDbService.BedWidth(bed));
				bedMap.put("ChildBedType", serverRoomTypeDbService.BedType(bed));
			}
			bedList.add(bedMap);

			List<Object> bedListout = new ArrayList<>();
			bedListout.add(bedList);
			roomMap.put("BedDetail", bedListout);  //

			roomMap.put("FloorRange", StringUtils.nullToEmpty(room.getFloor()) );
			roomMap.put("BedQuantityTotal", serverRoomTypeDbService.BedQuantityTotal(bed));

			String Broadband = room.getBroadband();
			roomMap.put("WirelessBroadnet", serverRoomTypeDbService.WirelessBroadnet(Broadband));
			roomMap.put("WiredBroadnet", serverRoomTypeDbService.WirelessBroadnet(Broadband));
			roomMap.put("Bath", "未知");




			if(room.getIsElong()==0){ //如果是自营的话
				try{
					roomMap.put("HasWindow", room.getIsWindow());
					roomMap.put("RoomTypeStd", room.getRoomTypeStd().toString());
					roomMap.put("AddBed", room.getAddBed().toString());
					roomMap.put("AddBedFee", room.getAddBedFee());
					String bathh="未知";
					Integer b=room.getBath();
					if(b==1){
						bathh="独立卫浴";
					}
					if(b==2){
						bathh="公共卫浴";
					}
					roomMap.put("Bath",bathh);
					roomMap.put("BedQuantityTotal",room.getBedQuantityTotal());
					roomMap.put("WirelessBroadnet",room.getWirelessBroadnet());
					roomMap.put("WiredBroadnet",room.getWiredBroadnet());


					String Description=room.getDescription();
					if(!Strings.isNullOrEmpty(Description)){
						roomMap.put("Description", Description);
					}

					List<Map<String, Object>> bedList2 = new ArrayList<Map<String, Object>>();
					Map<String, Object> bedMap2 = new HashMap<String, Object>();
					bedMap2.put("BedQuantity",room.getBedQuantity());
					bedMap2.put("BedWidth",room.getBedWidth());
					bedMap2.put("ChildBedType",serverRoomTypeDbService.getChildBedType(room.getChildBedType()));
					bedList2.add(bedMap2);

					List<Object> bedListout2 = new ArrayList<>();
					bedListout2.add(bedList2);
					roomMap.put("BedDetail", bedListout2);  //

					if(!Strings.isNullOrEmpty(room.getImageUrl())){
						List<Map<String, Object>> Images = new ArrayList<Map<String, Object>>();
						Map<String, Object> ImagesMap = new HashMap<String, Object>();
//				ImagesMap.put("ImageType","");
//				ImagesMap.put("ImageName","酒店外观");
						ImagesMap.put("ImageUrl",room.getImageUrl());
						Images.add(ImagesMap);
						roomMap.put("Images", Images);  //
					}

				}catch (Exception e){
					continue; // 报错的先不存了。
				}

			}


			/* 添加图片 begin **/
			List<Pic>  picListdb=  serverRoomTypeDbService.getPicList(Integer.toString(room.getId()));
			if(picListdb.size()>0){
				List<Map<String, Object>> Imagespic = new ArrayList<Map<String, Object>>();
				for (Pic pic : picListdb) {
					Map<String, Object> ImagesMap = new HashMap<String, Object>();
					String imagetype=pic.getImagetype();
					String imagename=pic.getImagename();
					String ImageUrl=pic.getImageurl();
					if(!imagetype.isEmpty()){
						ImagesMap.put("ImageType",imagetype);
					}
					if(!imagename.isEmpty()){
						ImagesMap.put("ImageName",imagename);
					}

					if(!ImageUrl.isEmpty()){
						ImagesMap.put("ImageUrl",ImageUrl);
 						Imagespic.add(ImagesMap);
					}

				}
				roomMap.put("Images", Imagespic);  //
			}
			/* 添加图片 end **/


			roomList.add(roomMap);
 		}

		data.put("RoomDetail",roomList);

		ResponseUtils res = new ResponseUtils();
		res.setCode("10000");
		res.setMsg("success");
//		res.setData(JsonKit.toJson(roomList));
		res.setData(data); //要放data
//		renderJson(res);

		ResponseUtilsOutResponse resOut=new ResponseUtilsOutResponse();
		resOut.setResponse(res);
		renderJson(resOut);

	}


	/*

	{"HotelID":"3402355","RoomID":"0001","RatePlanID":"294665086","StartDate":"2023-09-01","EndDate":"2023-09-02"}
	 */
	public void getPrice() throws Exception {

		boolean userdb= Constant.USEDB;
		if(userdb){
			getPriceDB();
			return;
		}
		// 初步判断  高德官网的 BedDetail 验证有问题
//		String urlttt="http://api.tetuijiudian.com/shuia/test/test5.php";
//		String sttt=	HttpKit.get(urlttt);
//
//		JSONObject jso=JSON.parseObject(sttt);
//		renderJson(jso);
//		if(true){
//			return;
//		}

		String debugForceCacheParam = getRequest().getParameter("debugForceCache");

		String dataMap = getRequest().getParameter("biz_content");
		JSONObject req_json = JSONObject.parseObject(dataMap);
		String HotelID=req_json.getString("HotelID");
		Hotel hotel=	hsrv.getHotelInfo(Long.parseLong(HotelID));
		String StartDate=req_json.getString("StartDate");
		String EndDate=req_json.getString("EndDate");
		String RatePlanID=req_json.getString("RatePlanID");
		String RoomID=req_json.getString("RoomID");

		if(RoomID==null){
			RoomID="";
		}
		if(RatePlanID==null){
			RatePlanID="";
		}

		String  prestrReidsKey= String.format("%s|%s|%s|%s|%s", HotelID,StartDate,EndDate,RatePlanID,RoomID);

		priceCache.setCached(PriceCache.getCacheStatus());//开启缓存

//		JSONObject data_param=new JSONObject();
//		data_param.put("Version", "1.35");
//		data_param.put("Local", "zh_CN");
//
//		Map<String, Object> Request=new HashMap<String, Object>();
//		Request.put("ArrivalDate", StartDate);
//		Request.put("DepartureDate", EndDate);
//		Request.put("HotelIds", hotel.getElongId());
//		Request.put("Options", "2,12,14");
//		Request.put("PaymentType", "All");
//		data_param.put("Request", JsonKit.toJson(Request));

		JSONObject dataRequest_detail = new JSONObject();
		//{"ArrivalDate":"2023-08-29","DepartureDate":"2023-08-30","HotelIds":"40101006","Options":"2","PaymentType":"All"}
		dataRequest_detail.put("ArrivalDate",StartDate);
		dataRequest_detail.put("DepartureDate",EndDate);

		try {
			dataRequest_detail.put("HotelIds",hotel.getElongId()); //数据库没有的，返回空房型
		}catch (Exception e ){
			renderJson(priceCache.returnNoRoom(HotelID));
			return;
		}


		dataRequest_detail.put("Options","2,12,14");
		dataRequest_detail.put("PaymentType","All");

		String  prestr= JSON.toJSONString(dataRequest_detail);

		boolean debugForceCache=true; //测试时，强写数据到redis

		if(Strings.isNullOrEmpty(debugForceCacheParam)){
			debugForceCache=false;
		}

		boolean pressureTest =priceCache.getPressureTest(); //压测时，没有缓存的，直接返回 没房

		if(!debugForceCache){
			String cRoominfo=priceCache.getPrice(prestrReidsKey);
			if(!Strings.isNullOrEmpty(cRoominfo)){


				Boolean filterNoneRoom=false;

				if(filterNoneRoom){
					JSONObject outJson=JSON.parseObject(cRoominfo);

					JSONObject data =outJson.getJSONObject("response").getJSONObject("data");
					JSONArray RoomInfos = data.getJSONArray("RoomInfos");
					JSONArray RoomInfos2 = new JSONArray();

					for( Object RoomInfo : RoomInfos){
						JSONObject  RoomInfoObj=(JSONObject)RoomInfo;
						String RoomIDStr = RoomInfoObj.getString("RoomID");
						if(Strings.isNullOrEmpty(RoomIDStr)){
							continue;
						}

						Boolean haveNullRate=false;
						JSONArray RatePlansArrs = RoomInfoObj.getJSONArray("RatePlans");
						for( Object RatePlansObj : RatePlansArrs){
							if(RatePlansObj==null){
								haveNullRate=true;
								break;
							}
						}
						if(haveNullRate){
							continue;
						}
						RoomInfos2.add(RoomInfoObj);

					}
					data.put("RoomInfos",RoomInfos2);
					outJson.put("data",data);
					renderJson(RoomInfos2);
					return;
				}

//				cRoominfo=cRoominfo.replaceAll("priceInfo","PriceInfo");
				renderJson(cRoominfo);
				return;
			}

			if(pressureTest){
				renderJson(priceCache.returnNoRoom(HotelID));
				return;
			}
		}



		String data="{\"Version\":\"1.35\",\"Local\":\"zh_CN\",\"Request\":"+prestr+"}";
		String  url= null;
		try {
			String ElongApiUrlFormat="http://gd.tetuijiudian.cn:859/apivamap/rest?method=%s&data=%s";
			url = String.format(ElongApiUrlFormat,  "hotel.detail", URLEncoder.encode(data, "utf-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return;
		}

//		String url="http://gd.tetuijiudian.cn:859/apivamap/rest?method=hotel.detail&data="+data_param.toJSONString();

		//data 需要url编码
//		String elongRDATA=URLEncoder.encode(data_param.toJSONString(), "utf-8");
//		String url="http://gd.tetuijiudian.cn:859/apivamap/rest?method=hotel.detail&data="+elongRDATA;
		System.out.println("开始:"+prestrReidsKey);
		System.out.println("开始:"+url);

		String result;
		String cacheResult= priceCache.getElongPriceReq(data);
//		cacheResult=null;
		if(cacheResult!=null && !cacheResult.equals("")){
			  result=	cacheResult;
		}else{
			  result=	HttpKit.get(url);
			priceCache.setElongPriceReq(data,result,1200);//1200 20分钟
		}



		System.out.println("结束:"+url);
		JSONObject res_obj=JSONObject.parseObject(result).getJSONObject("Result");
		JSONArray hotel_list_json=res_obj.getJSONArray("Hotels");


		JSONObject room_info_json=new JSONObject();
		room_info_json.put("HotelID", HotelID);

		JSONArray gaode_RoomInfos = new JSONArray();//返回给高德的房型数组

		List<Map<String, Object>> RatePlans_list=new ArrayList<Map<String,Object>>(); //定义在这也没事，roomid 相同的时候，才加
//		List<Map<String, Object>> data_roominfo_list=new ArrayList<Map<String,Object>>();
		//循环酒店
		for (int i = 0; i < hotel_list_json.size(); i++) {
//			JSONArray room_list_json=hotel_list_json.getJSONArray(i);
			JSONObject hotelJson= (JSONObject) hotel_list_json.get(i);

			JSONArray room_list_json = hotelJson.getJSONArray("Rooms");

			if(room_list_json==null){
                room_info_json.put("RoomInfos", gaode_RoomInfos);
				continue;
			}

			//循环房型
			for (int j = 0; j < room_list_json.size(); j++) {
				JSONObject room=room_list_json.getJSONObject(j);
				JSONObject gaode_roomInfo=new JSONObject();

				String hereRoomID=	room.getString("RoomId");


				gaode_roomInfo.put("RoomID",hereRoomID);




				if(Strings.isNullOrEmpty(RoomID) || hereRoomID.equals(RoomID)) {

					JSONArray rate_plans_arr=room.getJSONArray("RatePlans");
					//循环plan

					RatePlans_list=new ArrayList<Map<String,Object>>();

					for (int k = 0; k < rate_plans_arr.size(); k++) {

						JSONObject rate_plan=rate_plans_arr.getJSONObject(k);
						String hereRatePlanID = rate_plan.getString("RatePlanId");
						if(Strings.isNullOrEmpty(RatePlanID) || hereRatePlanID.equals(RatePlanID)) {
							Map<String, Object> ratePlans_map=new HashMap<String, Object>();
							ratePlans_map.put("RatePlanID", rate_plan.get("RatePlanId"));
							ratePlans_map.put("RatePlanName", rate_plan.get("RatePlanName"));
							ratePlans_map.put("IsInstantConfirm", rate_plan.get("InstantConfirmation"));
							ratePlans_map.put("Stock", 999);//库存
							ratePlans_map.put("PayType", rate_plan.getString("PaymentType").equals("SelfPay")?"FG":"PP");
							//区分预付，现付，担保。规则不一样

							//价格日历开始===================
							Map<String, Object> priceInfo=new HashMap<String, Object>();

							Map<String,Object> total=new HashMap<String, Object>();

							//高德在这里要转换成分
							double totalP = rate_plan.getDouble("TotalRate");;
							totalP= CommonUtils.decimal2(totalP) * 100; //转换成分
							int AmountAfterTaxFee=(int)totalP;
							total.put("AmountAfterTaxFee", AmountAfterTaxFee);

							priceInfo.put("Total", total);

							List<Map<String, Object>> dailyPrices_list=new ArrayList<Map<String,Object>>();
							JSONArray nightlyRates_arr=rate_plan.getJSONArray("NightlyRates");
							for (int l = 0; l < nightlyRates_arr.size(); l++) {
								JSONObject nightlyRates_obj=nightlyRates_arr.getJSONObject(l);
								Map<String, Object> dailyPrice_map=new HashMap<String, Object>();
								SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
								String dateString = formatter.format(nightlyRates_obj.getDate("Date"));
								dailyPrice_map.put("Date", dateString);

//								dailyPrice_map.put("Price", nightlyRates_obj.get("Cost"));
								//价格 是 类型
								JSONObject Price=new JSONObject();
//								double Cost = nightlyRates_obj.getDouble("Cost");;
								double Cost = nightlyRates_obj.getDouble("Member");//这里显示会员价，不然和TotalRate 不一致，后边需要改了再说
								Cost= CommonUtils.decimal2(Cost) * 100; //转换成分
								int Price_AmountAfterTaxFee=(int)Cost;
								Price.put("AmountAfterTaxFee", Price_AmountAfterTaxFee);
								dailyPrice_map.put("Price",Price);

								dailyPrice_map.put("Breakfast", nightlyRates_obj.get("BreakfastCount"));
								dailyPrices_list.add(dailyPrice_map);
							}

//							priceInfo.put("DailyPrices", JsonKit.toJson(dailyPrices_list));
							priceInfo.put("DailyPrices",dailyPrices_list);
							//价格日历结束====================
							ratePlans_map.put("PriceInfo", priceInfo);

							ratePlans_map.put("LadderType", 1);
							List<Map<String, Object>> LadderDeductPolicyEntity_list=new ArrayList<Map<String,Object>>();
							Map<String, Object> LadderDeductPolicyEntity_map=new HashMap<String, Object>();

							LadderDeductPolicyEntity_map.put("Start", this.timeStringToLong(StartDate));
							LadderDeductPolicyEntity_map.put("End", this.timeStringToLong(EndDate));

							double LadderDeductPolicyEntityPrice=rate_plan.getDoubleValue("TotalRate");
							LadderDeductPolicyEntityPrice= CommonUtils.decimal2(LadderDeductPolicyEntityPrice) * 100; //转换成分
							int LadderDeductPolicyEntityPriceInt=(int)LadderDeductPolicyEntityPrice;
							LadderDeductPolicyEntity_map.put("Price",LadderDeductPolicyEntityPriceInt);

							LadderDeductPolicyEntity_list.add(LadderDeductPolicyEntity_map);
							ratePlans_map.put("LadderDeductPolicyList", LadderDeductPolicyEntity_list);
							RatePlans_list.add(ratePlans_map);
						}


						gaode_roomInfo.put("RatePlans",RatePlans_list);
					}


					gaode_RoomInfos.add(gaode_roomInfo);
				}



			}

			room_info_json.put("RoomInfos", gaode_RoomInfos);
		}

		System.out.println("结束循环酒店:");
//		ResponseUtils res = new ResponseUtils();
//		res.setCode("10000");
//		res.setMsg("success");
//		res.setData(JsonKit.toJson(room_info_json));
//		renderJson(JsonKit.toJson(res));

		ResponseUtils res = new ResponseUtils();
		res.setCode("10000");
		res.setMsg("success");
//		res.setData(JsonKit.toJson(roomList));
		res.setData(room_info_json); //要放data
//		renderJson(res);

		ResponseUtilsOutResponse resOut=new ResponseUtilsOutResponse();
		resOut.setResponse(res);

		if(priceCache.getCached()){
//			String s = JSON.toJSONString(list, SerializerFeature.DisableCircularReferenceDetect);

			String txt=JSON.toJSONString(resOut );
			System.out.println("开始写priceCache:");
			if(  RatePlans_list.size()>0){ //这里很重要，艺龙roomid 和roomtypeid多对多，空的roomid就不要存了，怕会覆盖掉其它的
				if(prestrReidsKey.contains("2023-08-19")){
					System.out.println("....");
				}
				priceCache.setPrice(prestrReidsKey,txt);
				System.out.println("有房价");
			}else{
				System.out.println("没房价");
			}

			System.out.println("结束写priceCache:");
		}


		renderJson(resOut);
	}

	public void getPriceDB() throws Exception {
		JSONObject room_info_json=new JSONObject();
		try {

			String utc_timestamp=getRequest().getParameter("utc_timestamp");
			String app_id=getRequest().getParameter("app_id");
			if(utc_timestamp==null){
				utc_timestamp="";
			}
			if(app_id==null){
				app_id="";
			}


			String dataMap = getRequest().getParameter("biz_content");
			JSONObject req_json = JSONObject.parseObject(dataMap);
			String HotelID=req_json.getString("HotelID");

			room_info_json.put("HotelID", HotelID);

			Hotel hotel=	hsrv.getHotelInfo(Long.parseLong(HotelID));

			if(hotel.getDown()==1){
				ResponseUtils res = new ResponseUtils();
				res.setCode("10000");
				res.setMsg("success");

				room_info_json.put("RoomInfos",new ArrayList<>());
				res.setData(room_info_json); //要放data

				ResponseUtilsOutResponse resOut=new ResponseUtilsOutResponse();
				resOut.setResponse(res);


				renderJson(resOut);
				return;
			}







			String StartDate=req_json.getString("StartDate");
			String EndDate=req_json.getString("EndDate");
			String RatePlanID=req_json.getString("RatePlanID");
			String RoomID=req_json.getString("RoomID");

			if(RoomID==null){
				RoomID="";
			}
			if(RatePlanID==null){
				RatePlanID="";
			}



			Integer elongidInt= hotel.getElongId();
			String HotelName= hotel.getHotelName();
			int InvoiceMode=hotel.getInvoiceMode();

			PriceDbService pdbs=new PriceDbService();


//			String elongid=Integer.toString(elongidInt);

			String elongid= hotel.getElongIdStr();
			if(elongid.isEmpty()){
				elongid=Integer.toString(elongidInt);
			}

			int userid=hotel.getUserid();
			int userpid=hotel.getUserpid();

			String cacheKey=  String.format("getPriceDB|%s|%s|%s|%s|%s", elongid,StartDate,EndDate,RatePlanID,RoomID);
			ResponseUtilsOutResponse	cacheResult = CacheKit.get("getPriceDB", cacheKey);

			if (cacheResult != null) {
				pdbs.ReordPrice(Long.parseLong(HotelID),elongid,userid,userpid,HotelName,utc_timestamp,  app_id,  RatePlanID,RoomID ,StartDate,EndDate,cacheResult, true);
				renderJson(cacheResult);
				return;
			}


			JSONArray gaode_RoomInfos = new JSONArray();//返回给高德的房型数组

			List<Map<String, Object>> RatePlans_list=new ArrayList<Map<String,Object>>(); //

			List<Record> records= pdbs.getPriceList(  elongid,  StartDate,    EndDate,  RatePlanID,  RoomID);

			Map<String, Object> RatePlans_Map=new HashMap<>(); //
			Map<String, Object> RoomInfos_Map=new HashMap<>(); //

			Map<String, List> RoomType_RatePlanids=new HashMap<>(); // roomid key, rateplanids 逗号list
			Map<String, List> RatePlanid_Dates=new HashMap<>(); //

			Map<String,String> one=new HashMap<>();// 这个是为了 不让 room 下边的 rateplan 重复添加

			//price_plan_id 用这个 id 当作map key存起来

			for (Record record : records) {
				String price_plan_id=record.getStr("price_plan_id");
				String room_type_id=record.getStr("room_type_id");
				String date=record.getStr("date");
				String payway=record.getStr("payway"); //支付方式 1现付 2预付
				int status= StringUtils.nullToEmpty(record.getInt("status"));
				int planstatus= StringUtils.nullToEmpty(record.getInt("planstatus"));


				if(Strings.isNullOrEmpty(room_type_id)){
					continue;
				}

				if(Strings.isNullOrEmpty(price_plan_id)){
					continue;
				}

				if(payway==null || !payway.equals("2")){ //过滤掉 只要预付
					continue;
				}

				if(status==0){ //过滤掉 0
					continue;
				}
				if(planstatus==0){ //过滤掉 0
					continue;
				}


				/***** 存 RoomType_RatePlanids *****/
				List RatePlanids;

				if(!one.containsKey(room_type_id+price_plan_id)){
					if(!RoomType_RatePlanids.containsKey(room_type_id)){
						RatePlanids=new ArrayList();
					}else{
						RatePlanids=RoomType_RatePlanids.get(room_type_id);
					}

					RatePlanids.add(price_plan_id);
					RoomType_RatePlanids.put(room_type_id,RatePlanids);

					one.put(room_type_id+price_plan_id,"1");
				}


				/***** RoomType_RatePlanids 结束*****/


				/*****   RatePlanid_Dates begin *****/
				List dateList;
				if(!RatePlanid_Dates.containsKey(price_plan_id)){
					dateList=new ArrayList();
				}else{
					dateList=RatePlanid_Dates.get(price_plan_id);
				}
				dateList.add(record);
				RatePlanid_Dates.put(price_plan_id,dateList);
				/***** RatePlanid_Dates end *****/

			}

			JSONArray RoomInfos=new JSONArray();

			Iterator<Map.Entry<String,List>> iterable=RoomType_RatePlanids.entrySet().iterator();
			while(iterable.hasNext()){
				Map.Entry<String,List>entry=iterable.next();
				System.out.println(entry.getKey()+"->"+entry.getValue());

				String key=entry.getKey();
				List valueRatePlanids=entry.getValue();

				JSONObject RoomInfo=new JSONObject();
				JSONArray RatePlans=new JSONArray();

				RoomInfo.put("RoomID",key);
				int valueRatePlanidsLen=valueRatePlanids.size();
				for (int i = 0; i < valueRatePlanidsLen; i++) {
					JSONObject RatePlan=new JSONObject();
//						System.out.println(valueRatePlanids.get(i));
					String valueRatePlanid=(String) valueRatePlanids.get(i);
					RatePlan.put("RatePlanID",valueRatePlanid);

					JSONObject PriceInfo = new JSONObject();
					JSONArray DailyPrices = new JSONArray();

					List RatePlanid_DatesLists=RatePlanid_Dates.get(valueRatePlanid);
					int RatePlanidDatesListsLen=RatePlanid_DatesLists.size();

					int Price_Total_AmountAfterTaxFee=0;
					Record valueRecode = null; //每次循环
					int FirstDAYPrice = 0;


					int roomstock=0;//
					for (int ri = 0; ri < RatePlanidDatesListsLen; ri++) {
						valueRecode=(Record) RatePlanid_DatesLists.get(ri);


						JSONObject DailyPrice = new JSONObject();
						String dates=valueRecode.getStr("date");
						DailyPrice.put("Date",dates);
						int breakfast=StringUtils.nullToEmpty(valueRecode.getInt("breakfast"));
						DailyPrice.put("Breakfast",breakfast);



						//价格
						JSONObject Price=new JSONObject();
						double Cost = valueRecode.getDouble("price");//
						int employ_role_id =valueRecode.getInt("employ_role_id");
						if(employ_role_id==4){
							Cost=CommonUtils.FixCostPrice(Cost); //底价处理成卖价
						}



						Cost= CommonUtils.decimal2(Cost) * 100; //转换成分
						int Price_AmountAfterTaxFee=(int)Cost;
						Price.put("AmountAfterTaxFee", Price_AmountAfterTaxFee);
						DailyPrice.put("Price",Price);

						Price_Total_AmountAfterTaxFee=Price_Total_AmountAfterTaxFee+Price_AmountAfterTaxFee;

						DailyPrices.add(DailyPrice);

						if(dates.equals(StartDate)){
							FirstDAYPrice=Price_AmountAfterTaxFee;
						}

//					System.out.println(valueRecode);

						int roomstocktemp= StringUtils.nullToEmpty(valueRecode.getInt("roomstock"));
						int roomstockreal= StringUtils.nullToEmpty(valueRecode.getInt("roomstockreal"));
						int todaystock= StringUtils.nullToEmpty(valueRecode.getInt("todaystock"));
						int todaystockreal= StringUtils.nullToEmpty(valueRecode.getInt("todaystockreal"));

						if(todaystock<roomstocktemp){
							roomstocktemp=todaystockreal;
						}else{
							roomstocktemp=roomstockreal;
						}

						if(ri==0){
							roomstock=roomstocktemp;
						}else{
							if(roomstocktemp<roomstock){
								roomstock=roomstocktemp;
							}
						}




					}

					if(valueRecode!=null){ //


						RatePlan.put("InvoiceMode",2); // 1=酒店开票,2=CP提供发票

						//目前全作预付的
						String PayWay=valueRecode.getStr("payway");
						RatePlan.put("PayType","PP"); //默认预付
						if(PayWay.equals("1")){
							RatePlan.put("PayType","FG");
						}
//						int roomstock= StringUtils.nullToEmpty(valueRecode.getInt("roomstock"));

//						int roomstock= StringUtils.nullToEmpty(valueRecode.getInt("roomstock"));
//						int roomstockreal= StringUtils.nullToEmpty(valueRecode.getInt("roomstockreal"));
//						int todaystock= StringUtils.nullToEmpty(valueRecode.getInt("todaystock"));
//						int todaystockreal= StringUtils.nullToEmpty(valueRecode.getInt("todaystockreal"));
//
//						if(todaystock<roomstock){ //stock 和 today_stock 两个值 如果today_stock < stock 取today_stock  否则取stock
//							roomstock=todaystockreal;
//						}else{
//							roomstock=roomstockreal;
//						}


						RatePlan.put("Stock",roomstock);

						String planname=  StringUtils.nullToEmpty(valueRecode.getStr("planname")) ;
						RatePlan.put("RatePlanName",planname);

						RatePlan.put("IsInstantConfirm",false);

						if(InvoiceMode==1 ||  InvoiceMode==2){
							RatePlan.put("InvoiceMode",InvoiceMode);
						}


						int is_cancel= StringUtils.nullToEmpty(valueRecode.getInt("is_cancel"));
						//1:可以取消；2：不可取消
						if(is_cancel==0){
							is_cancel=2;
						}

						int LadderType=1; //
						if(is_cancel==2){  	//1:可以取消；2：不可取消


							Map<String, Object> LadderDeductPolicyEntity_map=new HashMap<String, Object>();

							LadderDeductPolicyEntity_map.put("Start", this.timeStringToLong("2023-01-01"));
							LadderDeductPolicyEntity_map.put("End", this.timeStringToLong(EndDate));
							LadderDeductPolicyEntity_map.put("Price", Price_Total_AmountAfterTaxFee);

							List<Map<String, Object>> LadderDeductPolicyEntity_list=new ArrayList<Map<String,Object>>();
							LadderDeductPolicyEntity_list.add(LadderDeductPolicyEntity_map);
							RatePlan.put("LadderDeductPolicyList", LadderDeductPolicyEntity_list);
						}else{ // 是阶梯 可取消
							LadderType=3;  // 取消的话，是阶梯 不可取消
							List<Map<String, Object>> LadderDeductPolicyEntity_list=new ArrayList<Map<String,Object>>();
							//这个是要扣费的 0:不扣费；1:金额；2：比例；3：首晚房费; 4：全部房费
							int cut_type= StringUtils.nullToEmpty(valueRecode.getInt("cut_type"));
							if(cut_type==0){
								int cutfromtime= 2* 24 * 60 * 60 * 1000; //从开始时间再加一天，到这天
								Long fromtime= this.timeStringToLongCut(StartDate,cutfromtime);


								Map<String, Object> LadderDeductPolicyEntity_map=new HashMap<String, Object>(); //不扣费
								LadderDeductPolicyEntity_map.put("Start", fromtime);
								LadderDeductPolicyEntity_map.put("End", this.timeStringToLong(EndDate));
								LadderDeductPolicyEntity_map.put("Price",0);
								LadderDeductPolicyEntity_list.add(LadderDeductPolicyEntity_map);
							}else{
								int advance_day= StringUtils.nullToEmpty(valueRecode.getInt("advance_day"));
								String specific_date= StringUtils.nullToEmpty(valueRecode.getStr("specific_date"));
								int hour = 0;
								int minute = 0;
								int second = 0;

								if(!Strings.isNullOrEmpty(specific_date)){
									try{
										String[] dsplit	= specific_date.split(":");
										hour=Integer.parseInt(dsplit[0]);
										minute=Integer.parseInt(dsplit[1]);

										String[] dsplit2	= specific_date.split(".");
										second=Integer.parseInt(dsplit2[1]);
									}catch (Exception e){

									}


								}

								int cuts= advance_day   * 24 * 60 * 60 * 1000;
								cuts=cuts - hour* 60 * 60 * 1000;
								cuts=cuts -  minute* 60 * 1000;
								cuts=cuts -   second  * 1000;

								//计算减多少时间

//


								Long starttime= this.timeStringToLong(StartDate);
								Long midtime= starttime-cuts;
								Long fromtime= midtime -  1* 24 * 60 * 60 * 1000 ; //多减一天



								Map<String, Object> LadderDeductPolicyEntity_map=new HashMap<String, Object>(); //不扣费
								LadderDeductPolicyEntity_map.put("Start", fromtime);
								LadderDeductPolicyEntity_map.put("End", midtime);
								LadderDeductPolicyEntity_map.put("Price",0);
								LadderDeductPolicyEntity_list.add(LadderDeductPolicyEntity_map);


								Map<String, Object> LadderDeductPolicyEntity_map_pay=new HashMap<String, Object>();
								LadderDeductPolicyEntity_map_pay.put("Start", midtime);
								LadderDeductPolicyEntity_map_pay.put("End", this.timeStringToLong(EndDate));


								int cutPrice = Price_Total_AmountAfterTaxFee;//默认扣全部费用
								double planamount= StringUtils.nullToEmpty(valueRecode.getDouble("planamount"));
								if(cut_type==1){ //0:不扣费；1:金额；2：比例；3：首晚房费; 4：全部房费


									if(planamount>0){

										double Cost= CommonUtils.decimal2(planamount) * 100; //转换成分
										int Price_AmountAfterTaxFee=(int)Cost;
										cutPrice=Price_AmountAfterTaxFee;
									}
								}
								if(cut_type==2){ //


									if(planamount>0){

										double Cost= Price_Total_AmountAfterTaxFee * CommonUtils.decimal2(planamount*0.01) ; //转换成分
										int Price_AmountAfterTaxFee=(int)Cost;
										cutPrice=Price_AmountAfterTaxFee;
									}
								}

								if(cut_type==3){ //


									cutPrice=FirstDAYPrice;
								}
								if(cutPrice<1){ //
									cutPrice=Price_Total_AmountAfterTaxFee;
								}



								LadderDeductPolicyEntity_map_pay.put("Price", cutPrice);
								LadderDeductPolicyEntity_list.add(LadderDeductPolicyEntity_map_pay);


							}


							RatePlan.put("LadderDeductPolicyList", LadderDeductPolicyEntity_list);


						}
						RatePlan.put("LadderType", LadderType);


					}


					JSONObject Total=new JSONObject();
					Total.put("AmountAfterTaxFee", Price_Total_AmountAfterTaxFee);

					PriceInfo.put("DailyPrices",DailyPrices);
					PriceInfo.put("Total",Total);



					RatePlan.put("PriceInfo",PriceInfo);

					RatePlans.add(RatePlan);
				}

				RoomInfo.put("RatePlans",RatePlans);

				RoomInfos.add(RoomInfo);

			}

			room_info_json.put("RoomInfos", RoomInfos);


			ResponseUtils res = new ResponseUtils();
			res.setCode("10000");
			res.setMsg("success");
			res.setData(room_info_json); //要放data

			ResponseUtilsOutResponse resOut=new ResponseUtilsOutResponse();
			resOut.setResponse(res);

			CacheKit.put("getPriceDB", cacheKey,resOut);

			pdbs.ReordPrice(Long.parseLong(HotelID),elongid,userid,userpid,HotelName,utc_timestamp,  app_id,  RatePlanID,RoomID ,StartDate,EndDate,resOut, false);

			renderJson(resOut);

		}catch (Exception e){
			e.printStackTrace();
			ResponseUtils res = new ResponseUtils();
			res.setCode("10000");
			res.setMsg("success");

			room_info_json.put("RoomInfos",new ArrayList<>());
			res.setData(room_info_json); //要放data

			ResponseUtilsOutResponse resOut=new ResponseUtilsOutResponse();
			resOut.setResponse(res);


			renderJson(resOut);


		}
	}
	//
	public void getPriceDBPress4RedisMem() throws Exception {
		JSONObject room_info_json=new JSONObject();
		try {


			String dataMap = getRequest().getParameter("biz_content");
			JSONObject req_json = JSONObject.parseObject(dataMap);
			String HotelID=req_json.getString("HotelID");

			room_info_json.put("HotelID", HotelID);

			Hotel hotel=	hsrv.getHotelInfo(Long.parseLong(HotelID));
			String StartDate=req_json.getString("StartDate");
			String EndDate=req_json.getString("EndDate");
			String RatePlanID=req_json.getString("RatePlanID");
			String RoomID=req_json.getString("RoomID");

			if(RoomID==null){
				RoomID="";
			}
			if(RatePlanID==null){
				RatePlanID="";
			}



			Integer elongidInt= hotel.getElongId();

			PriceDbService pdbs=new PriceDbService();
//			String elongid=Integer.toString(elongidInt);
			String elongid= hotel.getElongIdStr();
			if(elongid.isEmpty()){
				elongid=Integer.toString(elongidInt);
			}


			String cacheKey=  String.format("getPriceDBPress3Ledis|%s|%s|%s|%s|%s", elongid,StartDate,EndDate,RatePlanID,RoomID);
			String	cacheResult = RedisMemCache.getCache(  cacheKey);

			if (cacheResult != null) {
				renderJson(cacheResult);
				return;
			}


			JSONArray gaode_RoomInfos = new JSONArray();//返回给高德的房型数组

			List<Map<String, Object>> RatePlans_list=new ArrayList<Map<String,Object>>(); //

			List<Record> records= pdbs.getPriceList(  elongid,  StartDate,    EndDate,  RatePlanID,  RoomID);

			Map<String, Object> RatePlans_Map=new HashMap<>(); //
			Map<String, Object> RoomInfos_Map=new HashMap<>(); //

			Map<String, List> RoomType_RatePlanids=new HashMap<>(); // roomid key, rateplanids 逗号list
			Map<String, List> RatePlanid_Dates=new HashMap<>(); //

			Map<String,String> one=new HashMap<>();// 这个是为了 不让 room 下边的 rateplan 重复添加

			//price_plan_id 用这个 id 当作map key存起来

			for (Record record : records) {
				String price_plan_id=record.getStr("price_plan_id");
				String room_type_id=record.getStr("room_type_id");
				String date=record.getStr("date");
				String payway=record.getStr("payway"); //支付方式 1现付 2预付
				int status= StringUtils.nullToEmpty(record.getInt("status"));
				int planstatus= StringUtils.nullToEmpty(record.getInt("planstatus"));

				if(Strings.isNullOrEmpty(room_type_id)){
					continue;
				}

				if(Strings.isNullOrEmpty(price_plan_id)){
					continue;
				}

				if(payway==null || !payway.equals("2")){ //过滤掉 只要预付
					continue;
				}

				if(status==0){ //过滤掉 0
					continue;
				}
				if(planstatus==0){ //过滤掉 0
					continue;
				}


				/***** 存 RoomType_RatePlanids *****/
				List RatePlanids;

				if(!one.containsKey(room_type_id+price_plan_id)){
					if(!RoomType_RatePlanids.containsKey(room_type_id)){
						RatePlanids=new ArrayList();
					}else{
						RatePlanids=RoomType_RatePlanids.get(room_type_id);
					}

					RatePlanids.add(price_plan_id);
					RoomType_RatePlanids.put(room_type_id,RatePlanids);

					one.put(room_type_id+price_plan_id,"1");
				}


				/***** RoomType_RatePlanids 结束*****/


				/*****   RatePlanid_Dates begin *****/
				List dateList;
				if(!RatePlanid_Dates.containsKey(price_plan_id)){
					dateList=new ArrayList();
				}else{
					dateList=RatePlanid_Dates.get(price_plan_id);
				}
				dateList.add(record);
				RatePlanid_Dates.put(price_plan_id,dateList);
				/***** RatePlanid_Dates end *****/

			}

			JSONArray RoomInfos=new JSONArray();

			Iterator<Map.Entry<String,List>> iterable=RoomType_RatePlanids.entrySet().iterator();
			while(iterable.hasNext()){
				Map.Entry<String,List>entry=iterable.next();
				System.out.println(entry.getKey()+"->"+entry.getValue());

				String key=entry.getKey();
				List valueRatePlanids=entry.getValue();

				JSONObject RoomInfo=new JSONObject();
				JSONArray RatePlans=new JSONArray();

				RoomInfo.put("RoomID",key);
				int valueRatePlanidsLen=valueRatePlanids.size();
				for (int i = 0; i < valueRatePlanidsLen; i++) {
					JSONObject RatePlan=new JSONObject();
//						System.out.println(valueRatePlanids.get(i));
					String valueRatePlanid=(String) valueRatePlanids.get(i);
					RatePlan.put("RatePlanID",valueRatePlanid);

					JSONObject PriceInfo = new JSONObject();
					JSONArray DailyPrices = new JSONArray();

					List RatePlanid_DatesLists=RatePlanid_Dates.get(valueRatePlanid);
					int RatePlanidDatesListsLen=RatePlanid_DatesLists.size();

					int Price_Total_AmountAfterTaxFee=0;
					Record valueRecode = null; //每次循环
					int FirstDAYPrice = 0;
					for (int ri = 0; ri < RatePlanidDatesListsLen; ri++) {
						valueRecode=(Record) RatePlanid_DatesLists.get(ri);


						JSONObject DailyPrice = new JSONObject();
						String dates=valueRecode.getStr("date");
						DailyPrice.put("Date",dates);
						int breakfast=StringUtils.nullToEmpty(valueRecode.getInt("breakfast"));
						DailyPrice.put("Breakfast",breakfast);



						//价格
						JSONObject Price=new JSONObject();
						double Cost = valueRecode.getDouble("price");//
						int employ_role_id =valueRecode.getInt("employ_role_id");
						if(employ_role_id==4){
							Cost=CommonUtils.FixCostPrice(Cost); //底价处理成卖价
						}


						Cost= CommonUtils.decimal2(Cost) * 100; //转换成分
						int Price_AmountAfterTaxFee=(int)Cost;
						Price.put("AmountAfterTaxFee", Price_AmountAfterTaxFee);
						DailyPrice.put("Price",Price);

						Price_Total_AmountAfterTaxFee=Price_Total_AmountAfterTaxFee+Price_AmountAfterTaxFee;

						DailyPrices.add(DailyPrice);

						if(dates.equals(StartDate)){
							FirstDAYPrice=Price_AmountAfterTaxFee;
						}

//					System.out.println(valueRecode);
					}

					if(valueRecode!=null){ //把  rateplan的其它字段


						//目前全作预付的
						String PayWay=valueRecode.getStr("payway");
						RatePlan.put("PayType","PP"); //默认预付
						if(PayWay.equals("1")){
							RatePlan.put("PayType","FG");
						}

						int roomstock= StringUtils.nullToEmpty(valueRecode.getInt("roomstock"));
						RatePlan.put("Stock",roomstock);

						String planname=  StringUtils.nullToEmpty(valueRecode.getStr("planname")) ;
						RatePlan.put("RatePlanName",planname);

						RatePlan.put("IsInstantConfirm",false);


						int is_cancel= StringUtils.nullToEmpty(valueRecode.getInt("is_cancel"));
						//1:可以取消；2：不可取消
						if(is_cancel==0){
							is_cancel=2;
						}

						int LadderType=1; //1=不可取消。2=入住前任意时间段免费取消。3=阶梯取消扣款政策,和LadderDeductPolicyEntity共存。
						if(is_cancel==2){  	//1:可以取消；2：不可取消


							Map<String, Object> LadderDeductPolicyEntity_map=new HashMap<String, Object>();

							LadderDeductPolicyEntity_map.put("Start", this.timeStringToLong("2023-01-01"));
							LadderDeductPolicyEntity_map.put("End", this.timeStringToLong(EndDate));
							LadderDeductPolicyEntity_map.put("Price", Price_Total_AmountAfterTaxFee);

							List<Map<String, Object>> LadderDeductPolicyEntity_list=new ArrayList<Map<String,Object>>();
							LadderDeductPolicyEntity_list.add(LadderDeductPolicyEntity_map);
							RatePlan.put("LadderDeductPolicyList", LadderDeductPolicyEntity_list);
						}else{ // 是阶梯 可取消
							LadderType=3;  // 取消的话，是阶梯 不可取消
							List<Map<String, Object>> LadderDeductPolicyEntity_list=new ArrayList<Map<String,Object>>();
							//这个是要扣费的 0:不扣费；1:金额；2：比例；3：首晚房费; 4：全部房费
							int cut_type= StringUtils.nullToEmpty(valueRecode.getInt("cut_type"));
							if(cut_type==0){
								int cutfromtime= 2* 24 * 60 * 60 * 1000; //从开始时间再加一天，到这天
								Long fromtime= this.timeStringToLongCut(StartDate,cutfromtime);


								Map<String, Object> LadderDeductPolicyEntity_map=new HashMap<String, Object>(); //不扣费
								LadderDeductPolicyEntity_map.put("Start", fromtime);
								LadderDeductPolicyEntity_map.put("End", this.timeStringToLong(EndDate));
								LadderDeductPolicyEntity_map.put("Price",0);
								LadderDeductPolicyEntity_list.add(LadderDeductPolicyEntity_map);
							}else{
								int advance_day= StringUtils.nullToEmpty(valueRecode.getInt("advance_day"));
								String specific_date= StringUtils.nullToEmpty(valueRecode.getStr("specific_date"));
								int hour = 0;
								int minute = 0;
								int second = 0;

								if(!Strings.isNullOrEmpty(specific_date)){
									try{
										String[] dsplit	= specific_date.split(":");
										hour=Integer.parseInt(dsplit[0]);
										minute=Integer.parseInt(dsplit[1]);

										String[] dsplit2	= specific_date.split(".");
										second=Integer.parseInt(dsplit2[1]);
									}catch (Exception e){

									}


								}

								int cuts= advance_day   * 24 * 60 * 60 * 1000;
								cuts=cuts+ hour* 60 * 60 * 1000;
								cuts=cuts+  minute* 60 * 1000;
								cuts=cuts+  second  * 1000;

								//计算减多少时间

//								int cutfromtime=cuts+ 1* 24 * 60 * 60 * 1000; //从开始时间再加一天，到这天


								Long starttime= this.timeStringToLong(StartDate);
								Long midtime= starttime-cuts;
								Long fromtime= midtime -  1* 24 * 60 * 60 * 1000 ; //多减一天



								Map<String, Object> LadderDeductPolicyEntity_map=new HashMap<String, Object>(); //不扣费
								LadderDeductPolicyEntity_map.put("Start", fromtime);
								LadderDeductPolicyEntity_map.put("End", midtime);
								LadderDeductPolicyEntity_map.put("Price",0);
								LadderDeductPolicyEntity_list.add(LadderDeductPolicyEntity_map);


								Map<String, Object> LadderDeductPolicyEntity_map_pay=new HashMap<String, Object>();
								LadderDeductPolicyEntity_map_pay.put("Start", midtime);
								LadderDeductPolicyEntity_map_pay.put("End", this.timeStringToLong(EndDate));


								int cutPrice = Price_Total_AmountAfterTaxFee;//默认扣全部费用
								double planamount= StringUtils.nullToEmpty(valueRecode.getDouble("planamount"));
								if(cut_type==1){ //0:不扣费；1:金额；2：比例；3：首晚房费; 4：全部房费


									if(planamount>0){

										double Cost= CommonUtils.decimal2(planamount) * 100; //转换成分
										int Price_AmountAfterTaxFee=(int)Cost;
										cutPrice=Price_AmountAfterTaxFee;
									}
								}
								if(cut_type==2){ //0:不扣费；1:金额；2：比例；3：首晚房费; 4：全部房费


									if(planamount>0){

										double Cost= Price_Total_AmountAfterTaxFee * CommonUtils.decimal2(planamount) * 100; //转换成分
										int Price_AmountAfterTaxFee=(int)Cost;
										cutPrice=Price_AmountAfterTaxFee;
									}
								}

								if(cut_type==3){ //0:不扣费；1:金额；2：比例；3：首晚房费; 4：全部房费


									cutPrice=FirstDAYPrice;
								}
								if(cutPrice<1){ //
									cutPrice=Price_Total_AmountAfterTaxFee;
								}



								LadderDeductPolicyEntity_map_pay.put("Price", cutPrice);
								LadderDeductPolicyEntity_list.add(LadderDeductPolicyEntity_map_pay);


							}


							RatePlan.put("LadderDeductPolicyList", LadderDeductPolicyEntity_list);


						}
						RatePlan.put("LadderType", LadderType);


					}


					JSONObject Total=new JSONObject();
					Total.put("AmountAfterTaxFee", Price_Total_AmountAfterTaxFee);

					PriceInfo.put("DailyPrices",DailyPrices);
					PriceInfo.put("Total",Total);



					RatePlan.put("PriceInfo",PriceInfo);

					RatePlans.add(RatePlan);
				}

				RoomInfo.put("RatePlans",RatePlans);

				RoomInfos.add(RoomInfo);

			}

			room_info_json.put("RoomInfos", RoomInfos);


			ResponseUtils res = new ResponseUtils();
			res.setCode("10000");
			res.setMsg("success");
			res.setData(room_info_json); //要放data

			ResponseUtilsOutResponse resOut=new ResponseUtilsOutResponse();
			resOut.setResponse(res);

//		 CacheKit.put("getPriceDB", cacheKey,resOut);
			String txt=JSON.toJSONString(resOut );
			RedisMemCache.setCache(  cacheKey,txt,30);


			renderJson(resOut);

		}catch (Exception e){

			ResponseUtils res = new ResponseUtils();
			res.setCode("10000");
			res.setMsg("success");

			room_info_json.put("RoomInfos",new ArrayList<>());
			res.setData(room_info_json); //要放data

			ResponseUtilsOutResponse resOut=new ResponseUtilsOutResponse();
			resOut.setResponse(res);




			renderJson(resOut);


		}
	}
	//测试发现，其实性能也不提高
	public void getPriceDBPress3Ledis() throws Exception {
		JSONObject room_info_json=new JSONObject();
		try {


		String dataMap = getRequest().getParameter("biz_content");
		JSONObject req_json = JSONObject.parseObject(dataMap);
		String HotelID=req_json.getString("HotelID");

        room_info_json.put("HotelID", HotelID);

		Hotel hotel=	hsrv.getHotelInfo(Long.parseLong(HotelID));
		String StartDate=req_json.getString("StartDate");
		String EndDate=req_json.getString("EndDate");
		String RatePlanID=req_json.getString("RatePlanID");
		String RoomID=req_json.getString("RoomID");

		if(RoomID==null){
			RoomID="";
		}
		if(RatePlanID==null){
			RatePlanID="";
		}



		Integer elongidInt= hotel.getElongId();

		PriceDbService pdbs=new PriceDbService();
		String elongid=Integer.toString(elongidInt);


		String cacheKey=  String.format("getPriceDBPress3Ledis|%s|%s|%s|%s|%s", elongid,StartDate,EndDate,RatePlanID,RoomID);
		 String	cacheResult = LedisdbCache.getCache(  cacheKey);

		if (cacheResult != null) {
			renderJson(cacheResult);
			return;
		}


		JSONArray gaode_RoomInfos = new JSONArray();//返回给高德的房型数组

		List<Map<String, Object>> RatePlans_list=new ArrayList<Map<String,Object>>(); //

		List<Record> records= pdbs.getPriceList(  elongid,  StartDate,    EndDate,  RatePlanID,  RoomID);

		Map<String, Object> RatePlans_Map=new HashMap<>(); //
		Map<String, Object> RoomInfos_Map=new HashMap<>(); //

		Map<String, List> RoomType_RatePlanids=new HashMap<>(); // roomid key, rateplanids 逗号list
		Map<String, List> RatePlanid_Dates=new HashMap<>(); //

		Map<String,String> one=new HashMap<>();//

		//

		for (Record record : records) {
			String price_plan_id=record.getStr("price_plan_id");
			String room_type_id=record.getStr("room_type_id");
			String date=record.getStr("date");
			String payway=record.getStr("payway"); //支付方式 1现付 2预付
			int status= StringUtils.nullToEmpty(record.getInt("status"));
			int planstatus= StringUtils.nullToEmpty(record.getInt("planstatus"));

			if(Strings.isNullOrEmpty(room_type_id)){
				continue;
			}

			if(Strings.isNullOrEmpty(price_plan_id)){
				continue;
			}

			if(payway==null || !payway.equals("2")){ //过滤掉 只要预付
				continue;
			}

			if(status==0){ //过滤掉 0
				continue;
			}
			if(planstatus==0){ //过滤掉 0
				continue;
			}


			/***** 存 RoomType_RatePlanids *****/
			List RatePlanids;

			if(!one.containsKey(room_type_id+price_plan_id)){
				if(!RoomType_RatePlanids.containsKey(room_type_id)){
					RatePlanids=new ArrayList();
				}else{
					RatePlanids=RoomType_RatePlanids.get(room_type_id);
				}

				RatePlanids.add(price_plan_id);
				RoomType_RatePlanids.put(room_type_id,RatePlanids);

				one.put(room_type_id+price_plan_id,"1");
			}


			/***** RoomType_RatePlanids 结束*****/


			/*****   RatePlanid_Dates begin *****/
			List dateList;
			if(!RatePlanid_Dates.containsKey(price_plan_id)){
				dateList=new ArrayList();
			}else{
				dateList=RatePlanid_Dates.get(price_plan_id);
			}
			dateList.add(record);
			RatePlanid_Dates.put(price_plan_id,dateList);
			/***** RatePlanid_Dates end *****/

		}

		JSONArray RoomInfos=new JSONArray();

		Iterator<Map.Entry<String,List>> iterable=RoomType_RatePlanids.entrySet().iterator();
		while(iterable.hasNext()){
			Map.Entry<String,List>entry=iterable.next();
			System.out.println(entry.getKey()+"->"+entry.getValue());

			String key=entry.getKey();
			List valueRatePlanids=entry.getValue();

			JSONObject RoomInfo=new JSONObject();
			JSONArray RatePlans=new JSONArray();

			RoomInfo.put("RoomID",key);
			int valueRatePlanidsLen=valueRatePlanids.size();
			for (int i = 0; i < valueRatePlanidsLen; i++) {
				JSONObject RatePlan=new JSONObject();
//						System.out.println(valueRatePlanids.get(i));
				String valueRatePlanid=(String) valueRatePlanids.get(i);
				RatePlan.put("RatePlanID",valueRatePlanid);

				JSONObject PriceInfo = new JSONObject();
				JSONArray DailyPrices = new JSONArray();

				List RatePlanid_DatesLists=RatePlanid_Dates.get(valueRatePlanid);
				int RatePlanidDatesListsLen=RatePlanid_DatesLists.size();

				int Price_Total_AmountAfterTaxFee=0;
				Record valueRecode = null; //每次循环
				int FirstDAYPrice = 0;
				for (int ri = 0; ri < RatePlanidDatesListsLen; ri++) {
					  valueRecode=(Record) RatePlanid_DatesLists.get(ri);


					JSONObject DailyPrice = new JSONObject();
					String dates=valueRecode.getStr("date");
					DailyPrice.put("Date",dates);
					int breakfast=StringUtils.nullToEmpty(valueRecode.getInt("breakfast"));
					DailyPrice.put("Breakfast",breakfast);



					//价格
					JSONObject Price=new JSONObject();
					double Cost = valueRecode.getDouble("price");//
					int employ_role_id =valueRecode.getInt("employ_role_id");
					if(employ_role_id==4){
						Cost=CommonUtils.FixCostPrice(Cost); //底价处理成卖价
					}

					Cost= CommonUtils.decimal2(Cost) * 100; //转换成分
					int Price_AmountAfterTaxFee=(int)Cost;
					Price.put("AmountAfterTaxFee", Price_AmountAfterTaxFee);
					DailyPrice.put("Price",Price);

					Price_Total_AmountAfterTaxFee=Price_Total_AmountAfterTaxFee+Price_AmountAfterTaxFee;

					DailyPrices.add(DailyPrice);

					if(dates.equals(StartDate)){
						FirstDAYPrice=Price_AmountAfterTaxFee;
					}


				}

					if(valueRecode!=null){


						//目前全作预付的
						String PayWay=valueRecode.getStr("payway");
						RatePlan.put("PayType","PP"); //默认预付
						if(PayWay.equals("1")){
							RatePlan.put("PayType","FG");
						}

						int roomstock= StringUtils.nullToEmpty(valueRecode.getInt("roomstock"));
						RatePlan.put("Stock",roomstock);

						String planname=  StringUtils.nullToEmpty(valueRecode.getStr("planname")) ;
						RatePlan.put("RatePlanName",planname);

						RatePlan.put("IsInstantConfirm",false);


						int is_cancel= StringUtils.nullToEmpty(valueRecode.getInt("is_cancel"));
						//1:可以取消；2：不可取消
						if(is_cancel==0){
							is_cancel=2;
						}

						int LadderType=1;
						if(is_cancel==2){


							Map<String, Object> LadderDeductPolicyEntity_map=new HashMap<String, Object>();

							LadderDeductPolicyEntity_map.put("Start", this.timeStringToLong("2023-01-01"));
							LadderDeductPolicyEntity_map.put("End", this.timeStringToLong(EndDate));
							LadderDeductPolicyEntity_map.put("Price", Price_Total_AmountAfterTaxFee);

							List<Map<String, Object>> LadderDeductPolicyEntity_list=new ArrayList<Map<String,Object>>();
							LadderDeductPolicyEntity_list.add(LadderDeductPolicyEntity_map);
							RatePlan.put("LadderDeductPolicyList", LadderDeductPolicyEntity_list);
						}else{
							LadderType=3;
							List<Map<String, Object>> LadderDeductPolicyEntity_list=new ArrayList<Map<String,Object>>();

							int cut_type= StringUtils.nullToEmpty(valueRecode.getInt("cut_type"));
							if(cut_type==0){
								int cutfromtime= 2* 24 * 60 * 60 * 1000; //从开始时间再加一天，到这天
								Long fromtime= this.timeStringToLongCut(StartDate,cutfromtime);


								Map<String, Object> LadderDeductPolicyEntity_map=new HashMap<String, Object>(); //不扣费
								LadderDeductPolicyEntity_map.put("Start", fromtime);
								LadderDeductPolicyEntity_map.put("End", this.timeStringToLong(EndDate));
								LadderDeductPolicyEntity_map.put("Price",0);
								LadderDeductPolicyEntity_list.add(LadderDeductPolicyEntity_map);
							}else{
								int advance_day= StringUtils.nullToEmpty(valueRecode.getInt("advance_day"));
								String specific_date= StringUtils.nullToEmpty(valueRecode.getStr("specific_date"));
								int hour = 0;
								int minute = 0;
								int second = 0;

								if(!Strings.isNullOrEmpty(specific_date)){
									try{
										String[] dsplit	= specific_date.split(":");
										hour=Integer.parseInt(dsplit[0]);
										minute=Integer.parseInt(dsplit[1]);

										String[] dsplit2	= specific_date.split(".");
										second=Integer.parseInt(dsplit2[1]);
									}catch (Exception e){

									}


								}

								int cuts= advance_day   * 24 * 60 * 60 * 1000;
								cuts=cuts+ hour* 60 * 60 * 1000;
								cuts=cuts+  minute* 60 * 1000;
								cuts=cuts+  second  * 1000;




								Long starttime= this.timeStringToLong(StartDate);
								Long midtime= starttime-cuts;
								Long fromtime= midtime -  1* 24 * 60 * 60 * 1000 ; //多减一天



								Map<String, Object> LadderDeductPolicyEntity_map=new HashMap<String, Object>(); //不扣费
								LadderDeductPolicyEntity_map.put("Start", fromtime);
								LadderDeductPolicyEntity_map.put("End", midtime);
								LadderDeductPolicyEntity_map.put("Price",0);
								LadderDeductPolicyEntity_list.add(LadderDeductPolicyEntity_map);


								Map<String, Object> LadderDeductPolicyEntity_map_pay=new HashMap<String, Object>();
								LadderDeductPolicyEntity_map_pay.put("Start", midtime);
								LadderDeductPolicyEntity_map_pay.put("End", this.timeStringToLong(EndDate));


								int cutPrice = Price_Total_AmountAfterTaxFee;//默认扣全部费用
								double planamount= StringUtils.nullToEmpty(valueRecode.getDouble("planamount"));
								if(cut_type==1){

									if(planamount>0){

										double Cost= CommonUtils.decimal2(planamount) * 100; //转换成分
										int Price_AmountAfterTaxFee=(int)Cost;
										cutPrice=Price_AmountAfterTaxFee;
									}
								}
								if(cut_type==2){


									if(planamount>0){

										double Cost= Price_Total_AmountAfterTaxFee * CommonUtils.decimal2(planamount) * 100; //转换成分
										int Price_AmountAfterTaxFee=(int)Cost;
										cutPrice=Price_AmountAfterTaxFee;
									}
								}

								if(cut_type==3){


									cutPrice=FirstDAYPrice;
								}
								if(cutPrice<1){ //
									cutPrice=Price_Total_AmountAfterTaxFee;
								}



								LadderDeductPolicyEntity_map_pay.put("Price", cutPrice);
								LadderDeductPolicyEntity_list.add(LadderDeductPolicyEntity_map_pay);


							}


							RatePlan.put("LadderDeductPolicyList", LadderDeductPolicyEntity_list);


						}
						RatePlan.put("LadderType", LadderType);


					}


				JSONObject Total=new JSONObject();
				Total.put("AmountAfterTaxFee", Price_Total_AmountAfterTaxFee);

				PriceInfo.put("DailyPrices",DailyPrices);
				PriceInfo.put("Total",Total);



				RatePlan.put("PriceInfo",PriceInfo);

				RatePlans.add(RatePlan);
			}

			RoomInfo.put("RatePlans",RatePlans);

			RoomInfos.add(RoomInfo);

		}

		room_info_json.put("RoomInfos", RoomInfos);


		ResponseUtils res = new ResponseUtils();
		res.setCode("10000");
		res.setMsg("success");
		res.setData(room_info_json); //要放data

		ResponseUtilsOutResponse resOut=new ResponseUtilsOutResponse();
		resOut.setResponse(res);

//		 CacheKit.put("getPriceDB", cacheKey,resOut);
			String txt=JSON.toJSONString(resOut );
			LedisdbCache.setCache(  cacheKey,txt,30);


		renderJson(resOut);

		}catch (Exception e){

			ResponseUtils res = new ResponseUtils();
			res.setCode("10000");
			res.setMsg("success");

			room_info_json.put("RoomInfos",new ArrayList<>());
			res.setData(room_info_json); //要放data

			ResponseUtilsOutResponse resOut=new ResponseUtilsOutResponse();
			resOut.setResponse(res);




			renderJson(resOut);


		}
	}

	//用的是 encache 在本地可以，但在服务器上，性能还是达不到 300qps rq99 600
	public void getPriceDBPress2() throws Exception {

		JSONObject room_info_json=new JSONObject();
		try {


		String dataMap = getRequest().getParameter("biz_content");
		JSONObject req_json = JSONObject.parseObject(dataMap);
		String HotelID=req_json.getString("HotelID");

        room_info_json.put("HotelID", HotelID);

		Hotel hotel=	hsrv.getHotelInfo(Long.parseLong(HotelID));
			if(true){
				renderJson("cacheResultd");
				return;
			}
		String StartDate=req_json.getString("StartDate");
		String EndDate=req_json.getString("EndDate");
		String RatePlanID=req_json.getString("RatePlanID");
		String RoomID=req_json.getString("RoomID");

		if(RoomID==null){
			RoomID="";
		}
		if(RatePlanID==null){
			RatePlanID="";
		}



		Integer elongidInt= hotel.getElongId();

		PriceDbService pdbs=new PriceDbService();
		String elongid=Integer.toString(elongidInt);


		String cacheKey=  String.format("getPriceDBPress2|%s|%s|%s|%s|%s", elongid,StartDate,EndDate,RatePlanID,RoomID);


//		if(true){
//			renderJson("cacheResult");
//			return;
//		}

		ResponseUtilsOutResponse	cacheResult = CacheKit.get("getPriceDB", cacheKey);

		if (cacheResult != null) {
			renderJson(cacheResult);
			return;
		}


		JSONArray gaode_RoomInfos = new JSONArray();//返回给高德的房型数组

		List<Map<String, Object>> RatePlans_list=new ArrayList<Map<String,Object>>(); //

		List<Record> records= pdbs.getPriceList(  elongid,  StartDate,    EndDate,  RatePlanID,  RoomID);

		Map<String, Object> RatePlans_Map=new HashMap<>(); //
		Map<String, Object> RoomInfos_Map=new HashMap<>(); //

		Map<String, List> RoomType_RatePlanids=new HashMap<>(); // roomid key, rateplanids 逗号list
		Map<String, List> RatePlanid_Dates=new HashMap<>(); //

		Map<String,String> one=new HashMap<>();// 这个是为了 不让 room 下边的 rateplan 重复添加

		//price_plan_id 用这个 id 当作map key存起来

		for (Record record : records) {
			String price_plan_id=record.getStr("price_plan_id");
			String room_type_id=record.getStr("room_type_id");
			String date=record.getStr("date");
			String payway=record.getStr("payway"); //支付方式 1现付 2预付
			int status= StringUtils.nullToEmpty(record.getInt("status"));
			int planstatus= StringUtils.nullToEmpty(record.getInt("planstatus"));

			if(Strings.isNullOrEmpty(room_type_id)){
				continue;
			}

			if(Strings.isNullOrEmpty(price_plan_id)){
				continue;
			}

			if(payway==null || !payway.equals("2")){ //过滤掉 只要预付
				continue;
			}

			if(status==0){ //过滤掉 0
				continue;
			}
			if(planstatus==0){ //过滤掉 0
				continue;
			}


			/***** 存 RoomType_RatePlanids *****/
			List RatePlanids;

			if(!one.containsKey(room_type_id+price_plan_id)){
				if(!RoomType_RatePlanids.containsKey(room_type_id)){
					RatePlanids=new ArrayList();
				}else{
					RatePlanids=RoomType_RatePlanids.get(room_type_id);
				}

				RatePlanids.add(price_plan_id);
				RoomType_RatePlanids.put(room_type_id,RatePlanids);

				one.put(room_type_id+price_plan_id,"1");
			}


			/***** RoomType_RatePlanids 结束*****/


			/*****   RatePlanid_Dates begin *****/
			List dateList;
			if(!RatePlanid_Dates.containsKey(price_plan_id)){
				dateList=new ArrayList();
			}else{
				dateList=RatePlanid_Dates.get(price_plan_id);
			}
			dateList.add(record);
			RatePlanid_Dates.put(price_plan_id,dateList);
			/***** RatePlanid_Dates end *****/

		}

		JSONArray RoomInfos=new JSONArray();

		Iterator<Map.Entry<String,List>> iterable=RoomType_RatePlanids.entrySet().iterator();
		while(iterable.hasNext()){
			Map.Entry<String,List>entry=iterable.next();
			System.out.println(entry.getKey()+"->"+entry.getValue());

			String key=entry.getKey();
			List valueRatePlanids=entry.getValue();

			JSONObject RoomInfo=new JSONObject();
			JSONArray RatePlans=new JSONArray();

			RoomInfo.put("RoomID",key);
			int valueRatePlanidsLen=valueRatePlanids.size();
			for (int i = 0; i < valueRatePlanidsLen; i++) {
				JSONObject RatePlan=new JSONObject();
//						System.out.println(valueRatePlanids.get(i));
				String valueRatePlanid=(String) valueRatePlanids.get(i);
				RatePlan.put("RatePlanID",valueRatePlanid);

				JSONObject PriceInfo = new JSONObject();
				JSONArray DailyPrices = new JSONArray();

				List RatePlanid_DatesLists=RatePlanid_Dates.get(valueRatePlanid);
				int RatePlanidDatesListsLen=RatePlanid_DatesLists.size();

				int Price_Total_AmountAfterTaxFee=0;
				Record valueRecode = null; //每次循环
				int FirstDAYPrice = 0;
				for (int ri = 0; ri < RatePlanidDatesListsLen; ri++) {
					  valueRecode=(Record) RatePlanid_DatesLists.get(ri);


					JSONObject DailyPrice = new JSONObject();
					String dates=valueRecode.getStr("date");
					DailyPrice.put("Date",dates);
					int breakfast=StringUtils.nullToEmpty(valueRecode.getInt("breakfast"));
					DailyPrice.put("Breakfast",breakfast);



					//价格
					JSONObject Price=new JSONObject();
					double Cost = valueRecode.getDouble("price");//
					int employ_role_id =valueRecode.getInt("employ_role_id");
					if(employ_role_id==4){
						Cost=CommonUtils.FixCostPrice(Cost); //底价处理成卖价
					}


					Cost= CommonUtils.decimal2(Cost) * 100; //转换成分
					int Price_AmountAfterTaxFee=(int)Cost;
					Price.put("AmountAfterTaxFee", Price_AmountAfterTaxFee);
					DailyPrice.put("Price",Price);

					Price_Total_AmountAfterTaxFee=Price_Total_AmountAfterTaxFee+Price_AmountAfterTaxFee;

					DailyPrices.add(DailyPrice);

					if(dates.equals(StartDate)){
						FirstDAYPrice=Price_AmountAfterTaxFee;
					}

//					System.out.println(valueRecode);
				}

					if(valueRecode!=null){ //把  rateplan的其它字段


						//目前全作预付的
						String PayWay=valueRecode.getStr("payway");
						RatePlan.put("PayType","PP"); //默认预付
						if(PayWay.equals("1")){
							RatePlan.put("PayType","FG");
						}

						int roomstock= StringUtils.nullToEmpty(valueRecode.getInt("roomstock"));
						RatePlan.put("Stock",roomstock);

						String planname=  StringUtils.nullToEmpty(valueRecode.getStr("planname")) ;
						RatePlan.put("RatePlanName",planname);

						RatePlan.put("IsInstantConfirm",false);


						int is_cancel= StringUtils.nullToEmpty(valueRecode.getInt("is_cancel"));
						//1:可以取消；2：不可取消
						if(is_cancel==0){
							is_cancel=2;
						}

						int LadderType=1; //1=不可取消。2=入住前任意时间段免费取消。3=阶梯取消扣款政策,和LadderDeductPolicyEntity共存。
						if(is_cancel==2){  	//1:可以取消；2：不可取消


							Map<String, Object> LadderDeductPolicyEntity_map=new HashMap<String, Object>();

							LadderDeductPolicyEntity_map.put("Start", this.timeStringToLong("2023-01-01"));
							LadderDeductPolicyEntity_map.put("End", this.timeStringToLong(EndDate));
							LadderDeductPolicyEntity_map.put("Price", Price_Total_AmountAfterTaxFee);

							List<Map<String, Object>> LadderDeductPolicyEntity_list=new ArrayList<Map<String,Object>>();
							LadderDeductPolicyEntity_list.add(LadderDeductPolicyEntity_map);
							RatePlan.put("LadderDeductPolicyList", LadderDeductPolicyEntity_list);
						}else{ // 是阶梯 可取消
							LadderType=3;  // 取消的话，是阶梯 不可取消
							List<Map<String, Object>> LadderDeductPolicyEntity_list=new ArrayList<Map<String,Object>>();
							//这个是要扣费的 0:不扣费；1:金额；2：比例；3：首晚房费; 4：全部房费
							int cut_type= StringUtils.nullToEmpty(valueRecode.getInt("cut_type"));
							if(cut_type==0){
								int cutfromtime= 2* 24 * 60 * 60 * 1000; //从开始时间再加一天，到这天
								Long fromtime= this.timeStringToLongCut(StartDate,cutfromtime);


								Map<String, Object> LadderDeductPolicyEntity_map=new HashMap<String, Object>(); //不扣费
								LadderDeductPolicyEntity_map.put("Start", fromtime);
								LadderDeductPolicyEntity_map.put("End", this.timeStringToLong(EndDate));
								LadderDeductPolicyEntity_map.put("Price",0);
								LadderDeductPolicyEntity_list.add(LadderDeductPolicyEntity_map);
							}else{
								int advance_day= StringUtils.nullToEmpty(valueRecode.getInt("advance_day"));
								String specific_date= StringUtils.nullToEmpty(valueRecode.getStr("specific_date"));
								int hour = 0;
								int minute = 0;
								int second = 0;

								if(!Strings.isNullOrEmpty(specific_date)){
									try{
										String[] dsplit	= specific_date.split(":");
										hour=Integer.parseInt(dsplit[0]);
										minute=Integer.parseInt(dsplit[1]);

										String[] dsplit2	= specific_date.split(".");
										second=Integer.parseInt(dsplit2[1]);
									}catch (Exception e){

									}


								}

								int cuts= advance_day   * 24 * 60 * 60 * 1000;
								cuts=cuts+ hour* 60 * 60 * 1000;
								cuts=cuts+  minute* 60 * 1000;
								cuts=cuts+  second  * 1000;

								//计算减多少时间

//								int cutfromtime=cuts+ 1* 24 * 60 * 60 * 1000; //从开始时间再加一天，到这天


								Long starttime= this.timeStringToLong(StartDate);
								Long midtime= starttime-cuts;
								Long fromtime= midtime -  1* 24 * 60 * 60 * 1000 ; //多减一天



								Map<String, Object> LadderDeductPolicyEntity_map=new HashMap<String, Object>(); //不扣费
								LadderDeductPolicyEntity_map.put("Start", fromtime);
								LadderDeductPolicyEntity_map.put("End", midtime);
								LadderDeductPolicyEntity_map.put("Price",0);
								LadderDeductPolicyEntity_list.add(LadderDeductPolicyEntity_map);


								Map<String, Object> LadderDeductPolicyEntity_map_pay=new HashMap<String, Object>();
								LadderDeductPolicyEntity_map_pay.put("Start", midtime);
								LadderDeductPolicyEntity_map_pay.put("End", this.timeStringToLong(EndDate));


								int cutPrice = Price_Total_AmountAfterTaxFee;//默认扣全部费用
								double planamount= StringUtils.nullToEmpty(valueRecode.getDouble("planamount"));
								if(cut_type==1){ //0:不扣费；1:金额；2：比例；3：首晚房费; 4：全部房费


									if(planamount>0){

										double Cost= CommonUtils.decimal2(planamount) * 100; //转换成分
										int Price_AmountAfterTaxFee=(int)Cost;
										cutPrice=Price_AmountAfterTaxFee;
									}
								}
								if(cut_type==2){ //0:不扣费；1:金额；2：比例；3：首晚房费; 4：全部房费


									if(planamount>0){

										double Cost= Price_Total_AmountAfterTaxFee * CommonUtils.decimal2(planamount) * 100; //转换成分
										int Price_AmountAfterTaxFee=(int)Cost;
										cutPrice=Price_AmountAfterTaxFee;
									}
								}

								if(cut_type==3){ //0:不扣费；1:金额；2：比例；3：首晚房费; 4：全部房费


									cutPrice=FirstDAYPrice;
								}
								if(cutPrice<1){ //
									cutPrice=Price_Total_AmountAfterTaxFee;
								}



								LadderDeductPolicyEntity_map_pay.put("Price", cutPrice);
								LadderDeductPolicyEntity_list.add(LadderDeductPolicyEntity_map_pay);


							}


							RatePlan.put("LadderDeductPolicyList", LadderDeductPolicyEntity_list);


						}
						RatePlan.put("LadderType", LadderType);


					}


				JSONObject Total=new JSONObject();
				Total.put("AmountAfterTaxFee", Price_Total_AmountAfterTaxFee);

				PriceInfo.put("DailyPrices",DailyPrices);
				PriceInfo.put("Total",Total);



				RatePlan.put("PriceInfo",PriceInfo);

				RatePlans.add(RatePlan);
			}

			RoomInfo.put("RatePlans",RatePlans);

			RoomInfos.add(RoomInfo);

		}

		room_info_json.put("RoomInfos", RoomInfos);


		ResponseUtils res = new ResponseUtils();
		res.setCode("10000");
		res.setMsg("success");
		res.setData(room_info_json); //要放data

		ResponseUtilsOutResponse resOut=new ResponseUtilsOutResponse();
		resOut.setResponse(res);

		 CacheKit.put("getPriceDB", cacheKey,resOut);

		renderJson(resOut);

		}catch (Exception e){

			ResponseUtils res = new ResponseUtils();
			res.setCode("10000");
			res.setMsg("success");

			room_info_json.put("RoomInfos",new ArrayList<>());
			res.setData(room_info_json); //要放data

			ResponseUtilsOutResponse resOut=new ResponseUtilsOutResponse();
			resOut.setResponse(res);




			renderJson(resOut);


		}
	}


    public void getPriceDBPress() throws Exception {
        JSONObject room_info_json=new JSONObject();
        try {


            String dataMap = getRequest().getParameter("biz_content");
            JSONObject req_json = JSONObject.parseObject(dataMap);
            String HotelID=req_json.getString("HotelID");

            room_info_json.put("HotelID", HotelID);

//            Hotel hotel=	hsrv.getHotelInfo(Long.parseLong(HotelID));
            String StartDate=req_json.getString("StartDate");
            String EndDate=req_json.getString("EndDate");
            String RatePlanID=req_json.getString("RatePlanID");
            String RoomID=req_json.getString("RoomID");

            if(RoomID==null){
                RoomID="";
            }
            if(RatePlanID==null){
                RatePlanID="";
            }



            Integer elongidInt=93614049;

            PriceDbService pdbs=new PriceDbService();
            String elongid=Integer.toString(elongidInt);



            JSONArray gaode_RoomInfos = new JSONArray();//返回给高德的房型数组

            List<Map<String, Object>> RatePlans_list=new ArrayList<Map<String,Object>>(); //

//            List<Record> records= pdbs.getPriceList(  elongid,  StartDate,    EndDate,  RatePlanID,  RoomID);
            List<Record> records= pdbs.getPriceListPress(  elongid,  StartDate,    EndDate,  RatePlanID,  RoomID);

            ResponseUtils res = new ResponseUtils();
            res.setCode("10000");
            res.setMsg("success");
            res.setData(room_info_json); //要放data

            ResponseUtilsOutResponse resOut=new ResponseUtilsOutResponse();
            resOut.setResponse(res);

            renderJson(resOut);

        }catch (Exception e){

            ResponseUtils res = new ResponseUtils();
            res.setCode("10000");
            res.setMsg("success");

            room_info_json.put("RoomInfos",new ArrayList<>());
            res.setData(room_info_json); //要放data

            ResponseUtilsOutResponse resOut=new ResponseUtilsOutResponse();
            resOut.setResponse(res);

            renderJson(resOut);


        }
    }

	public int getint(Integer k){
		if(k==null){
			return 0;
		}
		return k;
	}

	//打开压测条件
	public void openPressureTest()   {
		PriceCache.setPressureTest(true);
		ResponseUtils res = new ResponseUtils();
		res.setCode("10000");
		res.setMsg("success");
		renderJson(res);
	}
	//关压测条件
	public void closePressureTest()   {
		PriceCache.setPressureTest(false);
		ResponseUtils res = new ResponseUtils();
		res.setCode("10000");
		res.setMsg("success");
		renderJson(res);
	}
	//  压测条件
	public void getPressureTest()   {
		;
		ResponseUtils res = new ResponseUtils();
		res.setCode("10000");
		res.setMsg(String.valueOf(PriceCache.getPressureTest()));
		renderJson(res);
	}

	//打开 价格cache
	public void openCacheStatus()   {
		PriceCache.setCacheStatus(true);
		ResponseUtils res = new ResponseUtils();
		res.setCode("10000");
		res.setMsg("success");
		renderJson(res);
	}
	//关压 价格cache
	public void closeCacheStatus()   {
		PriceCache.setCacheStatus(false);
		ResponseUtils res = new ResponseUtils();
		res.setCode("10000");
		res.setMsg("success");
		renderJson(res);
	}
	//  价格cache
	public void getCacheStatus()   {
		;
		ResponseUtils res = new ResponseUtils();
		res.setCode("10000");
		res.setMsg(String.valueOf(PriceCache.getCacheStatus()));
		renderJson(res);
	}

	public void getPricetest() throws Exception {


		// 初步判断  高德官网的 BedDetail 验证有问题
		String urlttt="http://api.tetuijiudian.com/shuia/test/test5.php";
		String sttt=	HttpKit.get(urlttt);

		JSONObject jso=JSON.parseObject(sttt);
		renderJson(jso);
		if(true){
			return;
		}


	}
	public static void main2(String[] args) {
		String url = "http://gd.tetuijiudian.cn:859/apivamap/rest?method=hotel.detail&data={\"Version\":\"1.35\",\"Local\":\"zh_CN\",\"Request\":{\"ArrivalDate\":\"2023-08-21\",\"DepartureDate\":\"2023-08-24\",\"HotelIds\":\"42501007\",\"Options\":\"2,12,14\",\"PaymentType\":\"All\"}}";
		String result = HttpKit.get(url);
		System.out.println(result);
	}

	public  long timeStringToLong(String timeString) throws Exception {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date = dateFormat.parse(timeString);
		long timestamp = date.getTime();
		long result = timestamp;
		return result;
	}

	public  long timeStringToLongCut(String timeString,int cut) throws Exception {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date = dateFormat.parse(timeString);
		long timestamp = date.getTime();
		long result = timestamp - cut;
		return result;
	}

}
