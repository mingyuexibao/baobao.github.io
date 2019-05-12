package com.enjoy.mayi.service.impl;

import com.enjoy.mayi.anotation.EnjoyService;
import com.enjoy.mayi.service.MyyiService;

@EnjoyService("MyyiServiceImpl")
public class MyyiServiceImpl implements MyyiService{

	public String query(String name, String age) {
		return "name==="+name+";    age==="+age;
	}

}
