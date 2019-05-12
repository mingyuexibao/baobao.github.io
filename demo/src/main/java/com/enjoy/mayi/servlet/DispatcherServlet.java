package com.enjoy.mayi.servlet;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.enjoy.mayi.anotation.EnjoyAutowired;
import com.enjoy.mayi.anotation.EnjoyController;
import com.enjoy.mayi.anotation.EnjoyRequestMapping;
import com.enjoy.mayi.anotation.EnjoyRequestParam;
import com.enjoy.mayi.anotation.EnjoyService;
import com.enjoy.mayi.controller.MayiController;

public class DispatcherServlet extends HttpServlet {
	
	List<String> classNames = new ArrayList<String>();
	Map<String,Object> beans = new HashMap<String,Object>();
	Map<String,Object> handMap = new HashMap<String,Object>();
	

	public void init(ServletConfig config){
		//扫描目录
		doScan("com.enjoy");
		
		//拿到类路径
		doInstance();
		
		doAutowired();
		
		urlMapping();//mayi/query =---->method
	}
	
	private void urlMapping() {
		for(Map.Entry<String, Object> entry:beans.entrySet()){
			Object instance = entry.getValue();
			Class<? extends Object> clazz = instance.getClass();
			if(clazz.isAnnotationPresent(EnjoyController.class)){
				EnjoyRequestMapping reqMapping = clazz.getAnnotation(EnjoyRequestMapping.class);
				String classPath = reqMapping.value();
				Method[] methods = clazz.getMethods();
				for(Method method:methods){
					if(method.isAnnotationPresent(EnjoyRequestMapping.class)){
						EnjoyRequestMapping reqMapping1 = method.getAnnotation(EnjoyRequestMapping.class);
						String methodPath = reqMapping1.value();
						
						handMap.put(classPath+methodPath, method);
					}else{
						continue;
					}
				}
			}
		}
		
	}

	private void doAutowired() {
		for(Map.Entry<String, Object> entry:beans.entrySet()){
			Object instance = entry.getValue();
			Class<? extends Object> clazz = instance.getClass();
			if(clazz.isAnnotationPresent(EnjoyController.class)){
				Field[] fields = clazz.getDeclaredFields();
				for(Field field:fields){
					if(field.isAnnotationPresent(EnjoyAutowired.class)){
						EnjoyAutowired auto = field.getAnnotation(EnjoyAutowired.class);
					    String key = auto.value();
					    Object value = beans.get(key);
					    //暴力访问
					    field.setAccessible(true);
					    try {
							field.set(instance, value);
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

	private void doInstance() {
		for (String className:classNames) {
			//com.enjoy.mayi.controller.MyyiController.class
			String cn = className.replace(".class", "");
			try {
				Class<?> clazz = Class.forName(cn);
				
				if(clazz.isAnnotationPresent(EnjoyController.class)){
					Object value = clazz.newInstance();
					EnjoyRequestMapping reqMap = clazz.getAnnotation(EnjoyRequestMapping.class);
					String key = reqMap.value();
					beans.put(key, value);
				}else if(clazz.isAnnotationPresent(EnjoyService.class)){
					Object value = clazz.newInstance();
					EnjoyService reqMap = clazz.getAnnotation(EnjoyService.class);
					String key = reqMap.value();
					beans.put(key, value);
				}
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

	//mvc.xml ---->basePackage=""
	private void doScan(String basePackage) {
		//扫描编译好的类路径
		URL url = this.getClass().getClassLoader().getResource("/"+basePackage.replaceAll("\\.", "/"));
		String fileStr = url.getFile();
		File file = new File(fileStr);
		
		String[] filesStr = file.list();
		for(String path:filesStr){
			File filePath = new File(fileStr+path);
			if(filePath.isDirectory()){
				doScan(basePackage+"."+path);
			}else{
				classNames.add(basePackage+"."+filePath.getName());
			}
		}
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		this.doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		//获取请求 /mayi/query ?
		String uri = req.getRequestURI();
		String context = req.getContextPath();// /springmvcdemo
		String path = uri.replace(context, "");
		
		Method method = (Method) handMap.get(path);
		MayiController instance = (MayiController) beans.get("/"+path.split("/")[1]);
		Object args[] = hand(req,resp,method);
		try {
			method.invoke(instance, args);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	private static Object[] hand(HttpServletRequest req, HttpServletResponse resp, Method method) {
		//拿到当前等待方法执行有哪些参数
		Class<?>[] paramClazzs = method.getParameterTypes();
		//根据参数额个数,new 一个参数的数组,将方法里的所有参数赋值到args来
		Object[] args = new Object[paramClazzs.length];
		int args_i = 0;
		int index = 0;
		for(Class<?> paramClazz:paramClazzs){
			if(ServletRequest.class.isAssignableFrom(paramClazz)){
				args[args_i++] = req;
			}
			if(ServletResponse.class.isAssignableFrom(paramClazz)){
				args[args_i++] = resp;
			}
			//从0--3判断有没有RequestParam注解,很明显paramClazz为0和1时,不是
			//当为2和3时为@RequestParam,需要解析
			//[@com.enjoy.mayi.annotation.EnjoyRequestParam(value=name)]
			Annotation[] paramAns = method.getParameterAnnotations()[index];
			if(paramAns.length>0){
				for(Annotation paramAn:paramAns){
					if(EnjoyRequestParam.class.isAssignableFrom(paramAn.getClass())){
						EnjoyRequestParam rp = (EnjoyRequestParam) paramAn;
						//找到注解里的name和age
						args[args_i++] = req.getParameter(rp.value());
					}
				}
			}
			index++;
		}
				
		return args;
	}

}
