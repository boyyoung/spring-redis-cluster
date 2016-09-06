package com.feng.cache.redis;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;

import redis.clients.jedis.JedisCluster;


public class RedisHmapCacheFactoryBean implements Cache {

  private JedisCluster jedisCluster;
  private String name;
  private int liveTime;
  @Override
  public Object getNativeCache() {
    return this.jedisCluster;
  }

  @Override
  public ValueWrapper get(Object key) {
   String keyf = (String) key;
	
    byte[] value = jedisCluster.hget(name.getBytes(), keyf.getBytes());
    if (value == null) 
	       return null;
	else	    	
     return  new SimpleValueWrapper(toObject(value));
  }
  
  @Override
  public void evict(Object key) {
	String keyf = (String) key;
    
    if(keyf!=null)
		  jedisCluster.hdel(name.getBytes(), keyf.getBytes());
	  else
		  jedisCluster.del(name.getBytes());
   
  }

  @Override
  public void put(Object key, Object value) {
    String keyf = (String) key;
    
    jedisCluster.hset(name.getBytes(),keyf.getBytes(), toByteArray(value));
    if (liveTime > 0) {
    	jedisCluster.expire(name.getBytes(), liveTime);
    }
   
  }

  /**
   * 描述 : <Object转byte[]>. <br>
   * <p>
   * <使用方法说明>
   * </p>
   * 
   * @param obj
   * @return
   */
  private byte[] toByteArray(Object obj) {
    byte[] bytes = null;
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try {
      ObjectOutputStream oos = new ObjectOutputStream(bos);
      oos.writeObject(obj);
      oos.flush();
      bytes = bos.toByteArray();
      oos.close();
      bos.close();
    } catch (IOException ex) {
      ex.printStackTrace();
    }
    return bytes;
  }

  /**
   * 描述 : <byte[]转Object>. <br>
   * <p>
   * <使用方法说明>
   * </p>
   * 
   * @param bytes
   * @return
   */
  private Object toObject(byte[] bytes) {
    Object obj = null;
    try {
      ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
      ObjectInputStream ois = new ObjectInputStream(bis);
      obj = ois.readObject();
      ois.close();
      bis.close();
    } catch (IOException ex) {
      ex.printStackTrace();
    } catch (ClassNotFoundException ex) {
      ex.printStackTrace();
    }
    return obj;
  }

  @SuppressWarnings("deprecation")
  @Override
  public void clear() {
	  jedisCluster.flushDB();
  }

	public void setName(String name) {
		this.name = name;
	}

	@Override
  	public String getName() {
		return this.name;
	}
	
	public long getLiveTime() {
		return liveTime;
	}
	
	public JedisCluster getJedisCluster() {
		return jedisCluster;
	}

	public void setJedisCluster(JedisCluster jedisCluster) {
		this.jedisCluster = jedisCluster;
	}

	public void setLiveTime(int liveTime) {
		this.liveTime = liveTime;
	}

	@Override
	public <T> T get(Object key, Class<T> type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ValueWrapper putIfAbsent(Object arg0, Object arg1) {
		// TODO Auto-generated method stub
		return null;
	}
}