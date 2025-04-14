package com.jinyan.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;
import com.jfinal.ext.kit.DateKit;
import com.jfinal.kit.JsonKit;
import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.druid.DruidPlugin;
import com.jinyan.kit.StringKit;
import com.jinyan.model.Hotel;
import com.jinyan.utils.CommonUtils;
import com.jinyan.utils.RedisUtil;
import redis.clients.jedis.Jedis;

import javax.swing.*;

import java.nio.channels.NonWritableChannelException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
处理一些 hotel 相关的业务， 废弃了，不用了
 */

public class HotelStuffService {

	public static final String Price = "End";
	private Hotel hotelDao = new Hotel().dao();

	private String redis_perfix_hotelid_list = "redis_perfix_hotelid_list_gd"; // 存在 ledisdb 中的 列表 键

	/*
	 * 这个是 艺龙酒店 艺龙文档
	 * https://open.elong.com/doc/info/cn-api-meta-hotel_incr_sharding_state
	 * 酒店和房型有变化时，这个接口会有增量数据， 服务器上有个程序会把 拉取这个酒店id,和高德 gd_hotel里边的 elongid 对应，找出高德id,
	 * 存在 这个键 “redis_perfix_hotelid_list_gd” 的 redis List 结构中。 供高德接口
	 * 
	 * 相关的接口有这些: 1、http://gd.tetuijiudian.cn/elongHandle/hotel/stopIncrShardingState
	 * 停止艺龙增量写入
	 * 
	 * 2
	 * http://gd.tetuijiudian.cn/elongHandle/hotel/incrShardingState?times=2023-07-
	 * 23 12:51:00 开始艺龙增量写入，如果times参数为空的话，就是以当前时间的lastid开始 一般不需要调用，后边的times 参考艺龙的文档
	 * https://open.elong.com/doc/info/cn-api-meta-hotel_incr_sharding_id
	 * 
	 * 3 http://gd.tetuijiudian.cn/elongHandle/hotel/getLenIncrShardingState
	 * 获取艺龙状态的增量 总的数量
	 * 
	 * 3 http://gd.tetuijiudian.cn/elongHandle/hotel/notifyChangeId 如果在系统其它的地方，“增加”
	 * 了数据库gd_hotel的酒店时，需调用这个 。意思是说，gd_hotel中表中的主键有变化时才调用。
	 * 
	 */
	public List<Long> getIncrStateIdList(String type, int currentPage, int pageSize) {

		if (currentPage < 1) {
			currentPage = 1;
		}
		if (pageSize < 20) {
			pageSize = 20;
		}
		Jedis jedis = RedisUtil.getConn();

		Long b = jedis.llen(redis_perfix_hotelid_list);
		if (currentPage < 1) { // 没有

		}

		int start = (currentPage - 1) * pageSize;
		int stop = start + pageSize - 1;

		List<String> hotelIds = jedis.lrange(redis_perfix_hotelid_list, start, stop);
		List<Long> hotels = new ArrayList<Long>();
		for (String h : hotelIds) {

			// {"ElongID":"92982868","GaoDeID":"994013432","ChangeTime":1692084733,"StartDate":"","EndDate":""}
//			Long num  = Long.parseLong(h);
			JSONObject jsonObject = JSONObject.parseObject(h);

			String GaoDeID = jsonObject.getString("GaoDeID");
			Long num = Long.parseLong(GaoDeID);
			if (num > 0) {
				hotels.add(num);
			}

		}

		return hotels;
	}

	/*
	 * 这个是清理掉 redis_perfix_hotelid_list 里的数据, 一般也不需要， 已经有一个接口
	 * http://gd.tetuijiudian.cn/elongHandle/hotel/clearIncrShardingState ，也是清除
	 * 状态增量的数据
	 */
	public void clearIncrStateIdList() {

		Jedis jedis = RedisUtil.getConn();

		Long b = jedis.llen(redis_perfix_hotelid_list);

		jedis.ltrim(redis_perfix_hotelid_list, 0, 0);

	}
	
	public static void main(String[] args) {
		DruidPlugin dp = new DruidPlugin(
				"jdbc:mysql://gd.tetuijiudian.cn:3306/gaode66?useSSL=false&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull",
				"gaode24", "Redbull2266_");
		ActiveRecordPlugin arp = new ActiveRecordPlugin(dp);

		// 与 jfinal web 环境唯一的不同是要手动调用一次相关插件的start()方法
		dp.start();
		arp.start();
		HotelStuffService hsfsrv = new HotelStuffService();
		hsfsrv.checkPrice("03001138", "938451", "2193740", 1, "2025-04-03", "2025-04-04");
	}

	public JSONObject checkPrice(String HotelID, String RoomID, String RatePlanID, int RoomNumber, String Arrival,
			String Departure) {
		String sql = "select price.order_rooms_sum,price.price_plan_id,price.room_type_id,price.date,price.price,price.status,price.elong_RoomId,price.employ_role_id,"
				+ "plan.name as planname,plan.status as planstatus,plan.advance_day,plan.specific_date,plan.is_cancel,plan.minDays,plan.maxDays,plan.minAdvHours,"
				+ "plan.maxAdvHours,plan.startTime,plan.endTime,plan.breakfast,plan.payway,plan.cut_type,plan.amount as planamount,room.stock as roomstock ,"
				+ "room.stock - price.order_rooms_sum as roomstockreal ,price.today_stock as todaystock ,price.today_stock - price.order_rooms_sum as todaystockreal ,"
				+ "hotel.noroom as noroom  from gd_ebooking_price as price  left join gd_ebooking_price_plan as plan on price.price_plan_id= plan.id  "
				+ "left join gd_hotel as hotel on price.hotel_id= hotel.ElongIdStr  left join gd_ebooking_room_type as room on price.room_type_id= room.id "
				+ "where price.hotel_id='"+HotelID+"'  and ( (hotel.noroom=0 and price.es_status=1) or hotel.noroom=1   or price.status=100)  "
				+ "and room.stock > price.order_rooms_sum and price.status>0 and price.price>0 and plan.status=1  and price.date>='"+Arrival+"' "
				+ "and price.date<'"+Departure+"' and price.price_plan_id='"+RatePlanID+"'";
		
		//String sql = "SELECT p.hotel_id,p.price_plan_id,p.room_type_id,p.price,p.date,p.`status`,(p.today_stock-p.order_rooms_sum) as amt,pp.cut_type,pp.status as ppstatus,pp.breakfast,pp.amount FROM	gd_ebooking_price AS p	INNER JOIN gd_ebooking_price_plan AS pp   on p.price_plan_id=pp.id WHERE	p.hotel_id =? 	AND p.price_plan_id =? 	AND p.room_type_id =? 	AND p.date >=?	AND p.date <=?  AND	( p.STATUS =? or p.STATUS =?)";
		//List<Record> list = Db.find(sql,HotelID,RoomID,RatePlanID,Arrival,Departure,1,100);
		List<Record> list = Db.find(sql);
		

		String tm1 = Arrival + " 14:00:00";
		String tm2 = Departure + " 17:00:00";
		Map<String, Object> data = new HashMap<String, Object>();// 总的json

		data.put("LadderType", 1);
		data.put("InvoiceMode", 1);
		data.put("PayType", "PP");
		
		
		List<Map<String, Object>> policyList = new ArrayList<Map<String, Object>>();
		int sum = 0,all_sum=0;
		int cut_type=0;
		int proportion=0;
		int firstDayPrice =0;
		int BookingCode=0;
		Map<String, Object> priceInfo=new HashMap<String, Object>();
		List<Map<String, Object>> oneDayList=new ArrayList<Map<String,Object>>();
		
		for (int i = 0; i < list.size(); i++) {
			Record record = list.get(i);
			int todaystockreal=record.getInt("todaystockreal");
			int price = record.getInt("price");//单价			
			cut_type=record.getInt("cut_type");//取消规则
			switch (cut_type) {
			case 0:
				sum=0;
				break;
			case 1:
				sum=record.getInt("amount");
				break;
			case 2:
				proportion=record.getInt("amount");
				sum+=price;
				break;
			case 3:
				if(i==0) {
					firstDayPrice=price;
				}
				break;
			case 4:
				sum+=price;
				break;
			default:
				break;
			}
			all_sum+=price;
			
			
			//组装priceInfo
			Map<String, Object> onDayPriceMap=new HashMap<String, Object>();
			Map<String, Object> onDayPrice=new HashMap<String, Object>();
			onDayPrice.put("AmountAfterTaxFee", price*100);
			onDayPriceMap.put("Price", onDayPrice);
			onDayPriceMap.put("Breakfast", record.getInt("breakfast"));
			onDayPriceMap.put("Date", record.getObject("date"));
			oneDayList.add(onDayPriceMap);
			
			
			if(todaystockreal<RoomNumber) {
				BookingCode=1000;
			}
			
			
		}
		priceInfo.put("DailyPrices", oneDayList);
		//priceInfo.put("PriceInfo", priceInfo);
		int days=StringKit.getDays(Arrival, Departure);
		if(list.size()<days) {
			BookingCode=1001;
		}
		if (BookingCode== 0) {
			data.put("BookingCode", "0");
			data.put("IsBookable", 1);
			data.put("Stock", 10);
			data.put("PriceInfo", priceInfo);
		} else {
			Map<String,String> map=new HashMap<String, String>();
			data.put("BookingCode", "1000");
			data.put("IsBookable", 0);
			data.put("Stock", 0);
			data.put("PriceInfo",map);
		}
		
		Map<String, Object> policy = new HashMap<String, Object>();
		Long timestamp_start = CommonUtils.timestamp(tm1);
		Long timestamp_end = CommonUtils.timestamp(tm2);
		policy.put("Start", timestamp_start);
		policy.put("End", timestamp_end);
		if(cut_type==2||cut_type==1) {
			policy.put("price",sum*proportion);
		}else if(cut_type==3) {
			policy.put("price", firstDayPrice*100);
		} else  {
			policy.put("price",sum*100);
		}
		
	
		
		policyList.add(policy);
		data.put("LadderDeductPolicyList", policyList);
		
		
//			Map<String, Object> priceInfo=new HashMap<String, Object>();
			Map<String, Object> total=new HashMap<String, Object>();
			Map<String, Object> AmountAfterTaxFee=new HashMap<String, Object>();
			AmountAfterTaxFee.put("AmountAfterTaxFee", all_sum*100);
			total.put("AmountAfterTaxFee", AmountAfterTaxFee);
			priceInfo.put("Total", total);
		
		System.out.println(JsonKit.toJson(data));
		
		JSONObject data_json=new JSONObject();
		data_json.put("data", JsonKit.toJson(data));
		data_json.put("result", true);
		
		return data_json;
	}

	public Record getPricePlan(String RatePlanID) {
		String sql = "select * from gd_ebooking_price_plan where id=?";
		return Db.findFirst(sql);
	}



	// 艺龙验价
	public JSONObject checkPrice2(String mHotelID, String mRoomID, String mRatePlanID, int mRoomNumber, String mArrival,
			String mDeparture) {

		JSONObject ret = new JSONObject();

		ElongHotelDataValidatelFace hotel_data_validate = new ElongHotelDataValidatelFace();

		JSONObject dataRequest = new JSONObject();
		// {"ArrivalDate":"2023-08-29","DepartureDate":"2023-08-30","HotelIds":"40101006","Options":"2","PaymentType":"All"}

		String ArrivalDate = mArrival;
		String DepartureDate = mDeparture;
		String EarliestArrivalTime = ArrivalDate + " 14:00:00";
		String LatestArrivalTime = DepartureDate + " 17:00:00";
		String HotelId = mHotelID;
		String RoomId = mRoomID;
		String RatePlanId = mRatePlanID;

		/**** 找出对应的 RoomTypeID *****/
		ElongHotelDetailFace hotel_detail = new ElongHotelDetailFace();
		JSONObject dataRequest_detail = new JSONObject();
		// {"ArrivalDate":"2023-08-29","DepartureDate":"2023-08-30","HotelIds":"40101006","Options":"2","PaymentType":"All"}
		dataRequest_detail.put("ArrivalDate", mArrival);
		dataRequest_detail.put("DepartureDate", mDeparture);
		dataRequest_detail.put("HotelIds", mHotelID);
		dataRequest_detail.put("Options", "2,12");
		dataRequest_detail.put("PaymentType", "All");
		hotel_detail.requestApi(dataRequest_detail);

		if (hotel_detail.hotelDetail == null || hotel_detail.isOk == false) {
			// 没有找到对应的酒店
			ret.put("result", false);
			return ret;
		}
		String RoomTypeId = hotel_detail.getRoomTypeID(RoomId, RatePlanId);
		if (Strings.isNullOrEmpty(RoomTypeId)) {
			// 没有找到对应的酒店
			ret.put("result", false);
			return ret;
		}

		/**** 找出对应的 RoomTypeID end *****/

		dataRequest.put("ArrivalDate", ArrivalDate);
		dataRequest.put("DepartureDate", DepartureDate);
		dataRequest.put("EarliestArrivalTime", EarliestArrivalTime);
		dataRequest.put("LatestArrivalTime", LatestArrivalTime);
		dataRequest.put("HotelId", HotelId);
		dataRequest.put("RoomId", RoomId);
		dataRequest.put("RoomTypeID", RoomTypeId);
		dataRequest.put("RatePlanId", RatePlanId);
		dataRequest.put("TotalPrice", hotel_detail.TotalRate * mRoomNumber);
		dataRequest.put("NumberOfRooms", mRoomNumber);

//		dataRequest.put("NumberOfAdults",200);
//        dataRequest.put("HotelCode",2);
//        dataRequest.put("SupplierId",2);

		hotel_data_validate.requestApi(dataRequest);

		/******* 开始匹配 艺龙 和 高德对应参数 *********/

		String Gaode_BookingCode = "1008"; // 默认

		String ResultCode = "";
		if (hotel_data_validate.hotelDataValidate == null || !hotel_data_validate.isOk) {

		} else {
			JSONObject Result = hotel_data_validate.hotelDataValidate.getJSONObject("Result");
			if (Result == null) {

			}
			ResultCode = Result.getString("ResultCode");
			Double GuaranteeRate = Result.getDouble("GuaranteeRate");
			String CancelTime = Result.getString("CancelTime");
			String FreeCancelTime = Result.getString("FreeCancelTime");
			String PenaltyAmount = Result.getString("PenaltyAmount"); // 罚金金额
		}

		JSONObject Result = hotel_data_validate.hotelDataValidate.getJSONObject("Result");

		if (ResultCode.equals("OK")) {
			Gaode_BookingCode = "0";
		}
		if (ResultCode.equals("Inventory")) {
			Gaode_BookingCode = "1000";
		}
		if (ResultCode.equals("Rate")) {
			Gaode_BookingCode = "1003";
		}
		Integer Gaode_IsBookable = 0; // 默认 1=可定，0=不可订
		if (ResultCode.equals("OK")) {
			Gaode_IsBookable = 1;
		}

		String Gaode_PayType = ""; // 默认
		String PaymentType = hotel_detail.RatePlan.getString("PaymentType");
		if (PaymentType.equals("Prepay")) {
			Gaode_PayType = "PP";
		}
		if (PaymentType.equals("SelfPay")) {
			Gaode_PayType = "FG";
		}

		// 这里总价，算上人数了 hotel_detail.TotalRate*mRoomNumber
		JSONObject Gaode_PriceInfo = new JSONObject();
		JSONObject Total = new JSONObject();
		double totalP = hotel_detail.TotalRate * mRoomNumber;
		totalP = CommonUtils.decimal2(totalP) * 100; // 转换成分
		int AmountAfterTaxFee = (int) totalP;
		Total.put("AmountAfterTaxFee", AmountAfterTaxFee); // 总卖价,单位：分
		Gaode_PriceInfo.put("Total", Total);

		//
		JSONArray DailyPrices = new JSONArray();

		JSONArray NightlyRates = hotel_detail.RatePlan.getJSONArray("NightlyRates");
		for (Object NightlyRate : NightlyRates) {
			JSONObject NightlyRateObj = (JSONObject) NightlyRate;
			double Member = NightlyRateObj.getDouble("Member"); // 显示会员价，后边需要改了再说
			Member = CommonUtils.decimal2(Member) * 100; // 转换成分
//			int Price=(int)Member;

			JSONObject Price = new JSONObject();
			int PriceAmountAfterTaxFee = (int) Member;
			Price.put("AmountAfterTaxFee", PriceAmountAfterTaxFee);
//			Price.put("AmountBeforePromotion",PriceAmountAfterTaxFee+1000);
//			JSONArray PromotionInfos=new JSONArray();
//			JSONObject PromotionInfo=new JSONObject();
//			PromotionInfo.put("PromotionName","高德专享");
//			PromotionInfo.put("PromotionPrice",1000);
//			PromotionInfos.add(PromotionInfo);
//			Price.put("PromotionInfos",PromotionInfos);

			String Date = NightlyRateObj.getString("Date");
			Date = Date.substring(0, 10);

			JSONObject DailyPrice = new JSONObject();
			DailyPrice.put("Date", Date);
			DailyPrice.put("Date", "2023-08-29");
			DailyPrice.put("Price", Price);

			DailyPrices.add(DailyPrice);

		}

		Gaode_PriceInfo.put("DailyPrices", DailyPrices);

		// Stock CurrentAlloment
		int Gaode_Stock = 0;// 默认 ,高德 0 是没库存

		// 艺龙 0 是不限
		// 入住时间内不能超售的最小值。当大于0小于5时，表示目前仅剩的房量；0表示房量充足，最少有1间可以预定，多间预定可能会失败
		int CurrentAlloment = hotel_detail.RatePlan.getInteger("CurrentAlloment");
		if (CurrentAlloment == 0) {
			Gaode_Stock = 999;
		} else {
			Gaode_Stock = CurrentAlloment;
		}

		if (CurrentAlloment > 0 && CurrentAlloment < 5) {

			// 看一下要订的房间数，是不是太多了
			if (mRoomNumber > CurrentAlloment) {
				Gaode_BookingCode = "1000";
				Gaode_IsBookable = 0;
			}
		}

		JSONArray Gaode_LadderDeductPolicyList = new JSONArray();

		JSONObject LadderDeductPolicy = new JSONObject();
		// 这个不可取消，也需要设个时间段，暂时设为 最晚入住时间
		Long timestamp_start = CommonUtils.timestamp(EarliestArrivalTime);
		Long timestamp_end = CommonUtils.timestamp(LatestArrivalTime);
		LadderDeductPolicy.put("Start", timestamp_start);
		LadderDeductPolicy.put("End", timestamp_end);
		LadderDeductPolicy.put("Price", (int) totalP); // todo 设成 全扣

		Gaode_LadderDeductPolicyList.add(LadderDeductPolicy);

		ret.put("result", true);

		JSONObject retData = new JSONObject();
		retData.put("BookingCode", Gaode_BookingCode);
		retData.put("IsBookable", Gaode_IsBookable);
		retData.put("PayType", Gaode_PayType);
		retData.put("PriceInfo", Gaode_PriceInfo);
		retData.put("Stock", Gaode_Stock);
		retData.put("LadderType", 1);

		// 先设为不可取消， 具体解析，要按 LadderParse节点 -- 0:不扣费；1:金额；2：比例；3：首晚房费；
		// 0:不扣费；1:金额；2：比例；3：首晚房费；
		// todo 这个后期再看业务需求更改
		retData.put("LadderDeductPolicyList", Gaode_LadderDeductPolicyList);

		ret.put("data", retData);

		return ret;

	}

}
