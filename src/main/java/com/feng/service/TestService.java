package com.feng.service;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;

import com.feng.entity.User;


public interface TestService {
	
	String defaultCache(String name);
	
	String guavaCache60seconds(String name);
	String guavaCache10minutes(String name);
	String guavaCache1hour(String name);
	
	String redisCache60seconds(User name);
	String redisCache10minutes(String name);
	String redisCache1hour(String name,String task);
	
	boolean redisCache1hour();
	List redisCache1hourList();

	String redisCache1hourOther(String name);
	
	
}
