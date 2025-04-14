package com.jinyan.utils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisUtil 
{
	private static JedisPool pool = null;
	
	/**
	 * 获取jedis连接池
	 * */
	public static JedisPool getPool()
	{
		if(pool == null)
		{
			//创建jedis连接池配置
			JedisPoolConfig config = new JedisPoolConfig();
			//最大连接数
			config.setMaxTotal(100);
			//最大空闲连接
			config.setMaxIdle(5);
//			config.setMaxIdle(50); //高并发时，把空闲数量调高些

			//创建redis连接池 gd.tetuijiudian.cn
			pool = new JedisPool(config,"gd.tetuijiudian.cn",6379,2000,"3er6J2JQ1og71m");
		}
		return pool;
	}
	
	/**
	 * 获取jedis连接
	 * */
	public static Jedis getConn()
	{
		return getPool().getResource();
	}
}
