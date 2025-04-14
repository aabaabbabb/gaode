package com.jinyan.controller;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;
import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.aop.Inject;
import com.jfinal.core.Path;
import com.jinyan.common.GdlogInterceptor;
import com.jinyan.controller.base.BaseController;
import com.jinyan.model.Order;
import com.jinyan.service.HotelOrderService;
import com.jinyan.service.HotelService;
import com.jinyan.service.ReissueInvoiceService;
import com.jinyan.utils.ResponseUtils;
import com.jinyan.utils.ResponseUtilsOutResponse;

// 这个类 用来处理，订单

@Clear
@Before(GdlogInterceptor.class)
@Path(value = "/hotel/orderxdj", viewPath = "")
public class HotelOrderXdjController extends BaseController {

	@Inject
	private HotelService hsrv;

	@Inject
	private HotelOrderService orderService;
	
	@Inject
	private ReissueInvoiceService rissrv;

	public static Map<String, String> headerMap = new HashMap<String, String>();

	static {
		headerMap.put("Content-Type", "application/x-www-form-urlencoded");
	}



	public void orderXdjChange() {

//		orderService.orderChanged("10000");

		ResponseUtils res = new ResponseUtils();
		try {
			String id = getPara("id");
			String estatus = getPara("estatus");


			orderService.orderNoticexdjLog(id,estatus);

			Boolean ret= orderService.orderXdjChanged(id,estatus);

			if(Strings.isNullOrEmpty(id) || Strings.isNullOrEmpty(estatus)){
				res.setCode("30000");
				res.setMsg("参数不对");
				ResponseUtilsOutResponse resOut = new ResponseUtilsOutResponse();
				resOut.setResponse(res);
				renderJson(resOut);
				return;
			}


			if(ret){
				res.setCode("10000");
				res.setMsg("success");

			}else{
				res.setCode("20000");
				res.setMsg("err 更新失败");

			}




		} catch (Exception e) {
			e.printStackTrace();
			JSONObject data = new JSONObject();
			data.put("ReturnCode", 999);
			data.put("ReturnDescript", "异常");

			res.setCode("10000");
			res.setMsg("success");

			res.setData(data);
		}

		ResponseUtilsOutResponse resOut = new ResponseUtilsOutResponse();
		resOut.setResponse(res);
		renderJson(resOut);
		return;
	}


	public void testPushXdjOrder(){
		//提交信息到小店家
		String id = getPara("id");

		AmapOrderPushThread2 thread=new AmapOrderPushThread2(id);
		thread.start();
	}





}

//处理一些订单的通知
class AmapOrderPushThread2 extends Thread {

	private String id;

	public AmapOrderPushThread2(String id) {
		this.id = id;
	}

	public void run() {

		Order order=new Order();
		Order order2=order.findFirst("select * from gd_order where id='"+this.id+"' ");
		if(order2==null){

			return;
		}


		Integer quantity=order2.getQuantity();


		try{

//			EbookingRoomType roomDao = new EbookingRoomType().dao().findById(order2.getRoomID());
//			EbookingPricePlan plan = new EbookingPricePlan().dao().findById(order2.getRatePlanID());
//
//
//			Map<String, Object> data  = new HashMap<String, Object>();
//			data.put("order_num", this.id);
//
//
//			HotelService hsrv=new HotelService();
//
//			Hotel hotel = hsrv.getHotelInfo(order2.getHotelID()); // 这里要是 长整型，高德id 长整型，如果用整形的经常报错
//
//
//
//			Integer ElongId = hotel.getElongId();
//
//			data.put("HotelID",String.valueOf(ElongId) );
//			data.put("hotelname",hotel.getHotelName());
//			data.put("roomType_id",order2.getRoomID());
//
//
//
//			data.put("roomType_id",order2.getRoomID());
//			data.put("roomName",roomDao.getName());
//			data.put("ratePlan_id",String.valueOf(plan.getId()));
//			data.put("planName",plan.getName());
//
//			//开始写 pricesDetails
//
//			String sqlPara = "select price.*" +
//					" from gd_ebooking_price as price " +
//					" where price.hotel_id='"+hotel.getElongId()+"' " +
//					" and price.price_plan_id="+order2.getRatePlanID()+" " +
//					"and price.date>='"+order2.getArrival()+"' and price.date<'"+order2.getDeparture()+"' ";
//
//			JSONArray PriceArray = new JSONArray();
//			List<EbookingPrice> priceList= new EbookingPrice().dao().find(sqlPara);
//
//			int pi=0;
//			for(EbookingPrice price:priceList){
//				JSONObject priceObject = new JSONObject();
//				priceObject.put("Date",price.getDate());
//				priceObject.put("Price",price.getPrice().multiply(new BigDecimal(quantity)));
//				PriceArray.add(priceObject);
//
//				data.put("pricesDetails["+pi+"][Date]",price.getDate());
//				data.put("pricesDetails["+pi+"][Price]",price.getPrice().multiply(new BigDecimal(quantity)));
//
//				pi=pi+1;
//			}
//
//			String dayPrice=PriceArray.toJSONString();
//
////			data.put("pricesDetails",PriceArray);
//
//			/**** 更新一下数据库******/
//			order2.setDayprice(dayPrice);
//			JSONObject cancelShow=new JSONObject();
//			cancelShow.put("advance_day",plan.getAdvanceDay());
//			cancelShow.put("specific_date",plan.getSpecificDate());
//			cancelShow.put("cut_type",plan.getCutType());
//			cancelShow.put("amount",plan.getAmount());
//			cancelShow.put("is_cancel",plan.getIsCancel());
//
//			String cancelShowTxt=cancelShow.toJSONString();
//			order2.setCancelshow(cancelShowTxt);
//			order2.update();
//
////			data.put("cancelShow",cancelShow);
//
//			data.put("cancelShow[advance_day]",plan.getAdvanceDay());
//			data.put("cancelShow[specific_date]",plan.getSpecificDate());
//			data.put("cancelShow[cut_type]",plan.getCutType());
//			data.put("cancelShow[amount]",plan.getAmount());
//			data.put("cancelShow[is_cancel]",plan.getIsCancel());
//
//
//
//			data.put("TotalMoney",String.valueOf(order2.getTotalPrice()));
//			data.put("TotalCost",String.valueOf(order2.getOrderRealPrice()));
//			data.put("guestname",order2.getGuests());
//			data.put("mobile",order2.getPersonMobile());
//			data.put("roomNum",String.valueOf(order2.getQuantity()));
//			data.put("tm1",order2.getArrival());
//			data.put("tm2",order2.getDeparture());
//			data.put("latetime",order2.getEarlyArrivalTime());
//			data.put("breakfastCount",String.valueOf(plan.getBreakfast()));
//			data.put("create_time",String.valueOf(order2.getCreated()));
//			data.put("status",order2.getEstatus());
//			data.put("city_id",hotel.getCityId());
//			data.put("city_name",hotel.getCityName());
//			data.put("is_cancelable",String.valueOf(plan.getIsCancel()));
//			data.put("cancel_time","");
//
//
//
//			Map<String, String> headers = new HashMap<String, String>(16);
////		headers.put("Content-Type", "multipart/form-data");
//			headers.put("Content-Type", "application/x-www-form-urlencoded");
//
////		String ret = HttpKit.post("http://127.0.0.1/shuia/index.php?m=gaode&a=getGetOrder", null, CommonUtils.asUrlParams(data),headers);
//			String qstr=	CommonUtils.asUrlParams(data);
////			qstr=qstr+"&pricesDetails="+dayPrice+"&cancelShow="+cancelShowTxt  ;
//
//
//			//这个是更新下订单的用户id
////			String ret2 = HttpKit.post("http://api.tetuijiudian.com/shuia/index.php?m=gaode&a=addOrderSync", null, qstr,headers);
////            String ret2 = HttpKit.post("http://api.tetuijiudian.com/shuia/index.php?m=gaode&a=addOrderSync", null, qstr,headers);
////		String ret = HttpKit.post("http://call.quchuchai.cn/Gaode/addOrder", null, dataMap.toString(),headers);
//
//
//
//
//			String ret = HttpKit.post("http://call.quchuchai.cn/Gaode/addOrder", null, qstr,headers);
////		String ret = HttpKit.post("http://call.quchuchai.cn/Gaode/addOrder", null, dataMap.toString(),headers);
//
////			log.info("ret"+ret);
//
//			System.out.println(ret);
		}catch (Exception e){
//			log.error(e);


		}

	}
}
