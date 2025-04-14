package com.jinyan.service;

import com.jinyan.utils.RedisUtil;
import redis.clients.jedis.Jedis;

public class LedisdbCache {

   static   String redis_perfix="LedisdbCache_common__"; //缓存键改了下，

    public static String getCache(String keys){

        String key=redis_perfix+keys;
        Jedis jedis = RedisUtil.getConn();
        String ret=  jedis.get(key);
        jedis.close();
        return ret;
    }



    public static String setCache(String keys,String info,int expireSecond){


        Jedis jedis = RedisUtil.getConn();
        String key=redis_perfix+keys;



       String ret=  jedis.set(key, info);
        jedis.expire(key, expireSecond);//秒

        jedis.close();

        return ret;
    }


}
