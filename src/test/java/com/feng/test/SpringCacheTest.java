package com.feng.test;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.feng.entity.User;
import com.feng.service.TestService;



@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:applicationContext.xml")
public class SpringCacheTest {

	@Autowired
	private TestService service;
	
	@Test
	public void testCache(){
		
	
		//String one = dao.get("ABE323");
		
		//String name=service.redisCache1hour("name","kang2");
		User user=new User();
		user.setId(1);
		user.setAge(2);
		user.setName("kang");
		//String name=service.redisCache60seconds(user);
		//清掉一个cache 下的所有缓存，
		//boolean name=service.redisCache1hour();
		
		List lis=service.redisCache1hourList();
		
		
		//System.out.println("redis:"+name); 
		System.out.println("list:"+lis.get(1)); 
		
	}


}
