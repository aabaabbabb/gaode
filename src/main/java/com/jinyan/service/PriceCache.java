package com.jinyan.service;

import com.jinyan.utils.RedisUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

import java.security.PublicKey;

public class PriceCache {

    static Boolean cached=false;   //默认不开启缓存
    String redis_perfix="price_cache003_"; //缓存键改了下，
    String redis_perfix_elongprice_hoteldetail="redis_perfix_elongprice_hoteldetail";
    static  String redis_perfix_pressureTest="redis_perfix_pressureTest";
    static  String redis_perfix_cachedprice_key="redis_perfix_cachedprice_key"; //这个是在线 要不要开cache 和上边的 price_cache_功能不一样



    Integer expireSecond=86400*20 ;// 20天
//    Integer expireSecond= 300 ;//
    public void setExpireSecond(Integer expireSecond) {
        this.expireSecond = expireSecond;
    }

    public static Boolean getCached() {
        return cached;
    }
    public static void setCached(Boolean cached) {
        PriceCache.cached = cached;
    }

    public String getPrice(String req){
        if(!cached){
            return "";
        }
        String key=redis_perfix+req;
        Jedis jedis = RedisUtil.getConn();
        String ret=  jedis.get(key);
        jedis.close();
        return ret;
    }



    public String setPrice(String req,String priceInfo){
        if(!cached){
            return "";
        }



        if(req.equals("570056042|2023-08-15|2023-08-19|274442867|0012")){
            System.out.println("1:");
        }

        Jedis jedis = RedisUtil.getConn();
        String key=redis_perfix+req;



       String ret=  jedis.set(key, priceInfo);
        jedis.expire(key, expireSecond);//秒

        jedis.close();

        return ret;
    }

    public void delPrice(String req){

        String key=redis_perfix+req;
        Jedis jedis = RedisUtil.getConn();
        jedis.del(key);
        jedis.close();
    }

    /*
    这个是缓存一下，艺龙请求的数据，因为艺龙请求的时候，因为可以传的参数是 roomtypeid ,高德的是roomid,
    其中有两个方法转换，
    一是用艺龙的产品信息，把roomtypeid转换成roomid，请求的时候带上roomtypeid，但同样还是得请求hotel.detail数据，而且 不同的供应商可能会相同的 roomtypeid,这就恶心人了，
     要是不同的供应商用了相同的roomtypeid,那得先把产品接口所有的roomtyped找出来，再多次请求hotel.detail
    二是请求艺龙数据时，不传roomtypeid,获取所有rooms,然后遍历，
    目前采用的是第二种，可以先缓存一下艺龙的数据，这样同一个酒店，短时间内，就不用多次请求了,
    还是就是自已取增量信息来更新数据库。
     */
    public String setElongPriceReq(String req,String priceInfo, Integer expireSecondPrice ){
        if(!cached){
            return "";
        }



        Jedis jedis = RedisUtil.getConn();
        String key=redis_perfix_elongprice_hoteldetail+req;

        String ret=  jedis.set(key, priceInfo);
        jedis.expire(key, expireSecondPrice);//秒

        jedis.close();

        return ret;
    }

    public String getElongPriceReq(String req){
        if(!cached){
            return "";
        }
        String key=redis_perfix_elongprice_hoteldetail+req;
        Jedis jedis = RedisUtil.getConn();
        String ret=  jedis.get(key);
        jedis.close();
        return ret;
    }

    public static boolean getPressureTest(){

        String key=redis_perfix_pressureTest;
        Jedis jedis = RedisUtil.getConn();
        String ret=  jedis.get(key);
        jedis.close();
        if(ret!=null && ret.equals("1")){
            return  true;
        }
        return false;
    }
    public static boolean setPressureTest(boolean b){

        String key=redis_perfix_pressureTest;
        Jedis jedis = RedisUtil.getConn();
        if(b){
            jedis.set(key,"1");
        }else{
            jedis.del(key);
        }

        jedis.close();

        return true;
    }

    public static boolean getCacheStatus(){

        String key=redis_perfix_cachedprice_key;
        Jedis jedis = RedisUtil.getConn();
        String ret=  jedis.get(key);
        jedis.close();
        if(ret!=null && ret.equals("1")){
            return  true;
        }
        return false;
    }
    public static boolean setCacheStatus(boolean b){

        String key=redis_perfix_cachedprice_key;
        Jedis jedis = RedisUtil.getConn();
        if(b){
            jedis.set(key,"1");
        }else{
            jedis.del(key);
        }

        jedis.close();

        return true;
    }

    public String returnNoRoom(String HotelId){

        String r="{\"response\":{\"data\":{\"RoomInfos\":[],\"HotelID\":\""+HotelId+"\"},\"msg\":\"success\",\"sub_code\":null,\"code\":\"10000\",\"sub_msg\":null}}";
        return  r;
    }

}
