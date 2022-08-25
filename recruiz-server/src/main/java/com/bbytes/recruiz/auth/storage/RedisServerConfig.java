/*package com.bbytes.recruiz.auth.storage;

import javax.annotation.Resource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
@Profile("prod")
public class RedisServerConfig {

	@Resource
	private Environment environment;

	@Bean
	JedisConnectionFactory jedisConnectionFactory() {
		JedisConnectionFactory jcf = new JedisConnectionFactory();
		jcf.setHostName(environment.getProperty("redis.hostname"));
		jcf.setPort(environment.getProperty("redis.port", Integer.class));

		String passowrd = environment.getProperty("redis.password");
		if (passowrd != null && !passowrd.trim().isEmpty())
			jcf.setPassword(passowrd);

		jcf.setUsePool(true);
		return jcf;
	}

	@Bean
	public RedisTemplate<String, Object> redisTemplate() {
		RedisTemplate<String, Object> template = new RedisTemplate<String, Object>();
		template.setConnectionFactory(jedisConnectionFactory());
		return template;
	}
	
	@Bean
	RedisTemplate<?, ?> redisTemplate(RedisConnectionFactory connectionFactory) {

		RedisTemplate<byte[], byte[]> template = new RedisTemplate<byte[], byte[]>();
		template.setConnectionFactory(connectionFactory);

		return template;
	}



}
*/