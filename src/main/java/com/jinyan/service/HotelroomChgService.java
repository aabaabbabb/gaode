package com.jinyan.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.beust.jcommander.Strings;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.ehcache.CacheKit;
import com.jinyan.model.Hotel;
import com.jinyan.model.HotelroomChg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class HotelroomChgService {
	
	private HotelroomChg hotelDao = new HotelroomChg().dao();
	
	public Page<HotelroomChg> paginate(int pageNumber, int pageSize) {
		return hotelDao.paginate(pageNumber, pageSize, "select * ", "from gd_hotel  order by id desc");
	}
	
	public List<HotelroomChg> getHotelroomChgList(int currentPage, int pageSize){
		String sqlPara="select id from gd_hotelroom_chg limit "+(currentPage-1)*pageSize+","+pageSize;

		return hotelDao.find(sqlPara);
	}
	public boolean  HotelroomChanged(Long id){

		String sql= "select * from gd_order where  id="+id+" ";



		HotelroomChg roomchg=hotelDao.findFirst(sql);
		if(roomchg!=null){
			return true;
		}

		roomchg=new HotelroomChg();
		roomchg.setId(id);
		roomchg.update();

		return true;
	}


	public static void main(String[] args) {
		Map<String, String> headerMap=new HashMap<String, String>();
		headerMap.put("Content-Type", "application/x-www-form-urlencode");
		Map<String, Object> dataMap=new HashMap<String, Object>();
		
		JSONObject bizContent = new JSONObject();
		bizContent.put("HotelID", "10109");
		bizContent.put("CtripHotelID", "");
		bizContent.put("HotelName", "北京大厦");
		bizContent.put("BrandCode", "品牌（携程品牌code）");
		bizContent.put("Brandname", "");
		bizContent.put("City", "北京");
		bizContent.put("Location", "酒店描述");
		bizContent.put("Address", "北京广安门外大街1号");
		bizContent.put("HotelDesc", "");
		bizContent.put("Brief", "酒店特色简介");
		bizContent.put("GDLat", "");
		bizContent.put("GDLon", "");
		bizContent.put("Lat", "");
		bizContent.put("Lon", "");
		bizContent.put("OpenYear", "");
		bizContent.put("FitmentYear", "");
		bizContent.put("TagId", "");
		bizContent.put("Tagname", "");
		bizContent.put("HotelType", 2);
		bizContent.put("HotelStar", 5);
		bizContent.put("HotelCategory", 1);
		bizContent.put("ScoreTotal", 70);
		
		bizContent.put("ScoreFacility", 60);
		bizContent.put("ScoreClean", 70);
		bizContent.put("ScoreService", 65);
		bizContent.put("ScoreEnvi", 86);
		bizContent.put("Telephone", "0104444488");
		
		JSONArray fa_list=new JSONArray();
		
		JSONObject fa_obj=new JSONObject();
		fa_obj.put("Category", "1");
		fa_obj.put("CategoryName", "服务设施");
		fa_obj.put("FacilityName", "中餐厅");
		fa_obj.put("FacilityCode", "1");
		fa_list.add(fa_obj);
		bizContent.put("Facilities", fa_list);
		
		
		bizContent.put("Pictures", "");
		JSONArray pic_list=new JSONArray();
		JSONObject pic_obj=new JSONObject();
		pic_obj.put("Iscover", 1);
		pic_obj.put("Pic_id", 1);
		pic_obj.put("Url", "");
		pic_obj.put("Title", "");
		
		bizContent.put("", "");
		bizContent.put("", "");
		bizContent.put("", "");
		
		
		dataMap.put("biz_content", "");
		dataMap.put("method", "amap.hotel.offline.pushPoiInfoList");
		dataMap.put("utc_timestamp", System.currentTimeMillis());
		//dataMap.put("sign", );
		dataMap.put("app_id", "202307180197047735");
		dataMap.put("version","1.0" );
		dataMap.put("sign_type", "RSA2");
		dataMap.put("charset","UTF-8");
	}
}
