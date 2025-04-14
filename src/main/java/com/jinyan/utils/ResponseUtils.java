package com.jinyan.utils;

import com.alibaba.fastjson.JSONObject;

public class ResponseUtils {
	private String code;



	private String sub_code="";


	private String sub_msg="";
    private String msg;
    private Object data;
    
    private JSONObject dataJson;
    
	public JSONObject getDataJson() {
		return dataJson;
	}
	public void setDataJson(JSONObject dataJson) {
		this.dataJson = dataJson;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public Object getData() {
		return data;
	}
	public void setData(Object data) {
		this.data = data;
	}
	public String getSub_code() {
		return sub_code;
	}

	public void setSub_code(String sub_code) {
		this.sub_code = sub_code;
	}
	public String getSub_msg() {
		return sub_msg;
	}

	public void setSub_msg(String sub_msg) {
		this.sub_msg = sub_msg;
	}

}



