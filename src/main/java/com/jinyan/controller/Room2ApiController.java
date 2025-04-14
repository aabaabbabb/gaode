package com.jinyan.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;
import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.aop.Inject;
import com.jfinal.core.Path;
import com.jfinal.kit.HttpKit;
import com.jfinal.kit.JsonKit;
import com.jinyan.common.Constant;
import com.jinyan.common.GdlogInterceptor;
import com.jinyan.controller.base.BaseController;
import com.jinyan.model.EbookingRoomType;
import com.jinyan.model.Hotel;
import com.jinyan.model.Pic;
import com.jinyan.service.HotelService;
import com.jinyan.service.HotelroomChgService;
import com.jinyan.service.PriceCache;
import com.jinyan.service.RoomTypeDbService;
import com.jinyan.utils.*;
//import redis.clients.jedis.Jedis;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

@Clear
@Before(GdlogInterceptor.class)
@Path(value = "/hotel", viewPath = "")
public class Room2ApiController extends BaseController {

	@Inject
	private HotelService hsrv;

	@Inject
	private PriceCache priceCache;

	@Inject
	private HotelroomChgService hotelroomChgStuff;
	@Inject
	private RoomTypeDbService serverRoomTypeDbService;

//	static Jedis jedis = RedisUtil.getConn();

	/**
	 * 商家推送酒店房型信息
	 * https://y.amap.com/docs/hotel/pushroominfo
	 */
//	@ActionKey("/hotel/mall/pushRoomInfo")
	public void pushRoomInfo() {
		boolean userdb=Constant.USEDB;
		if(userdb){
			pushRoomInfoDB();
			return;
		}
//		Long HotelID= getParaToLong("HotelID",  0L);
//
//		if(HotelID==0){
//			ResponseUtils res=new ResponseUtils();
//			res.setCode("40001");
//			res.setMsg("参数不对");
//			renderJson(res);
//			return;
//		}
//
//		Hotel mHotel=	hsrv.getHotelInfo(HotelID);
//
//		Map<String, String> dataMap = new HashMap<String, String>();
//		dataMap.put("method", "amap.hotel.offline.pushRoomInfo");
//		dataMap.put("utc_timestamp", String.valueOf(System.currentTimeMillis()));
//		dataMap.put("app_id", Constant.APP_ID);
//		dataMap.put("version", "1.0");
//		dataMap.put("sign_type", "RSA2");
//		dataMap.put("charset", "UTF-8");
//
//		JSONObject bizContent = new JSONObject();
//		bizContent.put("RequestID", UUID.randomUUID().toString());
//
//		JSONArray bizContentDataArray = new JSONArray();
//
//		JSONObject bizContentData = new JSONObject();
//		bizContentData.put("HotelID", HotelID);
//
//		JSONArray RoomDetailList = new JSONArray();
//
//		String value = jedis.get("el_hotel_static_info_" + mHotel.getElongId());
//		JSONObject json_obj = JSONObject.parseObject(value).getJSONObject("Result");
//		JSONArray json_arr = json_obj.getJSONArray("Rooms");
//		for (int i = 0; i < json_arr.size(); i++) {
//			JSONObject room_json = json_arr.getJSONObject(i);
//			Map<String, Object> roomMap = new HashMap<String, Object>();
//			roomMap.put("RoomID", room_json.getString("RoomID"));
//			String RoomName=room_json.getString("RoomName");
//			roomMap.put("RoomName", room_json.getString("RoomName"));
//			roomMap.put("Status", "1");
//			//房型类型
//			roomMap.put("RoomTypeStd", 1); //1-标准间
//			Integer BedQuantityTotal=1;
//			if(RoomName.contains("双床")){
//				roomMap.put("RoomTypeStd", 2);
//				BedQuantityTotal=2;
//			}
//			if(RoomName.contains("大床")){
//				roomMap.put("RoomTypeStd", 6);
//			}
//			roomMap.put("AddBed", "0"); //0-不能加床 1-可以加床
//			roomMap.put("AreaRange", room_json.get("Area"));
// 			roomMap.put("RoomQuantity", room_json.getInteger("Amount"));
//			roomMap.put("RoomPerson", room_json.getInteger("Capacity"));
//			roomMap.put("FloorRange", room_json.getString("Floor"));
//			roomMap.put("BedQuantityTotal", BedQuantityTotal);
//
//			Integer BroadnetAccess=room_json.getInteger("BroadnetAccess");
//			Integer WirelessBroadnet=0;
//			Integer WiredBroadnet=0;
//			if(BroadnetAccess==1)//0表示无宽带，1 表示有宽带, 2 表示有WIFI   国际酒店不存在、国内酒店存在
//			{
//				WirelessBroadnet=2;
//			}
//			if(BroadnetAccess==2)//0表示无宽带，1 表示有宽带, 2 表示有WIFI   国际酒店不存在、国内酒店存在
//			{
//				WiredBroadnet=2;
//			}
//			roomMap.put("WirelessBroadnet", WirelessBroadnet);
//
//			roomMap.put("WiredBroadnet", WiredBroadnet);
//
//			roomMap.put("Bath", "未知");
//
//
//			RoomDetailList.add(roomMap);
//		}
//
//		bizContentData.put("RoomDetail", RoomDetailList);
//
//
//		JSONArray bizContentDataList=new JSONArray();
//		bizContentDataList.add(bizContentData);
//
//		bizContent.put("Data",bizContentDataList);
//
//		dataMap.put("biz_content", JSON.toJSONString(bizContent));
//		try {
//			dataMap.put("sign", GenerateSignUtils.generateSign(dataMap));
//		} catch (Exception e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//
//
//		Map<String, String> headers = new HashMap<String, String>(16);
//		headers.put("Content-Type", "application/x-www-form-urlencoded");
//
//		String  prestr= StringUtils.asUrlParams(dataMap);
//
//		String ret = HttpKit.post(Constant.DEV_URL, null,prestr,
//				headers);
//		ResponseUtils res = new ResponseUtils();
//		res.setCode("10000");
//		res.setMsg("success");
////		res.setData(JsonKit.toJson(roomList));
//
// 		renderJson(res);
//		System.out.println(ret);
	}

	public void pushRoomInfoDB() {
		Long HotelID= getParaToLong("HotelID",  0L);
		String elongIdStr=get("elongId");
		if(HotelID==0&&elongIdStr.equals("")){
			ResponseUtils res=new ResponseUtils();
			res.setCode("40001");
			res.setMsg("参数不对");
			renderJson(res);
			return;
		}

		Hotel mHotel=	hsrv.getHotelInfoNo(HotelID,elongIdStr);
		if(mHotel==null) {
			mHotel=hsrv.addHotel(elongIdStr);
		}
		

		Map<String, String> dataMap = new HashMap<String, String>();
		dataMap.put("method", "amap.hotel.offline.pushRoomInfo");
		dataMap.put("utc_timestamp", String.valueOf(System.currentTimeMillis()));
		dataMap.put("app_id", Constant.APP_ID);
		dataMap.put("version", "1.0");
		dataMap.put("sign_type", "RSA2");
		dataMap.put("charset", "UTF-8");

		JSONObject bizContent = new JSONObject();
		bizContent.put("RequestID", UUID.randomUUID().toString());


		JSONObject bizContentData = new JSONObject();
		bizContentData.put("HotelID", mHotel.getId());

		JSONArray RoomDetailList = new JSONArray();

		String ElongId = mHotel.getElongIdStr();

//		String value = jedis.get("el_hotel_static_info_" + mHotel.getElongId());

		List<EbookingRoomType>  roomListdb= serverRoomTypeDbService.getEbookingRoomTypeList(ElongId);
		for (EbookingRoomType room : roomListdb) {
			Map<String, Object> roomMap = new HashMap<String, Object>();
			roomMap.put("RoomID", Integer.toString(room.getId())) ;
			roomMap.put("RoomName", room.getName());
			roomMap.put("Status", "1");
			String bed=room.getBed();
			String RoomTypeStd= serverRoomTypeDbService.BedConvertGaode(bed) ;
			roomMap.put("RoomTypeStd", RoomTypeStd); //1-标准间
			roomMap.put("AddBed", "0"); //0-不能加床 1-可以加床

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

			roomMap.put("RoomQuantity", room.getTotalCount());
			roomMap.put("AreaRange", serverRoomTypeDbService.AreaConvertGaode(room.getArea()));
			roomMap.put("RoomPerson",room.getPerson());
			roomMap.put("FloorRange", StringUtils.nullToEmpty(room.getFloor()) );
			roomMap.put("BedQuantityTotal", serverRoomTypeDbService.BedQuantityTotal(bed));
			String Broadband = room.getBroadband();
			roomMap.put("WirelessBroadnet", serverRoomTypeDbService.WirelessBroadnet(Broadband));
			roomMap.put("WiredBroadnet", serverRoomTypeDbService.WirelessBroadnet(Broadband));
			roomMap.put("Bath", "未知");

			if(room.getIsElong()==0){ //如果是自营的话
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


			RoomDetailList.add(roomMap);

		}


		bizContentData.put("RoomDetail", RoomDetailList);


		JSONArray bizContentDataList=new JSONArray();
		bizContentDataList.add(bizContentData);

		bizContent.put("Data",bizContentDataList);

		dataMap.put("biz_content", JSON.toJSONString(bizContent));
		try {
			dataMap.put("sign", GenerateSignUtils.generateSign(dataMap));
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}


		Map<String, String> headers = new HashMap<String, String>(16);
		headers.put("Content-Type", "application/x-www-form-urlencoded");

		String  prestr= StringUtils.asUrlParams(dataMap);

		String ret = HttpKit.post(Constant.DEV_URL, null,prestr,
				headers);
		ResponseUtils res = new ResponseUtils();
		res.setCode("10000");
		res.setMsg("success");
		Map<Object, Object> dataMap2=new HashMap<Object, Object>();
		dataMap2.put("gdId", mHotel.getId());
		res.setDataJson(JSONObject.parseObject(JsonKit.toJson(dataMap2)));
		//res.setData(JSON.toJSONString(dataMap2));
//		res.setData(JsonKit.toJson(roomList));

		hotelroomChgStuff.HotelroomChanged(HotelID);

		renderJson(res);
//		System.out.println(ret);

	}

	public static void main33(String[] args) {
		String jsonString="{\"data\":\"{\"gdId\":843332305}\",\"msg\":\"success\",\"sub_code\":\"\",\"sub_msg\":\"\",\"code\":\"10000\"}";
		JSONObject jsonObject=JSONObject.parseObject(jsonString);
		System.out.println(jsonObject);
	}


	public static void main2(String[] args) {
		String url = "http://gd.tetuijiudian.cn:859/apivamap/rest?method=hotel.detail&data={\"Version\":\"1.35\",\"Local\":\"zh_CN\",\"Request\":{\"ArrivalDate\":\"2023-08-21\",\"DepartureDate\":\"2023-08-24\",\"HotelIds\":\"42501007\",\"Options\":\"2,12,14\",\"PaymentType\":\"All\"}}";
		String result = HttpKit.get(url);
		System.out.println(result);
	}



}
