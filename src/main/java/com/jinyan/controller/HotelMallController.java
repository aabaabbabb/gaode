package com.jinyan.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;
import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.aop.Inject;
import com.jfinal.core.Path;
import com.jfinal.plugin.activerecord.Record;
import com.jinyan.common.Constant;
import com.jinyan.common.GdlogInterceptor;
import com.jinyan.controller.base.BaseController;
import com.jinyan.model.Hotel;
import com.jinyan.service.HotelService;
import com.jinyan.service.HotelStuffService;
import com.jinyan.service.PriceDbService;
import com.jinyan.utils.CommonUtils;
import com.jinyan.utils.ResponseUtils;
import com.jinyan.utils.ResponseUtilsOutResponse;
import com.jinyan.utils.StringUtils;

// 这个类 用来处理，其它相关联环竟中，需要处理的一些接口，

@Clear
@Before(GdlogInterceptor.class)
@Path(value = "/hotel/mall", viewPath = "")
public class HotelMallController extends BaseController {
	private static final Logger log = Logger.getLogger(HotelMallController.class);
	@Inject
	private HotelService hsrv;

	@Inject
	private HotelStuffService hsrvStuff;

	public static Map<String, String> headerMap = new HashMap<String, String>();
	
	
	static {
		headerMap.put("Content-Type", "application/x-www-form-urlencoded");
	}



	/***
	 * 高德调用酒店验价接口  【高德已连通】
	 *  biz_content
	 *
	 * 这个 是根据 艺龙文档两个接口
	 * 验价接口
	 * https://open.elong.com/doc/info/cn-api-search-hotel_data_validate
	 * 详情接口
	 * https://open.elong.com/doc/info/cn-api-search-hotel_detail#RatePlan
	 *
	 * 高德开发文档
	 * https://y.amap.com/docs/hotel/validateroomprice
	 */
//	@ActionKey("/hotel/mall/checkPrice")


	public void checkPrice() throws Exception {

		boolean userdb= Constant.USEDB;
		if(userdb){
			checkPriceDB();
			return;
		}

//		log.warn("------ checkPrice begin----------");

		String biz_content=getPara("biz_content");
//		log.warn(biz_content);
		JSONObject jsonObject =   JSONObject.parseObject(biz_content);
 		String HotelID = jsonObject.getString("HotelID");
		String RoomID = jsonObject.getString("RoomID");
		String RatePlanID = jsonObject.getString("RatePlanID");
		String Arrival = jsonObject.getString("Arrival");
		String Departure = jsonObject.getString("Departure");
		int RoomNumber = jsonObject.getIntValue("RoomNumber");

		Hotel hotel = hsrv.getHotelInfo(Long.parseLong(HotelID));



		Integer elongidInt=hotel.getElongId();

		String elongid= hotel.getElongIdStr();
		if(elongid.isEmpty()){
			elongid=Integer.toString(elongidInt);
		}


		JSONObject retobj=hsrvStuff.checkPrice(""+elongid,RoomID,RatePlanID,RoomNumber,Arrival,Departure);

		ResponseUtils res=new ResponseUtils();
		if(!retobj.getBoolean("result")){

			res.setCode("20000");
			res.setMsg("服务不可用,checkPrice fail" );
			res.setSub_code("isp.unknow-error");

			res.setSub_msg("服务暂不可用（业务系统不可用）:");

			ResponseUtilsOutResponse resOut=new ResponseUtilsOutResponse();
			resOut.setResponse(res);
			renderJson(resOut);
			return;
		}

		JSONObject data=retobj.getJSONObject("data");
		res.setCode("10000");
		res.setMsg("success");
		res.setData(JSON.toJSON(data));

		ResponseUtilsOutResponse resOut=new ResponseUtilsOutResponse();
		resOut.setResponse(res);
		renderJson(resOut);
		return;


//		int pageSize = jsonObject.getIntValue("pageSize");
//
//		List<Long> hList= hsrvStuff.getIncrStateIdList(type,currentPage,pageSize);
//
//		ResponseUtils res=new ResponseUtils();
//		res.setCode("10000");
//		res.setMsg("success");
//
//		Map<String,Object> rData=new HashMap<>();
//
//		rData.put("HotelID",hList);
////		res.setData(JSON.toJSON(hotelList ));
//		res.setData(JsonKit.toJson(rData));
//		ResponseUtilsOutResponse resOut=new ResponseUtilsOutResponse();
//		resOut.setResponse(res);
//		renderJson(resOut);

	}
	public void checkPriceDB() throws Exception {

		String dataMap = getRequest().getParameter("biz_content");
		JSONObject req_json = JSONObject.parseObject(dataMap);
		String HotelID=req_json.getString("HotelID");
		Hotel hotel=	hsrv.getHotelInfo(Long.parseLong(HotelID));
		String StartDate=req_json.getString("Arrival");
		String EndDate=req_json.getString("Departure");
		String RatePlanID=req_json.getString("RatePlanID");
		String RoomID=req_json.getString("RoomID");
		int RoomNumber = req_json.getIntValue("RoomNumber");

		if(RoomID==null){
			RoomID="";
		}
		if(RatePlanID==null){
			RatePlanID="";
		}



		Integer elongidInt= hotel.getElongId();

		int InvoiceMode=hotel.getInvoiceMode();

		PriceDbService pdbs=new PriceDbService();
//		String elongid=Integer.toString(elongidInt);


		String elongid= hotel.getElongIdStr();
		if(elongid.isEmpty()){
			elongid=Integer.toString(elongidInt);
		}


		JSONObject room_info_json=new JSONObject();
		room_info_json.put("HotelID", HotelID);

		JSONArray gaode_RoomInfos = new JSONArray();//返回给高德的房型数组

		List<Map<String, Object>> RatePlans_list=new ArrayList<Map<String,Object>>(); //

		List<Record> records= pdbs.getPriceList(  elongid,  StartDate,    EndDate,  RatePlanID,  RoomID,false);


		String objJson = JSONObject.toJSONString(records);//java对象转json
		String objjsonstr="------ Checkprice get list begin----------\r\n";
		  objjsonstr=objjsonstr+objJson+"\r\n";
		  objjsonstr=objjsonstr+"------ Checkprice get list end----------\n";
		log.warn(objjsonstr);

//		JSONObject msgEncap = JSON.parseObject("[{\"columnNames\":[\"date\",\"roomstock\",\"cut_type\",\"todaystockreal\",\"roomstockreal\",\"payway\",\"room_type_id\",\"elong_RoomId\",\"is_cancel\",\"advance_day\",\"price\",\"planstatus\",\"planname\",\"price_plan_id\",\"noroom\",\"planamount\",\"employ_role_id\",\"breakfast\",\"specific_date\",\"status\",\"todaystock\"],\"columnValues\":[\"2024-06-17\",10,3,0,9,2,57768,null,1,0,104.00,1,\"免房订单\",157680,0,0.00,0,0,\"16:00:00\",1,1],\"columns\":{\"date\":\"2024-06-17\",\"roomstock\":10,\"cut_type\":3,\"todaystockreal\":0,\"roomstockreal\":9,\"payway\":2,\"room_type_id\":57768,\"is_cancel\":1,\"advance_day\":0,\"price\":104.00,\"planstatus\":1,\"planname\":\"免房订单\",\"price_plan_id\":157680,\"noroom\":0,\"planamount\":0.00,\"employ_role_id\":0,\"breakfast\":0,\"specific_date\":\"16:00:00\",\"status\":1,\"todaystock\":1}},{\"columnNames\":[\"date\",\"roomstock\",\"cut_type\",\"todaystockreal\",\"roomstockreal\",\"payway\",\"room_type_id\",\"elong_RoomId\",\"is_cancel\",\"advance_day\",\"price\",\"planstatus\",\"planname\",\"price_plan_id\",\"noroom\",\"planamount\",\"employ_role_id\",\"breakfast\",\"specific_date\",\"status\",\"todaystock\"],\"columnValues\":[\"2024-06-18\",10,3,1,10,2,57768,null,1,0,104.00,1,\"免房订单\",157680,0,0.00,0,0,\"16:00:00\",1,1],\"columns\":{\"date\":\"2024-06-18\",\"roomstock\":10,\"cut_type\":3,\"todaystockreal\":1,\"roomstockreal\":10,\"payway\":2,\"room_type_id\":57768,\"is_cancel\":1,\"advance_day\":0,\"price\":104.00,\"planstatus\":1,\"planname\":\"免房订单\",\"price_plan_id\":157680,\"noroom\":0,\"planamount\":0.00,\"employ_role_id\":0,\"breakfast\":0,\"specific_date\":\"16:00:00\",\"status\":1,\"todaystock\":1}},{\"columnNames\":[\"date\",\"roomstock\",\"cut_type\",\"todaystockreal\",\"roomstockreal\",\"payway\",\"room_type_id\",\"elong_RoomId\",\"is_cancel\",\"advance_day\",\"price\",\"planstatus\",\"planname\",\"price_plan_id\",\"noroom\",\"planamount\",\"employ_role_id\",\"breakfast\",\"specific_date\",\"status\",\"todaystock\"],\"columnValues\":[\"2024-06-19\",10,3,1,10,2,57768,null,1,0,104.00,1,\"免房订单\",157680,0,0.00,0,0,\"16:00:00\",1,1],\"columns\":{\"date\":\"2024-06-19\",\"roomstock\":10,\"cut_type\":3,\"todaystockreal\":1,\"roomstockreal\":10,\"payway\":2,\"room_type_id\":57768,\"is_cancel\":1,\"advance_day\":0,\"price\":104.00,\"planstatus\":1,\"planname\":\"免房订单\",\"price_plan_id\":157680,\"noroom\":0,\"planamount\":0.00,\"employ_role_id\":0,\"breakfast\":0,\"specific_date\":\"16:00:00\",\"status\":1,\"todaystock\":1}}]");
//		JSONObject msgEncap   = JSON.parseArray(msgEncap.getString("msgList"), Record.class);
//		records= (List<Record>) JSONObject.parse("[{\"columnNames\":[\"date\",\"roomstock\",\"cut_type\",\"todaystockreal\",\"roomstockreal\",\"payway\",\"room_type_id\",\"elong_RoomId\",\"is_cancel\",\"advance_day\",\"price\",\"planstatus\",\"planname\",\"price_plan_id\",\"noroom\",\"planamount\",\"employ_role_id\",\"breakfast\",\"specific_date\",\"status\",\"todaystock\"],\"columnValues\":[\"2024-06-17\",10,3,0,9,2,57768,null,1,0,104.00,1,\"免房订单\",157680,0,0.00,0,0,\"16:00:00\",1,1],\"columns\":{\"date\":\"2024-06-17\",\"roomstock\":10,\"cut_type\":3,\"todaystockreal\":0,\"roomstockreal\":9,\"payway\":2,\"room_type_id\":57768,\"is_cancel\":1,\"advance_day\":0,\"price\":104.00,\"planstatus\":1,\"planname\":\"免房订单\",\"price_plan_id\":157680,\"noroom\":0,\"planamount\":0.00,\"employ_role_id\":0,\"breakfast\":0,\"specific_date\":\"16:00:00\",\"status\":1,\"todaystock\":1}},{\"columnNames\":[\"date\",\"roomstock\",\"cut_type\",\"todaystockreal\",\"roomstockreal\",\"payway\",\"room_type_id\",\"elong_RoomId\",\"is_cancel\",\"advance_day\",\"price\",\"planstatus\",\"planname\",\"price_plan_id\",\"noroom\",\"planamount\",\"employ_role_id\",\"breakfast\",\"specific_date\",\"status\",\"todaystock\"],\"columnValues\":[\"2024-06-18\",10,3,1,10,2,57768,null,1,0,104.00,1,\"免房订单\",157680,0,0.00,0,0,\"16:00:00\",1,1],\"columns\":{\"date\":\"2024-06-18\",\"roomstock\":10,\"cut_type\":3,\"todaystockreal\":1,\"roomstockreal\":10,\"payway\":2,\"room_type_id\":57768,\"is_cancel\":1,\"advance_day\":0,\"price\":104.00,\"planstatus\":1,\"planname\":\"免房订单\",\"price_plan_id\":157680,\"noroom\":0,\"planamount\":0.00,\"employ_role_id\":0,\"breakfast\":0,\"specific_date\":\"16:00:00\",\"status\":1,\"todaystock\":1}},{\"columnNames\":[\"date\",\"roomstock\",\"cut_type\",\"todaystockreal\",\"roomstockreal\",\"payway\",\"room_type_id\",\"elong_RoomId\",\"is_cancel\",\"advance_day\",\"price\",\"planstatus\",\"planname\",\"price_plan_id\",\"noroom\",\"planamount\",\"employ_role_id\",\"breakfast\",\"specific_date\",\"status\",\"todaystock\"],\"columnValues\":[\"2024-06-19\",10,3,1,10,2,57768,null,1,0,104.00,1,\"免房订单\",157680,0,0.00,0,0,\"16:00:00\",1,1],\"columns\":{\"date\":\"2024-06-19\",\"roomstock\":10,\"cut_type\":3,\"todaystockreal\":1,\"roomstockreal\":10,\"payway\":2,\"room_type_id\":57768,\"is_cancel\":1,\"advance_day\":0,\"price\":104.00,\"planstatus\":1,\"planname\":\"免房订单\",\"price_plan_id\":157680,\"noroom\":0,\"planamount\":0.00,\"employ_role_id\":0,\"breakfast\":0,\"specific_date\":\"16:00:00\",\"status\":1,\"todaystock\":1}}]");

// 日志中的结果，可以在这测一下
//		records=  JSONObject.parseArray("[{\"columnNames\":[\"date\",\"roomstock\",\"cut_type\",\"todaystockreal\",\"roomstockreal\",\"payway\",\"room_type_id\",\"elong_RoomId\",\"is_cancel\",\"advance_day\",\"price\",\"planstatus\",\"planname\",\"price_plan_id\",\"noroom\",\"planamount\",\"employ_role_id\",\"breakfast\",\"specific_date\",\"status\",\"todaystock\"],\"columnValues\":[\"2024-06-17\",10,3,0,9,2,57768,null,1,0,104.00,1,\"免房订单\",157680,0,0.00,0,0,\"16:00:00\",1,1],\"columns\":{\"date\":\"2024-06-17\",\"roomstock\":10,\"cut_type\":3,\"todaystockreal\":0,\"roomstockreal\":9,\"payway\":2,\"room_type_id\":57768,\"is_cancel\":1,\"advance_day\":0,\"price\":104.00,\"planstatus\":1,\"planname\":\"免房订单\",\"price_plan_id\":157680,\"noroom\":0,\"planamount\":0.00,\"employ_role_id\":0,\"breakfast\":0,\"specific_date\":\"16:00:00\",\"status\":1,\"todaystock\":1}},{\"columnNames\":[\"date\",\"roomstock\",\"cut_type\",\"todaystockreal\",\"roomstockreal\",\"payway\",\"room_type_id\",\"elong_RoomId\",\"is_cancel\",\"advance_day\",\"price\",\"planstatus\",\"planname\",\"price_plan_id\",\"noroom\",\"planamount\",\"employ_role_id\",\"breakfast\",\"specific_date\",\"status\",\"todaystock\"],\"columnValues\":[\"2024-06-18\",10,3,1,10,2,57768,null,1,0,104.00,1,\"免房订单\",157680,0,0.00,0,0,\"16:00:00\",1,1],\"columns\":{\"date\":\"2024-06-18\",\"roomstock\":10,\"cut_type\":3,\"todaystockreal\":1,\"roomstockreal\":10,\"payway\":2,\"room_type_id\":57768,\"is_cancel\":1,\"advance_day\":0,\"price\":104.00,\"planstatus\":1,\"planname\":\"免房订单\",\"price_plan_id\":157680,\"noroom\":0,\"planamount\":0.00,\"employ_role_id\":0,\"breakfast\":0,\"specific_date\":\"16:00:00\",\"status\":1,\"todaystock\":1}},{\"columnNames\":[\"date\",\"roomstock\",\"cut_type\",\"todaystockreal\",\"roomstockreal\",\"payway\",\"room_type_id\",\"elong_RoomId\",\"is_cancel\",\"advance_day\",\"price\",\"planstatus\",\"planname\",\"price_plan_id\",\"noroom\",\"planamount\",\"employ_role_id\",\"breakfast\",\"specific_date\",\"status\",\"todaystock\"],\"columnValues\":[\"2024-06-19\",10,3,1,10,2,57768,null,1,0,104.00,1,\"免房订单\",157680,0,0.00,0,0,\"16:00:00\",1,1],\"columns\":{\"date\":\"2024-06-19\",\"roomstock\":10,\"cut_type\":3,\"todaystockreal\":1,\"roomstockreal\":10,\"payway\":2,\"room_type_id\":57768,\"is_cancel\":1,\"advance_day\":0,\"price\":104.00,\"planstatus\":1,\"planname\":\"免房订单\",\"price_plan_id\":157680,\"noroom\":0,\"planamount\":0.00,\"employ_role_id\":0,\"breakfast\":0,\"specific_date\":\"16:00:00\",\"status\":1,\"todaystock\":1}}]",Record.class);


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

			if(Strings.isNullOrEmpty(room_type_id)){
				continue;
			}

			if(Strings.isNullOrEmpty(price_plan_id)){
				continue;
			}

			if(payway==null || !payway.equals("2")){ //过滤掉 只要预付
				continue;
			}


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



			List dateList;
			if(!RatePlanid_Dates.containsKey(price_plan_id)){
				dateList=new ArrayList();
			}else{
				dateList=RatePlanid_Dates.get(price_plan_id);
			}
			dateList.add(record);
			RatePlanid_Dates.put(price_plan_id,dateList);


		}

		JSONArray RoomInfos=new JSONArray();
		JSONObject RatePlan=new JSONObject(); //用这个作为返回数据

		Iterator<Map.Entry<String,List>> iterable=RoomType_RatePlanids.entrySet().iterator();
		if(iterable.hasNext()){ //只取一个room  while 改成 if
			Map.Entry<String,List>entry=iterable.next();

			String key=entry.getKey();
			List valueRatePlanids=entry.getValue();



			int valueRatePlanidsLen=valueRatePlanids.size();
			for (int i = 0; i < valueRatePlanidsLen; i++) {

//						System.out.println(valueRatePlanids.get(i));
				String valueRatePlanid=(String) valueRatePlanids.get(i);
//				RatePlan.put("RatePlanID",valueRatePlanid);

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
					int breakfast= StringUtils.nullToEmpty(valueRecode.getInt("breakfast"));
					DailyPrice.put("Breakfast",breakfast);

					//价格
					JSONObject Price=new JSONObject();
					double Cost = valueRecode.getDouble("price");//
                    int employ_role_id =valueRecode.getInt("employ_role_id");
                    if(employ_role_id==4){
                        Cost=CommonUtils.FixCostPrice(Cost); //底价处理成卖价
                    }


					Cost= CommonUtils.decimal2(Cost) * 100; //转换成分

					Cost=Cost*RoomNumber;

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

					if(todaystock<roomstocktemp){ //stock 和 today_stock 两个值 如果today_stock < stock 取today_stock  否则取stock
						roomstocktemp=todaystockreal;
					}else{
						roomstocktemp=roomstockreal;
					}

					if(ri==0){ //第一次把值给roomstock,为了取最小库存
						roomstock=roomstocktemp;
					}else{
						if(roomstocktemp<roomstock){
							roomstock=roomstocktemp;
						}
					}



				}


				if(valueRecode!=null){


					//目前全作预付的
					String PayWay=valueRecode.getStr("payway");
					RatePlan.put("PayType","PP"); //默认预付
					if(PayWay.equals("1")){
						RatePlan.put("PayType"," FG");
					}
//					roomstock= StringUtils.nullToEmpty(valueRecode.getInt("roomstock"));
//					roomstock= StringUtils.nullToEmpty(valueRecode.getInt("roomstockreal"));




					RatePlan.put("Stock",roomstock);

					if(InvoiceMode==1 ||  InvoiceMode==2){
						RatePlan.put("InvoiceMode",InvoiceMode);
					}

//					RatePlan.put("Stock",999);

//					String planname=  StringUtils.nullToEmpty(valueRecode.getStr("planname")) ;
//					RatePlan.put("RatePlanName",planname);

//					RatePlan.put("IsInstantConfirm",false);


					int is_cancel= StringUtils.nullToEmpty(valueRecode.getInt("is_cancel"));
					//1:可以取消；2：不可取消
					if(is_cancel==0){
						is_cancel=2;
					}

					int LadderType=1;
					if(is_cancel==2){  	//1:可以取消；2：不可取消


						Map<String, Object> LadderDeductPolicyEntity_map=new HashMap<String, Object>();

						LadderDeductPolicyEntity_map.put("Start", CommonUtils.timeStringToLong("2023-01-01"));
						LadderDeductPolicyEntity_map.put("End", CommonUtils.timeStringToLong(EndDate));
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
							Long fromtime= CommonUtils.timeStringToLong(StartDate)-cutfromtime;


							Map<String, Object> LadderDeductPolicyEntity_map=new HashMap<String, Object>(); //不扣费
							LadderDeductPolicyEntity_map.put("Start", fromtime);
							LadderDeductPolicyEntity_map.put("End", CommonUtils.timeStringToLong(EndDate));
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
							cuts=cuts -  second  * 1000;

							//计算减多少时间



							Long starttime= CommonUtils.timeStringToLong(StartDate);
							Long midtime= starttime-cuts;
							Long fromtime= midtime -  1* 24 * 60 * 60 * 1000 ; //多减一天



							Map<String, Object> LadderDeductPolicyEntity_map=new HashMap<String, Object>(); //不扣费
							LadderDeductPolicyEntity_map.put("Start", fromtime);
							LadderDeductPolicyEntity_map.put("End", midtime);
							LadderDeductPolicyEntity_map.put("Price",0);
							LadderDeductPolicyEntity_list.add(LadderDeductPolicyEntity_map);


							Map<String, Object> LadderDeductPolicyEntity_map_pay=new HashMap<String, Object>();
							LadderDeductPolicyEntity_map_pay.put("Start", midtime);
							LadderDeductPolicyEntity_map_pay.put("End", CommonUtils.timeStringToLong(EndDate));


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

									double Cost= Price_Total_AmountAfterTaxFee * CommonUtils.decimal2(planamount*0.01) ; //转换成分
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


				//添加 BookingCode IsBookable
				//status   planstatus
				int status= StringUtils.nullToEmpty(valueRecode.getInt("status"));
				int planstatus= StringUtils.nullToEmpty(valueRecode.getInt("planstatus"));
//				if(status==1 && planstatus==1  && roomstock>= RoomNumber){
				if(status>0 && planstatus==1  && roomstock>= RoomNumber){
					RatePlan.put("BookingCode","0");
					RatePlan.put("IsBookable",1);
				}else{
					RatePlan.put("BookingCode","1000");
					RatePlan.put("IsBookable",0);
				}

				break; //只取一个planid
			}



		}

		ResponseUtils res = new ResponseUtils();
		res.setCode("10000");
		res.setMsg("success");
		res.setData(RatePlan); //要放data

		ResponseUtilsOutResponse resOut=new ResponseUtilsOutResponse();
		resOut.setResponse(res);

		renderJson(resOut);

	}

	public void checkPriceDBNone() throws Exception {
		JSONObject room_info_json=new JSONObject();
		room_info_json.put("BookingCode", "1000");
		room_info_json.put("IsBookable", 0);
		room_info_json.put("PayType", "PP");


		ResponseUtils res = new ResponseUtils();
		res.setCode("10000");
		res.setMsg("success");
		res.setData(room_info_json); //要放data

		ResponseUtilsOutResponse resOut=new ResponseUtilsOutResponse();
		resOut.setResponse(res);

		renderJson(resOut);

	}
	


}
