package com.jinyan.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;
import com.jinyan.model.EbookingRoomType;
import com.jinyan.model.Hotel;
import com.jinyan.model.Pic;
import com.jinyan.utils.CommonUtils;
import com.jinyan.utils.RedisUtil;
import org.jsoup.helper.StringUtil;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
处理一些 roomtype 相关的业务，
 */

public class RoomTypeDbService {

 	private EbookingRoomType EbookingRoomTypeDao= new EbookingRoomType().dao();
 	private Pic PicDao= new Pic().dao();

	public List<EbookingRoomType> getEbookingRoomTypeList(String elongid){
		//status 还没起作有
//		String sqlPara="select id from gd_ebooking_room_type where status=1 and elong_hotel_id= '"+elongid+"'";
		String sqlPara="select * from gd_ebooking_room_type where  elong_hotel_id= '"+elongid+"'";

		return EbookingRoomTypeDao.find(sqlPara);
	}

	public List<Pic> getPicList(String roomid){
		//status 还没起作有
//		String sqlPara="select id from gd_ebooking_room_type where status=1 and elong_hotel_id= '"+elongid+"'";
		String sqlPara="select * from gd_pic where  roomid=  "+roomid+"";

		return PicDao.find(sqlPara);
	}

	public String BedConvertGaode(String bed){

		/*
		房型类型：
1-标准间2-双床房3-三人房4-单人间5-家庭房6-大床房7-四人间8-套房9-床位房10-公寓11-别墅12-房车13-帐篷14-小木屋15-工作室

    转换
		// bed
		//床型0：大床 1：双床 2：大/双床 3：三床 4：一单一双 5：单人床 6：上下铺 7：通铺 8：榻榻米 9：水床 10：圆床 11：拼床 99：未知

		 */

		String gaodeRoomTypeStd="1";

		if(bed.contains("大床")){
			gaodeRoomTypeStd="6";
		}
		if(bed.contains("多张")){
			gaodeRoomTypeStd="5";
		}
		if(bed.contains("双床")){
			gaodeRoomTypeStd="2";
		}

		return gaodeRoomTypeStd;
	}
	public String BedType(String bed){

		/*
		房型类型：
1-标准间2-双床房3-三人房4-单人间5-家庭房6-大床房7-四人间8-套房9-床位房10-公寓11-别墅12-房车13-帐篷14-小木屋15-工作室

    转换
		// bed
		//床型0：大床 1：双床 2：大/双床 3：三床 4：一单一双 5：单人床 6：上下铺 7：通铺 8：榻榻米 9：水床 10：圆床 11：拼床 99：未知

		 */

		String ChildBedType="bed";

		if(bed.contains("大床")){
			ChildBedType="大床";
		}
		if(bed.contains("双床")){
			ChildBedType="双人床";
		}
		if(bed.contains("圆床")){
			ChildBedType="圆床";
		}
		if(bed.contains("榻榻米")){
			ChildBedType="榻榻米";
		}
		if(bed.contains("多张床")){
			ChildBedType=bed;
		}

		return ChildBedType;
	}

	public int BedQuantityTotal(String bed){
		int Quantity=1;



		if(bed.contains("双床")){
			Quantity=2;
		}

		return Quantity;
	}

	public double BedWidth(String bed){
		double width=1.5;



		if(bed.contains("米")){

			String regex = "\\d+\\.\\d+";

			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(bed);

			boolean found = matcher.find();
			if (found) {
				String decimal = matcher.group();
				width = Double.parseDouble(decimal);

			} else {

			}

		}

		return width;
	}


	public float AreaConvertGaode(String area){

		float defA= 15;

		try{
			if(area.isEmpty()){
				return defA;
			}

			if(area.contains("-")){
				area=area.split("-")[0];
			}
			defA=Float.parseFloat(area);

			return defA;
		}catch (Exception e){
			return defA;
		}

	}

	public int WirelessBroadnet(String broadband){
		//宽带0无 ； 2免费 ； 3收费 ； 4 部分收费； 5部分有且收费 ； 6部分有且免费 ； 8未知
		//无线宽带（0没有，1全部房间有且收费，2全部房间有且免费，3部分房间有且收费，4部分房间有且免费）
 		//有线宽带（0没有，1全部房间有且收费，2全部房间有且免费，3部分房间有且收费，4部分房间有且免费）
 		int def=0;

 		if(broadband.equals("2")){
			def=2;
		}
		if(broadband.equals("3")){
			def=1;
		}

 		return def;

	}
	public String getChildBedType(int ChildBedType){
		//1-双人床2-圆床3-水床4-榻榻米5-炕6-沙发床7-太空舱8-小型双人床9-大床10-特大床11-单人床12-胶囊床13-上下铺
		String def="大床";

		if(ChildBedType==1){
			def="双人床";
		}
		if(ChildBedType==2){
			def="圆床";
		}
		if(ChildBedType==3){
			def="水床";
		}
		if(ChildBedType==4){
			def="榻榻米";
		}
		if(ChildBedType==5){
			def="炕";
		}
		if(ChildBedType==6){
			def="沙发床";
		}
		if(ChildBedType==7){
			def="太空舱";
		}
		if(ChildBedType==8){
			def="小型双人床";
		}

		if(ChildBedType==9){
			def="大床";
		}
		if(ChildBedType==10){
			def="特大床";
		}
		if(ChildBedType==11){
			def="单人床";
		}
		if(ChildBedType==12){
			def="胶囊床";
		}
		if(ChildBedType==13){
			def="上下铺";
		}




		return def;

	}
	public static void main(String[] args) {

	}
}
