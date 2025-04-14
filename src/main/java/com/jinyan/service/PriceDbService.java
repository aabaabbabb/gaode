package com.jinyan.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.beust.jcommander.Strings;
import com.jfinal.kit.HttpKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.ehcache.CacheKit;
import com.jinyan.model.*;
import com.jinyan.utils.CommonUtils;
import com.jinyan.utils.RedisUtil;
import com.jinyan.utils.ResponseUtils;
import com.jinyan.utils.ResponseUtilsOutResponse;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
处理一些 price 相关的业务，
 */

public class PriceDbService {

	private EbookingPrice EbookingPriceDao= new EbookingPrice().dao();


	public List<Record> getPriceList(String elongid,String StartDate,String EndDate,String RatePlanID,String RoomID){
 			return getPriceList(  elongid,  StartDate,  EndDate,  RatePlanID,  RoomID,true);
	}
	public List<Record> getPriceList(String elongid,String StartDate,String EndDate,String RatePlanID,String RoomID,Boolean useCache
	){


		/*
		id hotel_id price_plan_id room_type_id
date price is_del update_time create_time oper_id oper_name status
breakfast 0不包含 1分早餐 2分早餐。。。。。100不限
		 */

//		String sql = "select price.*" +
		String sql = "select price.order_rooms_sum,price.price_plan_id,price.room_type_id,price.date,price.price,price.status,price.elong_RoomId,price.employ_role_id,"
				+ "plan.name as planname,plan.status as planstatus,plan.advance_day,plan.specific_date,plan.is_cancel,plan.minDays,plan.maxDays,plan.minAdvHours,"
				+ "plan.maxAdvHours,plan.startTime,plan.endTime,plan.breakfast,plan.payway,plan.cut_type,plan.amount as planamount,room.stock as roomstock ,"
				+ "room.stock - price.order_rooms_sum as roomstockreal ,price.today_stock as todaystock ,price.today_stock - price.order_rooms_sum as todaystockreal ,"
				+ "hotel.noroom as noroom  from gd_ebooking_price as price  left join gd_ebooking_price_plan as plan on price.price_plan_id= plan.id  "
				+ "left join gd_hotel as hotel on price.hotel_id= hotel.ElongIdStr  left join gd_ebooking_room_type as room on price.room_type_id= room.id "
				+ "where price.hotel_id='"+elongid+"'  and ( (hotel.noroom=0 and price.es_status=1) or hotel.noroom=1   or price.status=100)  "
				+ "and room.stock > price.order_rooms_sum and price.status>0 and price.price>0 and plan.status=1  and price.date>='"+StartDate+"' "
				+ "and price.date<'"+EndDate+"' ";

		if(!RatePlanID.isEmpty()){
			sql=sql+" and price.price_plan_id = "+RatePlanID;
		}
		if(!RoomID.isEmpty()){
			sql=sql+" and price.room_type_id = "+RoomID;
		}
//		List<Record> h = Db.find(sql);

		String  key= String.format("getPriceList|%s|%s|%s|%s|%s", elongid,StartDate,EndDate,RatePlanID,RoomID);
		List<Record> h = CacheKit.get("price/list", key);
		if (h == null || !useCache) {
			h =  Db.find(sql);
			CacheKit.put("price/list", key, h);
		}

		return  h ;
	}

	public List<Record> getPriceListPress(String elongid,String StartDate,String EndDate,String RatePlanID,String RoomID){


		/*
		id hotel_id price_plan_id room_type_id
date price is_del update_time create_time oper_id oper_name status
breakfast 0不包含 1分早餐 2分早餐。。。。。100不限
		 */

		String sql = "select price.*" +
				" from gd_ebooking_price as price" +
				"  where price.hotel_id='"+elongid+"' " +
				"and price.date>='"+StartDate+"' and price.date<'"+EndDate+"' ";

		if(!RatePlanID.isEmpty()){
			sql=sql+" and price.price_plan_id = "+RatePlanID;
		}
		if(!RoomID.isEmpty()){
			sql=sql+" and price.room_type_id = "+RoomID;

		}


		String  key= String.format("%s|%s|%s|%s|%s", elongid,StartDate,EndDate,RatePlanID,RoomID);
		List<Record> h = CacheKit.get("price/list", key);
//        List<Record> h = Db.find(sql);
		if (h == null) {
			h =  Db.find(sql);
			CacheKit.put("price/list", key, h);
		}
		return  h ;
	}


	public void ReordPrice(Long gdid,String elongid,int userid,int userpid,String HotelName,String utc_timestamp, String app_id,String RatePlanID, String RoomID ,String StartDate, String EndDate, ResponseUtilsOutResponse res,boolean iscache){
		if(!utc_timestamp.isEmpty() && !app_id.isEmpty()){
			// record
//			RecordPushThread thread=new RecordPushThread(  gdid,  elongid ,  userid,  userpid, HotelName,utc_timestamp,app_id, RatePlanID,RoomID ,StartDate,EndDate,res,iscache);
//			thread.start();
		}
	}

	public static void main(String[] args) {
		PriceDbService pdbs=new PriceDbService();
		String elongid="93614049";
		String StartDate="2023-09-05";
		String EndDate="2023-09-06";
		String RatePlanID="";
		String RoomID="";
		List<Record> records= pdbs.getPriceList(  elongid,  StartDate,    EndDate,  RatePlanID,  RoomID);
		for (Record record : records) {
			String name=record.getStr("name");
		}
	}
}



//处理记录
//(String utc_timestamp, String app_id, ResponseUtilsOutResponse res,boolean iscache)
class RecordPushThread extends Thread {

	private Long gdid;
	private String elongid;
	private int userid;
	private int userpid;
	private String HotelName;
	private String utc_timestamp;
	private String app_id;
	private String RatePlanID;
	private String RoomID;
	private String StartDate;
	private String EndDate;
	private ResponseUtilsOutResponse res;
	private boolean iscache;

	public RecordPushThread(Long gdid,String elongid ,int userid,int userpid, String HotelName ,String utc_timestamp, String app_id ,String RatePlanID, String RoomID ,String StartDate, String EndDate, ResponseUtilsOutResponse res,boolean iscache) {
		this.gdid = gdid;
		this.elongid = elongid;
		this.userid = userid;
		this.userpid = userpid;
		this.HotelName = HotelName;
		this.utc_timestamp = utc_timestamp;
		this.app_id = app_id;
		this.RatePlanID = RatePlanID;
		this.RoomID = RoomID;
		this.StartDate = StartDate;
		this.EndDate = EndDate;
		this.res = res;
		this.iscache = iscache;
	}

	public void run() {
		ResponseUtils responseUtils= this.res.getResponse();
		Object o=responseUtils.getData();




		try{

			Hotelview hotelview=new Hotelview();
			if(this.iscache){
				hotelview.setCache(1);
			}
			if(!Strings.isStringEmpty(this.RatePlanID)){
				hotelview.setIssubmit(1);
			}
			hotelview.setGdid(this.gdid);
			hotelview.setUserid(this.userid);
			hotelview.setUserpid(this.userpid);

			hotelview.setElongId(Integer.parseInt(this.elongid));
			hotelview.setHotelName(this.HotelName);
			hotelview.setStartDate(this.StartDate);
			hotelview.setEndDate(this.EndDate);
			hotelview.setCreated(new Date());

			JSONObject	room_info_json;
			room_info_json =   (JSONObject)(o);

			JSONArray	RoomInfos = room_info_json.getJSONArray("RoomInfos");
			if( RoomInfos.size()>0 ){

				hotelview.setHaveroom(1);
			}

			hotelview.save();

			Jedis jedis = RedisUtil.getConn();
			String jedis_key="RecordHotelView"+hotelview.getId();

			String sss=room_info_json.toString();

			jedis.set(jedis_key,sss);
			Integer expireSecond=86400*10 ;//
			jedis.expire(jedis_key, expireSecond);//秒
			jedis.close();

		}catch (Exception e){
//

		}

	}
}
