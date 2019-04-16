package com.wo.queryinfo.service.Impl;


import org.junit.Test;
import org.springframework.web.bind.annotation.RequestMapping;


import java.util.ArrayList;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.swing.Spring;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
 

 
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wo.queryinfo.utils.HttpClientUtils;
 

 
 
 
 
public class TestConverter   {
	 public void sendHttpPost( ) throws Exception {
	 	CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpResponse response = null;
		HttpPost httpPost = new HttpPost("http://211.94.133.153:11214/openapi/bss/queryunifybillinfo/v1.0");  
		httpPost.addHeader("Authorization","platformID=\"3b608228-b810-4f4c-8026-4ef79209ce5d\",password=\"Unicom789\"");
		httpPost.addHeader("Content-Type","application/json");
		String json = "{\"userNumber\":\"18613379867\"}";
		httpPost.setEntity(new StringEntity(json)); 
		CloseableHttpResponse responses = httpClient.execute(httpPost); 
		System.out.println(responses.getStatusLine().getStatusCode()+"\n");
		HttpEntity entity = responses.getEntity(); 
		String responseContent = EntityUtils.toString(entity,"UTF-8");
		System.out.println(responseContent);
		responses.close();  
		httpClient.close();  
		  
		}
	//@Test
	 
		public void sendHttpPost2( ) throws Exception {  
		String url = "http://211.94.133.153:11214/openapi/bss/queryunifyuserinfo/v1.0";
		Map<String, Object> headers = new HashMap<>();
		headers.put("Authorization", "platformID=\"3b608228-b810-4f4c-8026-4ef79209ce5d\",password=\"Unicom789\"");
		headers.put("Content-Type", "application/json");
//		Map<String, Object> params = new HashMap<String, Object>();
//		params.put("userNumber", "13135633706");
		String json = "{\"userNumber\":\"15611112383\"}";
		String result = HttpClientUtils.doPost(url, headers, json);
		System.out.println(result);
		Test man3 = JSONObject.parseObject(result, Test.class);
		 
	}
	//@Test
	 
	public void sendHttpPost3( ) throws Exception {  
	String url = "http://211.94.133.153:11214/openapi/bss/queryunifybillinfo/v1.0";
	Map<String, Object> headers = new HashMap<>();
	headers.put("Authorization", "platformID=\"3b608228-b810-4f4c-8026-4ef79209ce5d\",password=\"Unicom789\"");
	headers.put("Content-Type", "application/json");
//	Map<String, Object> params = new HashMap<String, Object>();
//	params.put("userNumber", "13135633706");
 	String json = "{\"userNumber\":\"16601100123\",\"queryMonth\":\"201903\",\"queryType\":\"1\"}";
	String result = HttpClientUtils.doPost(url, headers, json);
	System.out.println(result);
	   
	 
	 
}
	 
	@Test
	public void sendpost3( ) throws Exception {  
		String url = "http://211.94.133.153:11214/openapi/bss/queryunifybillinfo/v1.0";
		Map<String, Object> headers = new HashMap<>();
		headers.put("Authorization", "platformID=\"3b608228-b810-4f4c-8026-4ef79209ce5d\",password=\"Unicom789\"");
		headers.put("Content-Type", "application/json");
//		Map<String, Object> params = new HashMap<String, Object>();
//		params.put("userNumber", "13135633706");
	 	String json = "{\"userNumber\":\"16601100123\",\"queryMonth\":\"201903\",\"queryType\":\"1\"}";
		String doPost = HttpClientUtils.doPost(url, headers, json);
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
			}
		}
	}

	 
	
	
	
	
	
	
	 
	
	
	
	
	
	
	
	
	
	
	
	
	
 }
