package com.jinyan.service;

import java.util.List;

import com.jfinal.kit.HttpKit;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.druid.DruidPlugin;

public class TestService {

	public static void main(String[] args) {
	    DruidPlugin dp = new DruidPlugin("gd.tetuijiudian.cn", "gaode24", "Redbull2266_");
	    ActiveRecordPlugin arp = new ActiveRecordPlugin(dp);
	   
	    
	    // 与 jfinal web 环境唯一的不同是要手动调用一次相关插件的start()方法
	    dp.start();
	    arp.start();

	    String sql="select elongIdStr from gd_hotel where down=0";
	    List<Record> list=Db.find(sql);
	    for (Record record : list) {
			String url="http://localhost:8552/hotel/pushRoomInfo?elongId="+record.getStr("elongIdStr");
			String result=HttpKit.get(url);
			System.out.println(result);
		}
	  }
}
