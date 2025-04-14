package com.jinyan.controller;

import java.io.IOException;
import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Clear;
import com.jfinal.core.Controller;
import com.jfinal.core.Path;
import com.jfinal.kit.HttpKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jinyan.service.HotelService;
import com.jinyan.utils.RedisUtil;

import redis.clients.jedis.Jedis;

@Path(value = "/admin", viewPath = "/admin/index")
public class IndexController extends Controller{

	@Clear
	public void index() {
		try {
			System.out.println("到这里了");
			String jedis_key="123";
			Jedis jedis = RedisUtil.getConn();
			jedis.set(jedis_key,"http://gd2.tetuijiudian.com/amapapi/hotel/pushRoomInfo?elongId=17004094");
			jedis.expire(jedis_key, 86400*20);
			jedis.close();
			renderText("成功了！"+jedis);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			renderText(e.getMessage());
		}
		renderText("页面");
		
	}
	
	HotelService hs=new HotelService();
	@Clear
	public void push() {
//		String url="http://gd2.tetuijiudian.com/amapapi/hotel/pushRoomInfo?elongId=17004094";
//		System.out.println(url);
//		String result=HttpKit.get(url);
//		System.out.println(result);
		
//		String filePath = "D:\\hotel.txt";
//        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
//            String line;
//            while ((line = reader.readLine()) != null) {
//                // 处理每一行文本
//            String sql="select elongIdStr from gd_hotel where down=0 and dltel='15847329289' and HotelName='"+line+"'";
//            Record record=Db.findFirst(sql);
//            if(record!=null) {
//			String url="http://localhost:8552/hotel/pushRoomInfo?elongId="+record.getStr("elongIdStr");
//			System.out.println(url);
//            String result=HttpKit.get(url);
//			System.out.println(result);
//			Thread.sleep(10000);
//                // 在这里添加你的处理逻辑
//            }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
        
		
	    String sql="select elongIdStr from gd_hotel where down=0";
		//String sql="SELECT * from gd_hotel where  ElongId='93381325'";
	    List<Record> list=Db.find(sql);
	    for (Record record : list) {
	    	try {
				String url="http://localhost:8552/hotel/pushRoomInfo?elongId="+record.getStr("elongIdStr");
				System.out.println(url);
				String result=HttpKit.get(url);
				System.out.println(result);

			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	}
	
	public static void main(String[] args) throws IOException {
		String address="逸庄酒店(宜宾李庄古镇店)";
		 String url = "https://restapi.amap.com/v3/geocode/geo?key=3a0f30185a4911a6e129033821dff84c&address=" + address;
		String result= HttpKit.get(url);
		JSONObject res_json=JSONObject.parseObject(result);
		if(res_json!=null) {
			String status=res_json.getString("status");
			if(status.equals("1")) {
				JSONArray geocodes=res_json.getJSONArray("geocodes");
				if(geocodes.size()==1) {
					JSONObject geocode=geocodes.getJSONObject(0);
					if(geocode!=null) {
						String location=geocode.getString("location");
						System.out.println(location);
						String[] lat_lon=location.split(",");
					}
				}
			}
		}
		System.out.println(result);
		
//        String address = "北京市朝阳区";
//        address = URLEncoder.encode(address, "UTF-8"); // 对地址进行编码
// 
//        String url = "https://restapi.amap.com/v3/geocode/geo?key=YOUR_KEY&address=" + address;
// 
//        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
//        connection.setRequestMethod("GET");
// 
//        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//        String line;
//        StringBuilder responseBuilder = new StringBuilder();
//        while ((line = reader.readLine()) != null) {
//            responseBuilder.append(line);
//        }
//        reader.close();
// 
//        // 解析返回的JSON数据，提取经纬度
//        // ...
// 
//        connection.disconnect();
    }
	
	
}
