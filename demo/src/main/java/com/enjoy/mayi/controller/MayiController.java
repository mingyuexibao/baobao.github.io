package com.enjoy.mayi.controller;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.enjoy.mayi.anotation.EnjoyAutowired;
import com.enjoy.mayi.anotation.EnjoyController;
import com.enjoy.mayi.anotation.EnjoyRequestMapping;
import com.enjoy.mayi.anotation.EnjoyRequestParam;
import com.enjoy.mayi.service.MyyiService;

@EnjoyController
@EnjoyRequestMapping("/mayi")
public class MayiController {
    
	@EnjoyAutowired("MyyiServiceImpl")
	private MyyiService myyiService;
	
	@EnjoyRequestMapping("/query")
	public void query(HttpServletRequest reqest,HttpServletResponse response
			,@EnjoyRequestParam("name") String name
			,@EnjoyRequestParam("age") String age) throws IOException{
		reqest.setCharacterEncoding("UTF-8");
		
		String result = myyiService.query(name, age);
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");
		PrintWriter pw = response.getWriter();
		pw.write(result);
		
	}

}
