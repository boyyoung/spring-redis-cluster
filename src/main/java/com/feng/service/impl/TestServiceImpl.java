package com.feng.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.feng.entity.User;
import com.feng.service.TestService;

@Service
public class TestServiceImpl implements TestService{
	Random random = new Random();
	@Override
	@Cacheable(value="default")
	public String defaultCache(String name) {
		System.err.println("db start break defaultCache");
		return "defaultCache";
	}
	
	@Override
	@Cacheable(value="guavaCache60seconds")
	public String guavaCache60seconds(String name) {
		System.err.println("db start break guavaCache60seconds");
		return "guavaCache60seconds";
	}


	@Override
	@Cacheable(value="guavaCache10minutes")
	public String guavaCache10minutes(String name) {
		System.err.println("db start break guavaCache10minutes");
		return "guavaCache10minutes";
	}

	@Override
	@Cacheable(value="guavaCache1hour")
	public String guavaCache1hour(String name) {
		System.err.println("db start break guavaCache1hour");
		return "guavaCache1hour";
	}

	@Override
	//@Cacheable(value="redisCache60seconds",key="'redisCache1hour&'+#name.id")	
	@Cacheable(value="redisCache60seconds")
	public String redisCache60seconds(User name) {
		int red=random.nextInt(10);
		System.err.println("db start break redisCache"+red+"seconds");
		return "redisCache"+red+"seconds";
	}

	@Override
	@Cacheable(value="redisCache10minutes")
	public String redisCache10minutes(String name) {
		System.err.println("db start break redisCache10minutes");
		return "redisCache10minutes";
	}

	@Override
	@Cacheable(value="redisCache1hour" ,key="'redisCache1hour'+#name+#task")
	//@CacheEvict(value="redisCache1hour")
	public String redisCache1hour(String name,String task) {
		int red=random.nextInt(10);
		System.err.println("db start break redisCache"+red+"hour");
		return "redisCache"+red+"hour";
	}
	
	@Override
	//@Cacheable(value="redisCache1hour111")
	public String redisCache1hourOther(String name) {
		System.err.println("db start break redisCache1hour");
		return "redisCache1hour";
	}
	
	@Override
	@Cacheable(value="redisCache1hour")
	public List redisCache1hourList() {
		System.err.println("db start break redisCache1hour");
		List list =new ArrayList();
		list.add("ceshi1");
		list.add("hahahah");
		list.add("nihaoba");
		return list;
	}


	@CacheEvict(value="redisCache1hour")
	//@Cacheable(value="redisCache1hour" ,key="redisCache1hour")
	public boolean redisCache1hour() {
		System.err.println("db start break redisCache1hour  delete");
		return true;
	}

}
