package com.jinyan.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.beust.jcommander.StringKey;
import com.beust.jcommander.Strings;
import com.jfinal.kit.HttpKit;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.*;
import com.jfinal.plugin.ehcache.CacheKit;
import com.jinyan.kit.CoordinateConverterKit;
import com.jinyan.model.EbookingRoomType;
import com.jinyan.model.Hotel;
import com.jinyan.utils.ControllerUtils;
import com.jinyan.utils.StringUtils;

public class HotelService {

	private Hotel hotelDao = new Hotel().dao();

	public Page<Hotel> paginate(int pageNumber, int pageSize) {
		return hotelDao.paginate(pageNumber, pageSize, "select * ", "from gd_hotel  order by id desc");
	}

	public List<Hotel> getHotelList(int currentPage, int pageSize) {
		String sqlPara = "select id from gd_hotel limit " + (currentPage - 1) * pageSize + "," + pageSize;

		return hotelDao.find(sqlPara);
	}

	public List<Map<String, Object>> getHotelListBy(JSONArray arr_json) {
		List<Map<String, Object>> hotelList = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < arr_json.size(); i++) {
			JSONObject json_obj = arr_json.getJSONObject(i);
			String sql = "select * from gd_hotel where id=?";
			Hotel hotel = hotelDao.findFirst(sql, json_obj);
			Map<String, Object> hotelMap = new HashMap<String, Object>();
			hotelMap.put("HotelID", hotel.getId());
			hotelMap.put("HotelName", hotel.getHotelName());
			hotelMap.put("City", hotel.getCityId());
			hotelMap.put("Address", hotel.getAddress());

			hotelMap.put("GDLat", hotel.getGaodeLat());
			hotelMap.put("GDLon", hotel.getGaodeLon());
			hotelMap.put("HotelType", hotel.getHotelType());
			hotelMap.put("Telephone", hotel.getPhone());
			hotelList.add(hotelMap);
		}

		return hotelList;
	}

	public List<Record> getHotelListById(List<String> ids) {

		String idStr = Strings.join(",", ids);
		String sql = "select * from gd_hotel where id in (" + idStr + ")";
		List<Record> h = Db.find(sql);

		return h;
	}

	public Hotel getHotelInfo(Long hid) {
		String key = "getHotelInfobyid" + hid;
		Hotel h = CacheKit.get("getHotelInfo/findById", key);
		if (h == null) {
			h = hotelDao.findById(hid);
			// String sql="select * from gd_hotel where ElongIdStr=?";
			// h=hotelDao.findFirst(sql,hid);
			CacheKit.put("getHotelInfo/findById", key, h);
		}

		return h;
	}

	public Hotel getHotelInfo(Long hid, String elongIdStr) {
		String key = "getHotelInfobyid" + hid;
		Hotel h = CacheKit.get("getHotelInfo/findById", key);
		if (h == null) {
			h = hotelDao.findById(hid);
			if (h == null) {
				String sql = "select * from gd_hotel where ElongIdStr=?";
				h = hotelDao.findFirst(sql, elongIdStr);
			}
			if (h != null) {
				CacheKit.put("getHotelInfo/findById", key, h);
			}
		}

		return h;
	}
	
	public Hotel getHotelInfoNo(Long hid, String elongIdStr) {
		Hotel h = hotelDao.findById(hid);
		if(h==null) {
			String sql = "select * from gd_hotel where ElongIdStr=?";
			h = hotelDao.findFirst(sql, elongIdStr);
		}
		return h;
	}
	

	public Hotel getHotelByElongIdStr(String elongIdStr) {
		String sql = "select * from gd_hotel where ElongIdStr=?";
		return hotelDao.findFirst(sql, elongIdStr);
	}

	public Hotel addHotel(String elongIdStr) {
		Hotel hotel = this.getHotelByElongIdStr(elongIdStr);
		if (hotel == null) {
			String url = "http://www2.api.watu.cn:895/e/hotel.static.info.php?eid=" + elongIdStr;
			System.out.println(url);
			String result_json = HttpKit.get(url);
			if (StrKit.notBlank(result_json)) {
				JSONObject res_json = JSONObject.parseObject(result_json);
				if (res_json != null) {
					hotel = new Hotel();
					Long id = this.getGaoDeId();
					hotel.setId(id);
					hotel.setTailnum(0);
					hotel.setElongId(Integer.parseInt(elongIdStr));
					hotel.setElongIdStr(elongIdStr);
					JSONObject result = res_json.getJSONObject("Result");
					JSONObject detail = result.getJSONObject("Detail");
					hotel.setHotelName(detail.getString("HotelName"));
					hotel.setHotelStatus(detail.getInteger("HotelStatus"));
					hotel.setStarRate(detail.getInteger("StarRate"));
					hotel.setCategory(detail.getInteger("Category"));
					hotel.setPhone(detail.getString("Phone"));
					hotel.setBrandName(detail.getString("BrandName"));
					String BaiduLat = detail.getString("BaiduLat");
					String BaiduLon = detail.getString("BaiduLon");


						List<String> lat_lon = CoordinateConverterKit.getCoordinate(Double.parseDouble(BaiduLon),
								Double.parseDouble(BaiduLat));
						hotel.setGaodeLat(lat_lon.get(1));
						hotel.setGaodeLon(lat_lon.get(0));
					
					hotel.setBaiduLat(BaiduLat);
					hotel.setBaiduLon(BaiduLon);
					hotel.setCityId(detail.getString("CityId"));
					hotel.setCityName(detail.getString("CityName"));
					hotel.setRoomTotalAmount(detail.getInteger("RoomTotalAmount"));
					hotel.setAddress(detail.getString("Address"));
					hotel.setDown(0);
					hotel.setNoroom(0);
					hotel.setCommissionRate(BigDecimal.valueOf(0));
					hotel.setInvoiceMode(0);
					hotel.setSource(0);
					hotel.setCreated(new Date());

					String url_xdj_dl = "http://call.quchuchai.cn/Gaode/getEmployInfo?HotelID=" + elongIdStr;
					String result_dl = HttpKit.post(url_xdj_dl, null);
					JSONObject res_json_dl = JSONObject.parseObject(result_dl);

					JSONObject data = res_json_dl.getJSONObject("data");
					if (data != null) {
						hotel.setDlname(data.getString("name"));
						hotel.setDltel(data.getString("mobile"));
					}
					if (hotel.save())
						this.addRoomType(res_json, elongIdStr);
				}
			}
		}
		return hotel;
	}

	public void addRoomType(JSONObject res_json, String elongIdStr) {
		JSONObject result = res_json.getJSONObject("Result");
		JSONArray Rooms = result.getJSONArray("Rooms");
		for (int i = 0; i < Rooms.size(); i++) {
			JSONObject room = Rooms.getJSONObject(i);
			EbookingRoomType ekrt = new EbookingRoomType();
			ekrt.setElongHotelId(elongIdStr);
			ekrt.setName(room.getString("RoomName"));
			ekrt.setBed(room.getString("BedType"));
			ekrt.setBedType(0);
			ekrt.setBroadband("2");
			ekrt.setTotalCount(room.getInteger("Amount"));
			ekrt.setIsDel(false);
			ekrt.setCreateTime(new Date());
			ekrt.setUpdateTime(new Date());
			if(!room.getString("Floor").equals("")) {
				ekrt.setFloor(room.getString("Floor").replaceAll("层", ""));
			}
			ekrt.setArea(room.getString("area"));
			ekrt.setPerson(2);
			ekrt.setIsWindow("2");
			ekrt.setElongRoomTypeId(room.getString("RoomID"));
			ekrt.setElongRoomid(room.getString("RoomID"));
			ekrt.save();
		}
	}

	public static void main(String[] args) {
		String url = "http://www2.api.watu.cn:895/e/hotel.static.info.php?eid=90008090";
		String result_str = HttpKit.get(url, null);
		JSONObject res_json = JSONObject.parseObject(result_str);
		JSONObject result = res_json.getJSONObject("Result");
		JSONArray Rooms = result.getJSONArray("Rooms");
		for (int i = 0; i < Rooms.size(); i++) {
			JSONObject room = Rooms.getJSONObject(i);
			System.out.println(room.get("Amount"));
		}
	}

	private Long getGaoDeId() {
		long currentTimeMillis = System.currentTimeMillis();
		// 使用 Random 来生成随机数
		Random random = new Random(currentTimeMillis);
		// 生成一个10位随机数
		int randomNumber = random.nextInt(1000000000);
		// 输出结果
		String sql = "select id from gd_hotel where id=?";
		Hotel ht = hotelDao.findFirst(sql, randomNumber);
		if (ht != null) {
			this.getGaoDeId();
		}
		return (long) randomNumber;
	}

	/**
	 * List<? extends Model>转为List<Map<String, Object>>， 转换成高德字段
	 *
	 * @param records
	 * @return
	 */
	public List<Map<String, Object>> recordsGaode(List<Record> records) {
		List<Map<String, Object>> maps = new ArrayList<>();
		for (Record record : records) {
			maps.add(recordToAmap(record));
		}
		return maps;
	}

	/**
	 * Map<String, Object> 转换成高德字段
	 *
	 * @param record
	 * @return
	 */
	public Map<String, Object> recordToAmap(Record record) {
		if (null == record) {
			return null;
		}
		String[] keys = record.getColumnNames();
		Map<String, Object> map = new HashMap<>();
		for (String key : keys) {
			Object value = record.get(key);

//避免输出null的json，过滤掉空值
			if (null != value) {
				if (key.equals("CityName")) {
					key = "City";
				}
				if (key.equals("id")) {
					key = "HotelID";
				}
				if (key.equals("Phone")) {
					key = "Telephone";
				}
				if (key.equals("BaiduLon")) {
					key = "Lon";
				}
				if (key.equals("BaiduLat")) {
					key = "Lat";
				}

				if (key.equals("GaodeLat")) {
					float num = Float.parseFloat((String) value);
					map.put("GDLat", (float) num);
				}
				if (key.equals("GaodeLon")) {
					float num = Float.parseFloat((String) value);
					map.put("GDLon", (float) num);
				}
				map.put(key, value);
			}

		}

		return map;
	}

	public static void main33(String[] args) {
		Map<String, String> headerMap = new HashMap<String, String>();
		headerMap.put("Content-Type", "application/x-www-form-urlencode");
		Map<String, Object> dataMap = new HashMap<String, Object>();

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

		JSONArray fa_list = new JSONArray();

		JSONObject fa_obj = new JSONObject();
		fa_obj.put("Category", "1");
		fa_obj.put("CategoryName", "服务设施");
		fa_obj.put("FacilityName", "中餐厅");
		fa_obj.put("FacilityCode", "1");
		fa_list.add(fa_obj);
		bizContent.put("Facilities", fa_list);

		bizContent.put("Pictures", "");
		JSONArray pic_list = new JSONArray();
		JSONObject pic_obj = new JSONObject();
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
		// dataMap.put("sign", );
		dataMap.put("app_id", "202307180197047735");
		dataMap.put("version", "1.0");
		dataMap.put("sign_type", "RSA2");
		dataMap.put("charset", "UTF-8");
	}

}
