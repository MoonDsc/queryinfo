package com.wo.queryinfo.service;

import com.alibaba.fastjson.JSON;

import javax.servlet.annotation.HttpConstraint;
import javax.servlet.annotation.HttpMethodConstraint;

public interface QueryCombo {

	@HttpMethodConstraint("www.wo")
	JSON sendpost(String userNumberMonth) throws Exception;
	@HttpMethodConstraint("www.wo")
	JSON Nettime(String userNumber ) throws Exception;
	
}
