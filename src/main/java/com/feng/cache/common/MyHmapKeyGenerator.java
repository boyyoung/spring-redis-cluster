package com.feng.cache.common;

import java.lang.reflect.Method;

import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.interceptor.KeyGenerator;

public class MyHmapKeyGenerator implements KeyGenerator {

	@Override
	public Object generate(Object target, Method method, Object... params) {
		//获取传入class的方法，并获取注解的参数
		try{
			Class objclass=target.getClass();
			Method objm=objclass.getDeclaredMethod(method.getName(), method.getParameterTypes());				
			String[] value=getAnnotationValue(objm);
			if(value!=null && value.length>0){
				String ch= getParams(params);
				if(!StringUtils.isEmpty(ch))
					return value[0]+"&"+getParams(params);
				else
					return value[0];
			}else{
				return getParams(params);
			}
		}catch(Exception e){
			//整合到各系统的时候，注意用log
			e.printStackTrace();
			return null;
		}
	}
	private String getParams(Object... params){
		if (params.length == 0) {
			return "";
		}else if (params.length == 1) {
			return (String) params[0];
		}else{
			String retparm="";
			for(Object pram:params){
				retparm=retparm+(String)pram;
			}
			return retparm;
		}
	}
	private String[] getAnnotationValue(Method objm)throws Exception{
		
		Cacheable anno = objm.getAnnotation(Cacheable.class);
		if(anno!=null){
			return anno.value();
		}
		CacheEvict evt = objm.getAnnotation(CacheEvict.class);
		if(evt!=null){
			return evt.value();
		}
		CachePut cput = objm.getAnnotation(CachePut.class);
		if(cput!=null)
			return cput.value();
		return null;
		
	}
}
