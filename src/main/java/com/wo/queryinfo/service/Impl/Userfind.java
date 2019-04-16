package com.wo.queryinfo.service.Impl;

import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.wo.queryinfo.service.QueryCombo;
import com.wo.queryinfo.utils.HttpClientUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpException;


public class Userfind implements QueryCombo {
	 
	public JSON sendpost(String userNumberMonth ) throws Exception {
		JSONObject code =new JSONObject();
		try{
 			Map jsonObject = JSONObject.parseObject(userNumberMonth);
			jsonObject.put("queryType","1");
			String sss = jsonObject.toString();
			String url = "http://211.94.133.153:11214/openapi/bss/queryunifybillinfo/v1.0";
			Map<String, Object> headers = new HashMap<>();
			headers.put("Authorization", "platformID=\"3b608228-b810-4f4c-8026-4ef79209ce5d\",password=\"Unicom789\"");
			headers.put("Content-Type", "application/json");
//		Map<String, Object> params = new HashMap<String, Object>();
//		params.put("userNumber", "13135633706");
//	 	String json = "{\"userNumber\":\"16601100123\",\"queryMonth\":\"201903\",\"queryType\":\"1\"}";
			String doPost = HttpClientUtils.doPost(url, headers, sss);
			String result = doPost;
			System.out.println(result);
			JSONObject map = JSONObject.parseObject(result);
			Object object = map.get("accDetalInfo");
			String adinfo = JSONArray.toJSONString(object);
			JSONArray accDetalInfo = JSONArray.parseArray(adinfo);
			Object accLists = accDetalInfo.get(0);
			JSONObject accList = JSONObject.parseObject(JSONArray.toJSONString(accLists));
			Object accList0 = accList.get("accList");
 			JSONArray list = JSONArray.parseArray(JSONObject.toJSONString(accList0));
			JSONObject oldjson1=new JSONObject();
 			for(Object o : list){
				JSONObject acc = JSONObject.parseObject(JSONObject.toJSONString(o));
				String accType = acc.get("accType").toString();
 				if("月固定费".equals(accType))
				{	JSONArray detail = JSONArray.parseArray(JSONObject.toJSONString(acc.get("detailList")));
					Object delObj = detail.get(0);
					JSONObject del = JSONObject.parseObject(JSONObject.toJSONString(delObj));
					Object object2 = del.get("accFee");
					//System.out.println("结果："+object2);
					String substring = object2.toString().substring(0,3);
					System.out.println("结果："+substring);
					oldjson1.put("packageMoney", substring);
 				}
 			}
			Object aaa = map.get("resultCode");
			if(aaa.equals("0")){
				code.put("code", "0");
				code.put("message","成功");
				code.put("userInfo", oldjson1);
				System.out.println(code);
				return code;
			}
		}catch(HttpException e){
 			code.put("code", "1");
			code.put("message","失败");
			code.put("userInfo", "第三方系统异常");
			System.out.println(code);
			return code;
		}catch(SocketTimeoutException e){
 			code.put("code", "2");
			code.put("message","失败");
			code.put("userInfo", "连接等待超时");
			System.out.println(code);
			return code;
		}catch(Exception e){
			code.put("code", "4");
			code.put("message","失败");
			code.put("userInfo", "内部系统错误");
			System.out.println(code);
			return code;
		}
		return code;
	}

	@Override
	public JSON Nettime(String userNumber ) throws Exception {
		JSONObject code =new JSONObject();
		try {
 		String url = "http://211.94.133.153:11214/openapi/bss/queryunifyuserinfo/v1.0";
		Map<String, Object> headers = new HashMap<>();
		headers.put("Authorization", "platformID=\"3b608228-b810-4f4c-8026-4ef79209ce5d\",password=\"Unicom789\"");
		headers.put("Content-Type", "application/json");
		String json = "{\"userNumber\":\"18571683290\"}";
		String result = HttpClientUtils.doPost(url, headers, userNumber);
		System.out.println(result);
		JSONObject map = JSONObject.parseObject(result);
		Object object = map.get("openDate");
		String string = object.toString();
		System.out.println(string);
		Object aaa = map.get("resultCode");
			if(aaa.equals("0")){
				code.put("code", "0");
				code.put("message","成功");
				code.put("userInfo", string);
				System.out.println(code);
				return code;
			}
		}catch(HttpException e){

			code.put("code", "1");
			code.put("message","失败");
			code.put("userInfo", "第三方系统异常");
			System.out.println(code);
			return code;
		}catch(SocketTimeoutException e){

			code.put("code", "2");
			code.put("message","失败");
			code.put("userInfo", "连接等待超时");
			System.out.println(code);
			return code;
		}catch(Exception e){
			code.put("code", "4");
			code.put("message","失败");
			code.put("userInfo", "内部系统错误");
			System.out.println(code);
			return code;
		}
		return code;
	}
}
