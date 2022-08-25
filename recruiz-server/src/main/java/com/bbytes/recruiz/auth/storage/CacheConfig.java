package com.bbytes.recruiz.auth.storage;

import javax.annotation.Resource;

import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.core.RedisTemplate;

import com.bbytes.recruiz.service.SpringProfileService;

@Configuration
public class CacheConfig {

	@Resource
	private SpringProfileService profileService;

	/*@Resource
	private RedisTemplate<String, Object> redisTemplate;
*/
	@Bean
	public CacheManager cacheManager() {
/*		if (profileService.isProdMode()) {
			RedisCacheManager redisCacheManager = new RedisCacheManager(redisTemplate);
			redisCacheManager.setUsePrefix(true);
			redisCacheManager.initializeCaches();
			return redisCacheManager;
		} else {
*/			ConcurrentMapCacheManager concurrentMapCacheManager = new ConcurrentMapCacheManager();
			return concurrentMapCacheManager;
//		}

	}

}
