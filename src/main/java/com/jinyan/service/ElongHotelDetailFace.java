package com.jinyan.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.kit.HttpKit;
import com.jinyan.common.Constant;
import com.jinyan.utils.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

//艺龙 https://open.elong.com/doc/info/cn-api-search-hotel_detail 相关
//高德 https://y.amap.com/docs/hotel/validateroomprice
public class ElongHotelDetailFace {

    public String method="hotel.detail";
    public JSONObject hotelDetail  ;
    public JSONObject RatePlan  ; // 当前选择的 RatePlan 方便后续操作
    public  double  TotalRate ;
    public Boolean  isOk=false; //是否有数据
    public JSONObject dataRequest = new JSONObject();



    private  String ElongApiUrlFormat="http://gd.tetuijiudian.cn:859/apivamap/rest?method=%s&data=%s";

    public void requestApi(JSONObject dataRequest){


        String  prestr= JSON.toJSONString(dataRequest);
        String data="{\"Version\":\"1.35\",\"Local\":\"zh_CN\",\"Request\":"+prestr+"}";
        String  url= null;
        try {
            url = String.format(ElongApiUrlFormat,  this.method, URLEncoder.encode(data, "utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return;
        }


        Map<String, String> headers = new HashMap<String, String>(16);
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        String ret = HttpKit.get(url,null, headers);

        this.hotelDetail=JSONObject.parseObject(ret);
//        System.out.println(ret);
        String Code = this.hotelDetail.getString("Code");
        if(Code.equals("0")){
            this.isOk=true;
        }
    }

    /*
        完成功能
        1、返回 对应的 roomTypeId
        2、赋值  this.RatePlan=RatePlanObject; 方便后结操作
        3、 赋值 TotalRate
     */
    public String getRoomTypeID(String RoomId,String  RatePlanId){
        JSONObject Result = this.hotelDetail.getJSONObject("Result");
        if(Result==null){

            return "";
        }
        JSONArray Hotels = Result.getJSONArray("Hotels");
        if(Hotels==null){

            return "";
        }

        for (Object h:Hotels ) {
            JSONObject hotel =(JSONObject)h;
            JSONArray Rooms = hotel.getJSONArray("Rooms");
            if(Rooms==null){
                return "";
            }
            for (Object room:Rooms ) {
                JSONObject roomObj =(JSONObject)room;
                String  RoomIdV = roomObj.getString("RoomId");
                if(!RoomIdV.equals(RoomId)){
                    continue; //不是相同roomid 就继续
                }
                JSONArray RatePlans = roomObj.getJSONArray("RatePlans");
                if(RatePlans==null){
                    return "";
                }

                for (Object RatePlan:RatePlans ) {
                    JSONObject RatePlanObject =(JSONObject)RatePlan;

                    String  RatePlanIdV = RatePlanObject.getString("RatePlanId");
                    if(!RatePlanIdV.equals(RatePlanId)){
                        continue; //不是相同RatePlanId 就继续
                    }

                    String  RoomTypeId = RatePlanObject.getString("RoomTypeId");
                    this.RatePlan=RatePlanObject;

                    this.TotalRate =   RatePlanObject.getDouble("TotalRate");

                    return  RoomTypeId;

                }
                String HotelId2=hotel.getString("HotelId");
            }
            String HotelId2=hotel.getString("HotelId");
        }

        return "";
    }
    public static void main(String[]args) throws Exception {
        ElongHotelDetailFace hotel_detail=new ElongHotelDetailFace();

        JSONObject dataRequest = new JSONObject();
        //{"ArrivalDate":"2023-08-29","DepartureDate":"2023-08-30","HotelIds":"40101006","Options":"2","PaymentType":"All"}
        dataRequest.put("ArrivalDate","2023-08-29");
       dataRequest.put("DepartureDate","2023-08-30");
        dataRequest.put("HotelIds","40101006");
        dataRequest.put("Options","2，12");
       dataRequest.put("PaymentType","All");

        hotel_detail.requestApi(dataRequest);
        JSONArray Hotels = hotel_detail.hotelDetail.getJSONObject("Results").getJSONArray("Hotels");


        for (Object h:Hotels ) {
            JSONObject hotel =(JSONObject)h;
            String HotelId=hotel.getString("HotelId");
            String HotelId2=hotel.getString("HotelId");
        }

    }

}


