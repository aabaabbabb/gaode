package com.jinyan.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;
import com.jfinal.kit.HttpKit;
import com.jinyan.utils.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

//艺龙 https://open.elong.com/doc/info/cn-api-search-hotel_data_validate 相关
//高德 https://y.amap.com/docs/hotel/validateroomprice
public class ElongHotelDataValidatelFace {

    public String method="hotel.data.validate";
    public JSONObject hotelDataValidate  ;
    public JSONObject dataRequest = new JSONObject();
    public Boolean  isOk=false; //是否有数据
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
//        headers.put("Content-Type", "application/x-www-form-urlencoded");
        String ret = HttpKit.get(url,null, headers);

        /*
        {"Code":"0","Result":{"ResultCode":"OK","GuaranteeRate":0.0,"CancelTime":"2023-08-10T06:43:01+08:00","CurrencyCode":"RMB","FreeCancelTime":"2023-08-10T06:43:01+08:00","CancelChange":false},"Guid":"e6a626b3-9a8b-4f19-b2ab-a9fc1f1096de"}
        {"Code":"H001167|不可订，房间数量需小于8间","Guid":"90079c41-1a3f-4177-9edd-02f37491654c"}
        {"Code":"H001084-2|总价(TotalPrice)错误，应该不小于结算价:5,397.84","Result":{"ResultCode":"Rate","GuaranteeRate":0.0,"CurrencyCode":"RMB","ErrorMessage":"H001084-2|总价(TotalPrice)错误，应该不小于结算价:5,397.84","CancelChange":false},"Guid":"77a7cbe5-0b3f-4313-a725-9f9aae43b4ef"}
        {"Code":"H000002|房型编号必须填写(RoomTypeId) ","Guid":"96b096fe-5062-4127-95f0-dc0fa19cf143"}
         */


        this.hotelDataValidate=JSONObject.parseObject(ret);
        String Code = this.hotelDataValidate.getString("Code");
        if(Code.equals("0")){
            this.isOk=true;
        }
//        System.out.println(ret);

    }

    public static void main(String[]args)  {
        ElongHotelDataValidatelFace hotel_data_validate=new ElongHotelDataValidatelFace();

        JSONObject dataRequest = new JSONObject();
        //{"ArrivalDate":"2023-08-29","DepartureDate":"2023-08-30","HotelIds":"40101006","Options":"2","PaymentType":"All"}

       String ArrivalDate="2023-08-29";
       String DepartureDate="2023-08-30";
       String EarliestArrivalTime=ArrivalDate+" 14:00:00";
       String LatestArrivalTime=DepartureDate+" 17:00:00";
       String HotelId= "40101006";
       String RoomId= "1181";
       int RatePlanId= 258178048;


       /****  找出对应的 RoomTypeID  *****/
        ElongHotelDetailFace hotel_detail=new ElongHotelDetailFace();
        JSONObject dataRequest_detail = new JSONObject();
        //{"ArrivalDate":"2023-08-29","DepartureDate":"2023-08-30","HotelIds":"40101006","Options":"2","PaymentType":"All"}
        dataRequest_detail.put("ArrivalDate","2023-08-29");
        dataRequest_detail.put("DepartureDate","2023-08-30");
        dataRequest_detail.put("HotelIds","40101006");
        dataRequest_detail.put("Options","2,12");
        dataRequest_detail.put("PaymentType","All");
        hotel_detail.requestApi(dataRequest_detail);

        if(hotel_detail.hotelDetail==null || hotel_detail.isOk==false){
            // 没有找到对应的酒店
            return;
        }
        String RoomTypeId = hotel_detail.getRoomTypeID(RoomId,String.valueOf(RatePlanId));
        if(Strings.isNullOrEmpty(RoomTypeId)){
            // 没有找到对应的酒店
            return;
        }

        /****  找出对应的 RoomTypeID  end *****/

        dataRequest.put("ArrivalDate",ArrivalDate);
       dataRequest.put("DepartureDate",DepartureDate);
       dataRequest.put("EarliestArrivalTime",EarliestArrivalTime);
       dataRequest.put("LatestArrivalTime",LatestArrivalTime);
        dataRequest.put("HotelId",HotelId);
        dataRequest.put("RoomId",RoomId);
        dataRequest.put("RoomTypeID",RoomTypeId);
        dataRequest.put("RatePlanId",RatePlanId);
        dataRequest.put("TotalPrice",6397.84);
        dataRequest.put("NumberOfRooms",7);
//        dataRequest.put("NumberOfAdults",200);
//        dataRequest.put("HotelCode",2);
//        dataRequest.put("SupplierId",2);


        hotel_data_validate.requestApi(dataRequest);

         if(hotel_data_validate.hotelDataValidate==null || !hotel_data_validate.isOk){

            //不可订
            return;
        }

        JSONObject Result=hotel_data_validate.hotelDataValidate.getJSONObject("Result");
        if(Result==null){

            //不可订
            return;
        }
        String ResultCode   =Result.getString("ResultCode");
        Double GuaranteeRate   =Result.getDouble("GuaranteeRate");
        String CancelTime   =Result.getString("CancelTime");
        String FreeCancelTime   =Result.getString("FreeCancelTime");
        String PenaltyAmount   =Result.getString("PenaltyAmount"); //罚金金额


    }

}


