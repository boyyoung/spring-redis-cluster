
本方案是结合spring-cache的注解进行开发的一个缓存框架，整合了guava 和redis 两种类型的缓存 guava 缓存较为简单，只需要引入相关的jar包即可，redisCluster 则需要在服务端创建redisCluster集群，此种集群采用ruby构建
注意下面这个配置是创建一个cacheManager 用于引入spring-cache的注解 cachePut等 ，注意key-generator 是配置了默认的key生产规则，具体规则需要自己写代码实现，本方案中结合spring ehcache，采用默认key的生成策略为 “函数名+ 参数值”方式，多个参数继续叠加的方式 ，注意本方案中实现的自定义key生成器不支持参数为对象key值规则，当然你也可以使用spring el表达式来定义key的规则

	<cache:annotation-driven cache-manager="cacheManager" key-generator="myKeyGenerator"/> 

 
 
下面着重讲解一下redis的封装，
redis 采用了redisCluster 集群模式，利用ruby进行集群的构建，因此调用的时候采用了jedis 中的JedisCluster 客户端进行调用，注意redis 集群模式不同于其他集群，redis集群采用的是多主从分片机制，即一个主从节点和其他主从节点数据并一致，读取的时候JedisCluster会根据key值得生成策略路由到相关节点，此种意味着一个主从节点挂掉，这个节点上数据就没有了（这个是我自己的理解，欢迎拍砖，如果错误一定要找我探讨一下，进行深入学习）

服务器redisCluster集群搭建完毕后，节点配置策略采用了网上别人写的代码，JedisClusterFactory 这个类，

	<bean name="genericObjectPoolConfig" class="org.apache.commons.pool2.impl.GenericObjectPoolConfig" >  
	        <property name="maxWaitMillis" value="-1" />  
	        <property name="maxTotal" value="1000" />  
	        <property name="minIdle" value="8" />  
	        <property name="maxIdle" value="100" />  
	</bean>  
	  
	<bean id="jedisCluster" class="com.feng.cache.redis.JedisClusterFactory">  
	    <property name="addressConfig">  
	        <value>classpath:redis.properties</value>  
	    </property>  
	    <property name="addressKeyPrefix" value="address" />   <!--  属性文件里  key的前缀 -->    
	    <property name="timeout" value="10000" />  
	    <property name="maxRedirections" value="6" />  
	    <property name="genericObjectPoolConfig" ref="genericObjectPoolConfig" />  
	</bean>  
	
注入后采用了apache pool连创建链接池，这个连接池也可以采用其他连接池，目的是提升连接数，提升性能使用的时候采用配置如下

<!-- redis缓存1小时-->

    <bean id="redisCache3" class="com.feng.cache.redis.RedisHmapCacheFactoryBean">
		 <property name="name" value="redisCache1hour"/><!-- 缓存名称 -->
		 <property name="liveTime" value="3600"/><!-- 缓存时间 ：以秒为单位-->
    	 <property name="jedisCluster" ref="jedisCluster" /><!-- 缓存实现 -->
    </bean>	 
    
   注意，这里有我自己实现的一个cacheManager ，目的是封装cache的注解，用于和spring-cache的annotation 进行整合，即：@Cacheable ，@CacheEvict ，@CachePut 等注解
    注意spring-cache 默认的生成key生成策略是 直接以传入的参数为key ，此种方式很容易产生重复数据，而  spring ehcache 采用按照缓存名称（value）进行数据分片，不同缓存vlaue中key一致不会影响缓存读取，
    本方案中redis的实现模拟了spring ehcahce 将缓存存放在redis的hmap中，并按照指定规则
    
      <bean id="MyKeyGenerator" class="com.feng.cache.common.MyKeyGenerator"/> 
   即： 缓存名（vlaue）（bean的name属性）为hmap名，以参数值为key值，多个参数值是相加的，目前参数值只支持简单数据类型，比如String ,int等单一元素为key值，暂不支持以对象为key值，（这个可以扩展，未来视使用情况扩展支撑）
   本方案使用的默认key生成器为MyKeyGenerator，实现的key自定义生成器采用将函数名拼在参数前面的模式目的是避免同一个java类中key重复
    
cacheManager 是我自己实现的一个接入类，RedisHmapCacheFactoryBean 按照缓存名进行分片（hmap），根据传入的参数为key值，并模仿ehcache 的模式设置了缓存value的生命周期，今后如果添加其他属性，视情况扩展，

     <property name="name" value="redisCache60seconds"/><!-- 缓存名称 -->
		 <property name="liveTime" value="60"/><!-- 缓存时间：以秒为单位 -->  存活的生命周期，单位秒
	
RedisHmapCacheFactoryBean 注入了jedisCluster

	<property name="jedisCluster" ref="jedisCluster" /><!-- 缓存实现 --> 

使用springcache注解的用于封装注解，直接注解在函数实现上，便于代码整合，同时能够实现延迟加载，如果数据不存在缓存中则读入缓存，存在则直接获取缓存中的数据

使用方式：
默认方式：
@Cacheable(value="redisCache10minutes") 
 注意这种方式value作为hmap的mapname，key=函数名+函数 参数值1+函数参数值2+***（）,建议采用默认方式，刷新的时候，只需要见一个相同名称的函数
示例如下：key=redisCache10minutes+name（name的值）

	@Cacheable(value="redisCache10minutes")//添加缓存
	//@CacheEvict(value="redisCache10minutes")//刷新name=*的缓存（注意，是删除）
	public String redisCache10minutes(String name) {
		System.err.println("db start break redisCache10minutes");
		return "redisCache10minutes";
	} 

 
springel表达式设置方式如下:


	@Override
	@Cacheable(value="redisCache1hour",key="'redisCache1hour'+#name+#task")
	//@CacheEvict(value="redisCache1hour",key="'redisCache1hour&'+#name+#task")//单条刷新
	public String redisCache1hour(String name,String task) {
		int red=random.nextInt(10);
		System.err.println("db start break redisCache"+red+"hour");
		return "redisCache"+red+"hour";
	}
	
或者：

	@Override
	@Cacheable(value="redisCache60seconds",key="'redisCache1hour'+#name.id")
	public String redisCache60seconds(User name) {
		int red=random.nextInt(10);
		System.err.println("db start break redisCache"+red+"seconds");
		return "redisCache"+red+"seconds";
	}
注意，对于没有参数的函数需要加缓存，使用@Cacheable的时候 key只默认是函数名参考，如果是时用@CacheEvict 则表示要刷新hmap为value值得真个缓存模块
	

@CacheEvict使用注意事项：
该注解添加在没有参数的函数上，则默认刷新整个value的缓存，添加在有参数的函数上，不用el表达式注解的话，刷新value下 传入的参数对应的缓存，使用el表达式，则刷新按照el表达式规则刷新制定key值得缓存
	
以上是我最近按照自己思路，参考springehcahce 进行的一个封装（便于平台切换缓存），大家有更好的方法，欢迎交流，封装的不够无痕切换ehcache ，大家有好的意见可以指点






5.	Redis-cluster
Redis3.0.0版本中自身支持cluster模式，redis提供的创建集群的工具为ruby语言脚本。所以在安装redis并配置为cluster模式时，需要同时安装ruby工具（包括ruby,rubygems），以及ruby语言中的redis支持包。
Ruby+rubygems+redis.gem安装
cd /home/software/ruby
tar –xzf ruby-2.2.2.tar.gz
cd ruby-2.2.2
./configure --prefix=/home/ruby
make && make install
export PATH=.:/home/ruby/bin:$PATH
cd /home/software/ruby
tar –xzf rubygems-2.4.6.tgz
cd rubygems-2.4.6
ruby setup.rb
cd /home/software/ruby
gem install -l redis-3.2.1.gem
Redis编译安装
cd /home/software
tar –xzf redis-3.0.0.tar.gz
cd redis-3.0.0
make
mkdir –p /home/redis
cd /home/redis
mkdir bin config data log
cp /home/software/redis-3.0.0/src/* /home/redis/bin/
之后需要修改redis的配置及创建集群的脚本等

Redis配置文件示例
cat redis-31301.conf
daemonize yes
pidfile /home/redis/log/redis-31301.pid
port 31301
bind *.*.*.*
cluster-enabled yes
cluster-config-file nodes.conf
cluster-node-timeout 2000
cluster-require-full-coverage no
aof-rewrite-incremental-fsync yes
tcp-backlog 511
timeout 300
tcp-keepalive 1
loglevel verbose
logfile "/home/redis /log/31301.log"
databases 16
save 900 1
save 300 10
save 60 10000
stop-writes-on-bgsave-error no
rdbcompression yes
rdbchecksum yes
dbfilename dump.rdb
dir /home/redis/data
slave-serve-stale-data yes
slave-read-only yes
repl-diskless-sync no
repl-diskless-sync-delay 5
repl-disable-tcp-nodelay no
slave-priority 100
appendonly yes
appendfilename "appendonly.aof"
appendfsync everysec
no-appendfsync-on-rewrite no
auto-aof-rewrite-percentage 100
auto-aof-rewrite-min-size 64mb
aof-load-truncated yes
lua-time-limit 5000
slowlog-log-slower-than 10000
slowlog-max-len 128
latency-monitor-threshold 0
notify-keyspace-events ""
hash-max-ziplist-entries 512
hash-max-ziplist-value 64
list-max-ziplist-entries 512
list-max-ziplist-value 64
set-max-intset-entries 512
zset-max-ziplist-entries 128
zset-max-ziplist-value 64
hll-sparse-max-bytes 3000
activerehashing yes
client-output-buffer-limit normal 0 0 0
client-output-buffer-limit slave 256mb 64mb 60
client-output-buffer-limit pubsub 32mb 8mb 60
hz 10
maxclients 15000


redis创建集群脚本
cat createCluster.sh 
#!/bin/sh  
export PATH=/home/ruby/bin:.:$PATH
echo "start create redis cluster..."
#创建redis集群时使用到的IP必须与redis.conf文件中bind的IP相同，redis-cluster访问时必须使用该IP
./bin/redis-trib.rb create --replicas 2 127.0.0.1:7000 127.0.0.1:7001 127.0.0.1:7002 127.0.0.1:7003 127.0.0.1:7004 127.0.0.1:7005 
echo "create redis cluster succfull..."



