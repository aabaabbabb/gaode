package com.jinyan.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;
import com.jfinal.kit.HttpKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jinyan.common.Constant;
import com.jinyan.controller.HotelMallController;
import com.jinyan.model.*;
import com.jinyan.utils.CommonUtils;
import com.jinyan.utils.GenerateSignUtils;
import com.jinyan.utils.RedisUtil;
import com.jinyan.utils.StringUtils;
import com.sun.org.apache.xpath.internal.objects.XNull;
import org.apache.log4j.Logger;
import org.jsoup.helper.StringUtil;
import redis.clients.jedis.Jedis;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/*
处理一些 Order 相关的业务，
 */

public class HotelOrderService {

	/*
	艺龙状态参考
	array('F'=>'已入住','C'=>'已结帐','H'=>'变更','E'=>'取消','O'=>'满房','G'=>'变价','W'=>'虚拟','Z'=>'删除,另换酒店','I'=>'大单','P'=>'暂无价格','R'=>'预付','U'=>'特殊满房','T'=>'计划中','B1'=>'有预定未查到','B2'=>'待查','B3'=>'暂不确定','J'=>'仅酒店已确认','V'=>'已审','S'=>'特殊','M'=>'恶意','N'=>'新单','A'=>'已确认','D'=>'删除','B'=>'NO SHOW')
	 */

//	private Order orderDao = new Order().dao();

	//后边加 AmapOrderID
	//redis_perfix_order_biz_content_{AmapOrderID}
	private String redis_perfix_order_biz_content="redis_perfix_order_biz_content_"; // 存在 ledisdb 中的   键

	
	public static void main(String[]args) {
		
		Jedis jedis = RedisUtil.getConn();
		String jedis_key="123";

		jedis.set(jedis_key,"叶涛");
		jedis.expire(jedis_key, 86400*20);
		jedis.close();
		System.out.println(jedis);
	}

	//返回 CpOrderID
	public String createOrder(String biz_content) throws Exception{
		System.out.println(biz_content);

		JSONObject jsonObject = JSONObject.parseObject(biz_content);
		String AmapOrderID = jsonObject.getString("AmapOrderID");
//		Jedis jedis = RedisUtil.getConn();




		Long HotelID = jsonObject.getLongValue("HotelID");
		String RoomID = jsonObject.getString("RoomID");
		String RatePlanID = jsonObject.getString("RatePlanID");
		int Quantity = jsonObject.getIntValue("Quantity");
		String Arrival = jsonObject.getString("Arrival");
		String Departure = jsonObject.getString("Departure");
		String EarlyArrivalTime = jsonObject.getString("EarlyArrivalTime");
		String LastArrivalTime = jsonObject.getString("LastArrivalTime");
		int Person = jsonObject.getIntValue("Person");
		String PersonName = jsonObject.getString("PersonName");
		String PersonMobile = jsonObject.getString("PersonMobile");
		String PersonEmail = jsonObject.getString("PersonEmail");
		BigDecimal TotalPrice = jsonObject.getBigDecimal("TotalPrice");
		BigDecimal OrderRealPrice = jsonObject.getBigDecimal("OrderRealPrice");
		String BalanceType = jsonObject.getString("BalanceType");

		JSONArray Guests = jsonObject.getJSONArray("Guests");
		String Notice = jsonObject.getString("Notice");

		Order order=new Order();

		Boolean isNew=true;
		Order order2=order.findFirst("select * from gd_order where AmapOrderID='"+AmapOrderID+"' ");
		if(order2!=null){
			order=order2;
			isNew=false;

		}

		order.setAmapOrderID(AmapOrderID);
 		order.setHotelID(  HotelID);
 		order.setRoomID(  RoomID);
 		order.setQuantity(  Quantity);
 		order.setRatePlanID(  RatePlanID);
 		order.setArrival(  Arrival);
 		order.setDeparture(  Departure);
 		order.setEarlyArrivalTime(  EarlyArrivalTime);
 		order.setLastArrivalTime(  LastArrivalTime);
 		order.setPerson(  Person);
 		order.setPersonName(  PersonName);
 		order.setPersonMobile(  PersonMobile);
 		order.setPersonEmail(  PersonEmail);
 		order.setTotalPrice( TotalPrice);
 		if(OrderRealPrice!=null){
			order.setOrderRealPrice(  OrderRealPrice);
		}
		order.setBalanceType( BalanceType);

 		String guests="";
 		for(Object guestObj:Guests){
 			JSONObject gu=(JSONObject)guestObj;
			String ChineseName=gu.getString("ChineseName");
			guests=guests+ChineseName+",";
		}

 		if(StringUtils.endsWithIgnoreCase(guests,",")){
			guests = guests.substring(0, guests.length() - 1);
		}

 		order.setGuests(guests);
 		order.setNotice(Notice);


 		order.setEstatus("V");

	    Date day=new Date();
//		SimpleDateFormat now= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		order.setCreated(day);
		order.setUpdated(day);




		if(isNew){
			order.save();
		}else{
			order.update();
		}


		Integer id=order.getId();





		//String jedis_key=redis_perfix_order_biz_content+AmapOrderID;

//		jedis.set(jedis_key,biz_content);
//		jedis.expire(jedis_key, 86400*20);
//		jedis.close();

	    /*  其它地方，如果需要完整 biz_content ，可以这样取
		String b=jedis.get(jedis_key);
		 */

//		this.orderNotice(String.valueOf(id));

		//提交信息到小店家
		AmapOrderPushThread thread=new AmapOrderPushThread(String.valueOf(id));
		thread.start();

		return String.valueOf(id);
	}

	//
	public boolean cancleOrder(String biz_content) throws Exception{


		JSONObject jsonObject = JSONObject.parseObject(biz_content);
		String AmapOrderID = jsonObject.getString("AmapOrderID");
		String CpOrderID = jsonObject.getString("CpOrderID");


		String sql= "select * from gd_order where AmapOrderID='"+AmapOrderID+"' and id="+CpOrderID+" ";

		Order order=new Order().findFirst(sql);
		if(order!=null){

//			order.setEstatus("E");
			order.setEstatus("B2");
			order.setUpdated(new Date());

			order.setCancelstatus(1); //申请取消标志置1
			order.setCancelReqDatetime(new Date());

			order.update();

			this.orderNotice(CpOrderID,"cancle");
			HttpKit.get("http://api.quchuchai.cn/ebookinggd.order/orderCancelXdj?CpOrderID="+CpOrderID);
			HttpKit.get("http://api.xiaodianjia.cn/gdapi/update/orderUnStatus?gdid="+order.getId());
			return  true;
		}
		return  false;
	}
	public JSONObject getInfo(String biz_content) throws Exception{

		JSONObject jsonData = JSONObject.parseObject(biz_content);

		JSONObject jsonObject = JSONObject.parseObject(biz_content);
		String AmapOrderID = jsonObject.getString("AmapOrderID");
		String CpOrderID = jsonObject.getString("CpOrderID");


		String sql= "select * from gd_order where AmapOrderID='"+AmapOrderID+"' and id="+CpOrderID+" ";

//		Order order=new Order().findFirst(sql);

		Order order=new Order();
		order= order.findFirst(sql);

		if(order!=null){

			jsonData.put("AmapOrderID",order.getAmapOrderID());

			Integer OrderStatus = 0;
			String estatus=order.getEstatus();
//			if(estatus.equals("N")){ //艺龙新单，就是高德 待确认
//				OrderStatus=1;
//			}

			if(CommonUtils.contains(new String[]{"N","V"},estatus)){
				OrderStatus=1;
			}

			if(CommonUtils.contains(new String[]{"B2"},estatus)){
				OrderStatus=5;
			}

			if(estatus.equals("A")){ // 就是高德  确认
				OrderStatus=3;
			}

			if(CommonUtils.contains(new String[]{"O","G","W","Z","P","M"},estatus)){
				OrderStatus=2;
			}
			if(CommonUtils.contains(new String[]{"E"},estatus)){
				OrderStatus=6;
			}
			if(CommonUtils.contains(new String[]{"C","F"},estatus)){
				OrderStatus=7;
			}
			jsonData.put("OrderStatus",OrderStatus);


			/* PenaltyAmount PenaltyAmount */
			if(OrderStatus==6){
				//{"advance_day":0,"amount":30.00,"cut_type":2,"specific_date":"16:00:00","is_cancel":1}
				if(order.getDiffstatus()==1){
//					String cancelShow=order.getCancelshow();
//					JSONObject jsonObjectcancelShow =   JSONObject.parseObject(cancelShow);
//					int advance_day = jsonObjectcancelShow.getIntValue("advance_day");
//					float amount = jsonObjectcancelShow.getFloatValue("amount");
//					int cut_type = jsonObjectcancelShow.getIntValue("cut_type");
					jsonData.put("PenaltyAmount",order.getPenaltyAmount());
				}
			}
			BigDecimal getPortionRefundAmount=order.getPortionRefundAmount();
			if(getPortionRefundAmount.compareTo(BigDecimal.ZERO)!=0){
				jsonData.put("PortionRefundAmount",getPortionRefundAmount);
			}


			jsonData.put("HasInvoice",false);

			jsonData.put("CpOrderID",String.valueOf(order.getId()) );
			jsonData.put("Arrival",order.getArrival().substring(0,10) );
			jsonData.put("Departure",order.getDeparture().substring(0,10) );
			jsonData.put("TotalPrice",String.valueOf(order.getTotalPrice() ));

			//jsonData.put("PortionRefundAmount", "");//
			jsonData.put("CurrencyCode","RMB");
			jsonData.put("ReturnCode",0);
			jsonData.put("ReturnDescript","查询成功");



			//更新高德读取记录
			order.setGdestatus(estatus);
			order.setGdestatusTime(new Date());

			order.update();

		}else{
			jsonData.put("ReturnCode",1001);
			jsonData.put("ReturnDescript","业务失败");
			throw  new Exception("没找到订单");
		}

		return jsonData;


	}

	//获取订单变化列表
	public List<Record> getOrderChangedList(String  StartTime, String EndTime){


		String sql = "select * from gd_order_change where   `update` >='"+StartTime+"' and   `update` <= '"+EndTime+"' ";
		List<Record> h = Db.find(sql);

		return  h ;
	}
	//变化订单
	/*
	有订单变化时，调用这个
	 */
	public boolean orderChanged(String id){

		String sql= "select * from gd_order where  id="+id+" ";

		Order order=new Order().findFirst(sql);
		if(order==null){
			return false;
		}

		String AmapOrderID=order.getAmapOrderID();

		if(Strings.isNullOrEmpty(AmapOrderID)){
			return false;
		}

		Boolean isNew=false;
		OrderChange oChg=new OrderChange().findFirst("select * from gd_order_change where  CpOrderID="+id+" ");
		if(oChg==null){
			isNew=true;
			oChg=new OrderChange();
		}

		oChg.setAmapOrderID(AmapOrderID);

		Integer CpOrderID;
		try {
			CpOrderID = Integer.parseInt(id);
		}
		catch (NumberFormatException e) {
			 return  false;
		}
		oChg.setCpOrderID(CpOrderID);

		oChg.setUpdate(new Date());

		if(isNew){
			oChg.save();
		}else{
			oChg.update();
		}

		this.orderNotice(id);

		return true;
	}


	//带有订单状态改变，小店家过来的
	public boolean orderXdjChanged(String id,String estatus){

		String sql= "select * from gd_order where  id="+id+" ";

		Order order=new Order().findFirst(sql);
		if(order==null){
			return false;
		}

		String AmapOrderID=order.getAmapOrderID();

		if(Strings.isNullOrEmpty(AmapOrderID)){
			return false;
		}

		order.setEstatus(estatus);
		order.update();

		Boolean isNew=false;
		OrderChange oChg=new OrderChange().findFirst("select * from gd_order_change where  CpOrderID="+id+" ");
		if(oChg==null){
			isNew=true;
			oChg=new OrderChange();
		}

		oChg.setAmapOrderID(AmapOrderID);

		Integer CpOrderID;
		try {
			CpOrderID = Integer.parseInt(id);
		}
		catch (NumberFormatException e) {
			 return  false;
		}
		oChg.setCpOrderID(CpOrderID);

		oChg.setUpdate(new Date());

		if(isNew){
			oChg.save();
		}else{
			oChg.update();
		}


		this.orderNoticexdj(id);

		return true;
	}

	public boolean orderNotice(String id){

		String url="http://api.tetuijiudian.com/shuia/index.php?m=gaode&a=orderchg&id="+id;
		String ret = HttpKit.get(url);

		return true;
	}
	public boolean orderNoticexdjLog(String id,String estatus){

		//通知信息
		OrderXdjChangeLogThread thread=new OrderXdjChangeLogThread(id,estatus);
		thread.start();


		return true;
	}
	public boolean orderNoticexdj(String id){

		//通知信息
		OrderXdjChangeThread thread=new OrderXdjChangeThread(id);
		thread.start();


//		String url="http://api.tetuijiudian.com/shuia/index.php?m=gaode&a=orderchg&from=xdj&id="+id;
//		String ret = HttpKit.get(url);

		return true;
	}


	public boolean orderNotice(String id,String txt){

//		String url="http://api.tetuijiudian.com/shuia/index.php?m=gaode&a=orderchg&id="+id+"&txt="+txt;
//		String ret = HttpKit.get(url);


		//通知信息
		OrderCancelThread thread=new OrderCancelThread(id,txt);
		thread.start();

		return true;
	}


	public static void main22(String[] args) {


		//提交信息到小店家
		AmapOrderPushThread thread=new AmapOrderPushThread("10000");
		thread.start();

//		Map<String, String> data = new HashMap<String, String>();
//		data.put("order_num", "10011");
//
//
//		data.put("status", "C");
//
//
//
//		Map<String, String> headers = new HashMap<String, String>(16);
//		headers.put("Content-Type", "application/x-www-form-urlencoded");
//
////		String ret = HttpKit.post("http://127.0.0.1/shuia/index.php?m=gaode&a=getGetOrder", null, CommonUtils.asUrlParams(data),headers);
//		String qstr = CommonUtils.asUrlParams(data);
//
//		String ret = HttpKit.post("http://call.quchuchai.cn/Gaode/addOrder", null, qstr, headers);
////		String ret = HttpKit.post("http://call.quchuchai.cn/Gaode/addOrder", null, dataMap.toString(),headers);
//		System.out.println("ret" + ret);

	}


}


// 取消订单
class OrderCancelThread extends Thread {
	private static final Logger log = Logger.getLogger(OrderCancelThread.class);
	private String id;
	private String txt;

	public OrderCancelThread(String id, String txt) {
		this.id = id;
		this.txt = txt;
	}

	public void run() {

		String url="http://api.tetuijiudian.com/shuia/index.php?m=gaode&a=orderchg&id="+id+"&txt="+txt;


		log.info("------ OrderCancelThread begin----------"+url);
		String ret = HttpKit.get(url);
		log.info(ret);
		log.info("------ OrderCancelThread end----------");
	}
}

// 小店家更新
class OrderXdjChangeThread extends Thread {
	private static final Logger log = Logger.getLogger(OrderXdjChangeThread.class);
	private String id;

	public OrderXdjChangeThread(String id) {
		this.id = id;
	}

	public void run() {

		String url="http://api.tetuijiudian.com/shuia/index.php?m=gaode&a=orderchg&from=xdj&id="+id;

		log.info("------ OrderXdjChangeThread begin----------"+url);
		String ret = HttpKit.get(url);
		log.info(ret);
		log.info("------ OrderXdjChangeThread end----------");
	}
}
// 小店家更新 记录
class OrderXdjChangeLogThread extends Thread {
	private static final Logger log = Logger.getLogger(OrderXdjChangeLogThread.class);
	private String id;
	private String estatus;

	public OrderXdjChangeLogThread(String id,String estatus) {
		this.id = id;
		this.estatus = estatus;
	}

	public void run() {

		String url="http://api.tetuijiudian.com/shuia/index.php?m=gaode&a=xdjstatuschg&id="+id+"&estatus="+estatus;

		log.info("------ OrderXdjChangeLogThread begin----------"+url);
		String ret = HttpKit.get(url);
		log.info(ret);
		log.info("------ OrderXdjChangeLogThread end----------");
	}
}
//处理一些订单的通知
 class AmapOrderPushThread extends Thread {
	private static final Logger log = Logger.getLogger(AmapOrderPushThread.class);
	private String id;

	public AmapOrderPushThread(String id) {
		this.id = id;
	}

	public void run() {
		log.info("------ AmapOrderPushThread begin----------");
		Order order=new Order();
		Order order2=order.findFirst("select * from gd_order where id='"+this.id+"' ");
		if(order2==null){
			log.info("------ no id----------"+this.id);
			return;
		}


		Integer quantity=order2.getQuantity();


		try{

			EbookingRoomType roomDao = new EbookingRoomType().dao().findById(order2.getRoomID());
			 EbookingPricePlan plan = new EbookingPricePlan().dao().findById(order2.getRatePlanID());


			Map<String, Object> data  = new HashMap<String, Object>();
			data.put("order_num", this.id);


			HotelService hsrv=new HotelService();

		Hotel hotel = hsrv.getHotelInfo(order2.getHotelID()); // 这里要是 长整型，高德id 长整型，如果用整形的经常报错



		Integer ElongId = hotel.getElongId();

		data.put("HotelID",String.valueOf(ElongId) );
		data.put("hotelname",hotel.getHotelName());
		data.put("roomType_id",order2.getRoomID());



		data.put("roomType_id",order2.getRoomID());
		data.put("roomName",roomDao.getName().replace("&","-"));
		data.put("ratePlan_id",String.valueOf(plan.getId()));
		data.put("planName",plan.getName());

		//开始写 pricesDetails

			String sqlPara = "select price.*" +
					" from gd_ebooking_price as price " +
					" where price.hotel_id='"+hotel.getElongId()+"' " +
					" and price.price_plan_id="+order2.getRatePlanID()+" " +
					"and price.date>='"+order2.getArrival()+"' and price.date<'"+order2.getDeparture()+"' ";

			JSONArray PriceArray = new JSONArray();
			List<EbookingPrice> priceList= new EbookingPrice().dao().find(sqlPara);

			int pi=0;
			BigDecimal TotalCost= BigDecimal.valueOf(0);
			for(EbookingPrice price:priceList){
				JSONObject priceObject = new JSONObject();
				priceObject.put("Date",price.getDate());

//				TotalCost= TotalCost.add(price.getPrice().multiply(new BigDecimal(quantity)));
				TotalCost= TotalCost.add(price.getCost().multiply(new BigDecimal(quantity)));
//				TotalCost= TotalCost.add(price.getCost());

//				priceObject.put("Price",price.getPrice().multiply(new BigDecimal(quantity)));
				priceObject.put("Price",price.getPrice());
				PriceArray.add(priceObject);

				data.put("pricesDetails["+pi+"][Date]",price.getDate());
//				data.put("pricesDetails["+pi+"][Price]",price.getPrice().multiply(new BigDecimal(quantity)));
				data.put("pricesDetails["+pi+"][Price]",price.getPrice());

				pi=pi+1;
			}

			String dayPrice=PriceArray.toJSONString();

//			data.put("pricesDetails",PriceArray);

			/**** 更新一下数据库******/
			order2.setDayprice(dayPrice);
			JSONObject cancelShow=new JSONObject();
			cancelShow.put("advance_day",plan.getAdvanceDay());
			cancelShow.put("specific_date",plan.getSpecificDate());
			cancelShow.put("cut_type",plan.getCutType());
			cancelShow.put("amount",plan.getAmount());
			cancelShow.put("is_cancel",plan.getIsCancel());

			String cancelShowTxt=cancelShow.toJSONString();
			order2.setCancelshow(cancelShowTxt);

			order2.setTotalCost(TotalCost);
			order2.update();

//			data.put("cancelShow",cancelShow);

			data.put("cancelShow[advance_day]",plan.getAdvanceDay());
			data.put("cancelShow[specific_date]",plan.getSpecificDate());
			data.put("cancelShow[cut_type]",plan.getCutType());
			data.put("cancelShow[amount]",plan.getAmount());
			data.put("cancelShow[is_cancel]",plan.getIsCancel());



			data.put("TotalMoney",String.valueOf(order2.getTotalPrice()));
			data.put("TotalCost",TotalCost);
			data.put("guestname",order2.getGuests());
			data.put("mobile",order2.getPersonMobile());
			data.put("roomNum",String.valueOf(order2.getQuantity()));
			data.put("tm1",order2.getArrival());
			data.put("tm2",order2.getDeparture());
			data.put("latetime",order2.getEarlyArrivalTime());
			data.put("breakfastCount",String.valueOf(plan.getBreakfast()));
			data.put("create_time",String.valueOf(order2.getCreated()));
			data.put("status",order2.getEstatus());
			data.put("city_id",hotel.getCityId());
			data.put("city_name",hotel.getCityName());
			data.put("is_cancelable",String.valueOf(plan.getIsCancel()));
			data.put("cancel_time","");



		Map<String, String> headers = new HashMap<String, String>(16);
//		headers.put("Content-Type", "multipart/form-data");
			headers.put("Content-Type", "application/x-www-form-urlencoded");

//		String ret = HttpKit.post("http://127.0.0.1/shuia/index.php?m=gaode&a=getGetOrder", null, CommonUtils.asUrlParams(data),headers);
		String qstr=	CommonUtils.asUrlParams(data);
//			qstr=qstr+"&pricesDetails="+dayPrice+"&cancelShow="+cancelShowTxt  ;
			log.warn("qstr_canshu_"+qstr);


//			Jedis jedis = RedisUtil.getConn();
//			String key="pushxdjorder"+order2.getId();
//			String retjedis=  jedis.set(key, qstr);
//			jedis.expire(key, 864000);//秒
//			jedis.close();
			long currentTimeMillis = System.currentTimeMillis();
			try {
				//这个是更新下订单的用户id
				String ret2 = HttpKit.post("https://api.tetuijiudian.com/shuia/index.php?m=gaode&a=addOrderSync", null, qstr,headers);
//            String ret2 = HttpKit.post("http://api.tetuijiudian.com/shuia/index.php?m=gaode&a=addOrderSync", null, qstr,headers);
//		String ret = HttpKit.post("http://call.quchuchai.cn/Gaode/addOrder", null, dataMap.toString(),headers);

				log.warn("ret2_jd2_"+ret2);


				 currentTimeMillis = System.currentTimeMillis();

				String ret = HttpKit.post("http://call.quchuchai.cn/Gaode/addOrder", null, qstr,headers);
//		String ret = HttpKit.post("http://call.quchuchai.cn/Gaode/addOrder", null, dataMap.toString(),headers);

				log.warn("ret2_xdj_"+ret);

				if(!ret.contains("\"success\":1")){
					for (int i = 3; i >= 0; i--) {
						try {

							Thread.sleep(60000); // 休眠1秒
							ret = HttpKit.post("http://call.quchuchai.cn/Gaode/addOrder", null, qstr,headers);
//		String ret = HttpKit.post("http://call.quchuchai.cn/Gaode/addOrder", null, dataMap.toString(),headers);

							log.info("ret"+i+ret);

							if(ret.contains("\"success\":1")){
								break;
							}


						} catch (InterruptedException e) {

							e.printStackTrace();

						}
					}
				}

			}catch (Exception e) {


				log.error(" AmapOrderPushThread exception:", e);

				long specifiedTimeMillis  = System.currentTimeMillis();
				long timeDifferenceMillis = specifiedTimeMillis - currentTimeMillis;
				String sendtxt = "订单:"+order2.getId()+"推送失败。耗时："+timeDifferenceMillis+"毫秒";
				HttpKit.get("http://api.tetuijiudian.com/shuia/index.php?g=content&m=exedlt&a=logincheck&txt="+sendtxt);
			}









		//{"success":1,"msg":"同步成功","data":{"order_id":"10282"}}


		}catch (Exception e){
//			log.error(e);
			log.error(" AmapOrderPushThread exception:", e);

		}

	}
	
}
