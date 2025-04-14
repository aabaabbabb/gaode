package com.jinyan.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.aop.Inject;
import com.jfinal.core.Path;
import com.jfinal.kit.HttpKit;
import com.jfinal.plugin.activerecord.Record;
import com.jinyan.common.GdlogInterceptor;
import com.jinyan.controller.base.BaseController;
import com.jinyan.model.ReissueInvoice;
import com.jinyan.service.HotelOrderService;
import com.jinyan.service.HotelService;
import com.jinyan.service.ReissueInvoiceService;
import com.jinyan.utils.ResponseUtils;
import com.jinyan.utils.ResponseUtilsOutResponse;

// 这个类 用来处理，订单

@Clear
@Before(GdlogInterceptor.class)
@Path(value = "/hotel/order", viewPath = "")
public class HotelOrderController extends BaseController {

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
	
	public static void main(String[] args) {
		String CpOrderID="100210397";
		Thread t2=new Thread(()->{
			HttpKit.get("http://api.xiaodianjia.cn/gdapi/create/order?gdid="+CpOrderID);
		}) ;
		//给李铁用
		t2.start();

	}

	/***
	 * 高德调用酒店下单接口 【高德已连通】 biz_content
	 *
	 * 高德开发文档 https://y.amap.com/docs/hotel/order_submit
	 */
//	@ActionKey("/hotel/order/submit")
	public void submit() {
		
		try {
			String biz_content = getPara("biz_content");
			String CpOrderID = orderService.createOrder(biz_content);

//			Thread t2=new Thread(()->{
//				HttpKit.get("http://api.xiaodianjia.cn/gdapi/create/order?gdid="+CpOrderID);
//				System.out.println("推送小店家"+CpOrderID);
//			}) ;
//			//给李铁用
//			t2.start();

			ResponseUtils res = new ResponseUtils();

			JSONObject data = new JSONObject();
			res.setCode("10000");
			res.setMsg("success");
			data.put("CpOrderID", CpOrderID);
			data.put("ReturnCode", 0);
			data.put("ReturnDescript", "预订成功");
			res.setData(data);

			ResponseUtilsOutResponse resOut = new ResponseUtilsOutResponse();
			resOut.setResponse(res);
			renderJson(resOut);
			return;

		} catch (Exception e) {
			e.printStackTrace();
			ResponseUtils res = new ResponseUtils();
			res.setCode("20000");
			res.setMsg("服务不可用,checkPrice fail");
			res.setSub_code("isp.unknow-error");

			res.setSub_msg("服务暂不可用（业务系统不可用）:");

			ResponseUtilsOutResponse resOut = new ResponseUtilsOutResponse();
			resOut.setResponse(res);
			renderJson(resOut);
			return;
		}

	}

	/***
	 * 高德调用酒店订单取消 【高德已连通】 biz_content
	 *
	 * 高德开发文档 https://y.amap.com/docs/hotel/order_cancel
	 */
	public void cancel() {
		ResponseUtils res = new ResponseUtils();
		try {
			String biz_content = getPara("biz_content");
			Boolean cancled = orderService.cancleOrder(biz_content);
           
			if (cancled) {

				JSONObject data = new JSONObject();
				data.put("ReturnCode", 0);
				data.put("ReturnDescript", "提交取消申请成功");

				res.setCode("10000");
				res.setMsg("success");

				res.setData(data);

			} else {
				JSONObject data = new JSONObject();
				data.put("ReturnCode", 1001);
				data.put("ReturnDescript", "业务失败");

				res.setCode("10000");
				res.setMsg("success");

				res.setData(data);
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

	/***
	 * 高德查询酒店订单详情 【高德已连通】 biz_content
	 *
	 * 高德开发文档 https://y.amap.com/docs/hotel/order_getinfo
	 */
	public void getInfo() {
		ResponseUtils res = new ResponseUtils();
		try {
			String biz_content = getPara("biz_content");
			JSONObject jsonData = orderService.getInfo(biz_content);

			res.setCode("10000");
			res.setMsg("success");

			res.setData(jsonData);

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

	/***
	 * 高德同步增量变化订单 【高德已连通】 biz_content
	 *
	 * 高德开发文档 https://y.amap.com/docs/hotel/order_getchanged
	 */
	public void getChangedList() {

//		orderService.orderChanged("10000");

		ResponseUtils res = new ResponseUtils();
		try {
			String biz_content = getPara("biz_content");
			JSONObject jsonObject = JSONObject.parseObject(biz_content);
			String StartTime = jsonObject.getString("StartTime");
			String EndTime = jsonObject.getString("EndTime");

			List<Record> records = orderService.getOrderChangedList(StartTime, EndTime);

			JSONObject OrderItem = new JSONObject();
			JSONArray OrderItemList = new JSONArray();
			for (Record record : records) {

				JSONObject o = new JSONObject();
				o.put("AmapOrderID", record.getStr("AmapOrderID"));
				o.put("CpOrderID", record.getStr("CpOrderID"));

				OrderItemList.add(o);
			}
			OrderItem.put("OrderItemList", OrderItemList);

			res.setCode("10000");
			res.setMsg("success");

			res.setData(OrderItem);

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

	public void orderChanged() {

//		orderService.orderChanged("10000");

		ResponseUtils res = new ResponseUtils();
		try {
			String id = getPara("id");
			Boolean ret= orderService.orderChanged(id);

			if(ret){
				res.setCode("10000");
				res.setMsg("success");

			}else{
				res.setCode("20000");
				res.setMsg("err 订单增量添加失败");

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



	public  void reissueInvoice() {
		String dataMap = getRequest().getParameter("biz_content");
		JSONObject req_json = JSONObject.parseObject(dataMap);
		ReissueInvoice res=new ReissueInvoice();
		res.setAmapOrderID(req_json.getString("AmapOrderID"));
		res.setCpOrderID(req_json.getString("CpOrderID"));

		JSONObject req_jsonInvoice=req_json.getJSONObject("Invoice");

		res.setInvoiceType(req_jsonInvoice.getInteger("InvoiceType"));
		res.setInvoiceTitleType(req_jsonInvoice.getInteger("InvoiceTitleType"));
		res.setTitle(req_jsonInvoice.getString("Title"));
		res.setTaxpayerNumber(req_jsonInvoice.getString("TaxpayerNumber"));
		res.setInvoiceAmount(req_jsonInvoice.getDouble("InvoiceAmount"));
		res.setBody(req_jsonInvoice.getString("Body"));
		res.setInvoiceRemark(req_jsonInvoice.getString("InvoiceRemark"));
		res.setPostPayType(req_jsonInvoice.getString("PostPayType"));

		JSONObject req_jsonContactInfo=req_jsonInvoice.getJSONObject("ContactInfo");

		if(req_jsonContactInfo!=null) {
			res.setPresonName(req_jsonContactInfo.getString("PresonName"));
			res.setPhoneNumber(req_jsonContactInfo.getString("PhoneNumber"));
			res.setEmail(req_jsonContactInfo.getString("Email"));
		}

		JSONObject req_jsonPostAddress=req_jsonInvoice.getJSONObject("PostAddress");

		if(req_jsonPostAddress!=null) {
			res.setProvince(req_jsonPostAddress.getString("Province"));
			res.setCity(req_jsonPostAddress.getString("City"));
			res.setDistrict(req_jsonPostAddress.getString("District"));
			res.setDetail(req_jsonPostAddress.getString("Detail"));
		}

		JSONObject req_jsonCompanyInfo=req_jsonInvoice.getJSONObject("CompanyInfo");

		if(req_jsonCompanyInfo!=null){
			res.setConpanyName(req_jsonCompanyInfo.getString("ConpanyName"));
			res.setCompanyAddress(req_jsonCompanyInfo.getString("CompanyAddress"));
			res.setCompanyPhone(req_jsonCompanyInfo.getString("CompanyPhone"));
			res.setCompanyBankName(req_jsonCompanyInfo.getString("CompanyBankName"));
			res.setCompanyBankAccount(req_jsonCompanyInfo.getString("CompanyBankAccount"));

		}

		boolean bool=rissrv.add(res);
		ResponseUtils resutils = new ResponseUtils();
		if(bool) {
			resutils.setCode("10000");
			resutils.setMsg("success");
			Map<String, Object> data=new HashMap<String, Object>();
			data.put("ReturnCode", 0);
			data.put("ReturnDescript", "申请开票成功");
			
//			resutils.setData(JsonKit.toJson(data));
			resutils.setData(data);
			ResponseUtilsOutResponse resOut = new ResponseUtilsOutResponse();
			resOut.setResponse(resutils);
			renderJson(resOut);
		}
	}


}
