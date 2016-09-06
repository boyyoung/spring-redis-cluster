package com.feng.cache.common;

import java.lang.reflect.Method;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.interceptor.KeyGenerator;

public class MyKeyGenerator implements KeyGenerator {

	@Override
	public Object generate(Object target, Method method, Object... params) {
		String key= method.getName();
		boolean ievtict=isCacheEvict(target,method);

		if (params.length == 0 && ievtict) {
			return null;
		}else if(params.length == 0 && !ievtict){
			return key;
		}else {
			for(int i=0;i<params.length;i++){
				Object param = params[i];
				if (param != null) {
					key =key+param;
				}
			}
			return key;
		}
	}
	
	private boolean isCacheEvict(Object target, Method method){
		try {
			Class objclass=target.getClass();
			Method objm= objclass.getDeclaredMethod(method.getName(), method.getParameterTypes());
			CacheEvict evt = objm.getAnnotation(CacheEvict.class);
			if(evt!=null){
				return true;
			}
		} catch (NoSuchMethodException | SecurityException e) {
			return false;
		}	
		return false;
	}

}
