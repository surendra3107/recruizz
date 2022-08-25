package com.bbytes.recruiz;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;

import com.bbytes.recruiz.service.BulkTenantMailgunEmailServiceImpl;

@Configuration
public class AppConfig {

	@Bean
	@Scope("prototype")
	@Lazy(value = true)
	public BulkTenantMailgunEmailServiceImpl getMailgunBulkEmailClient(String apikey, String domain) {
		return new BulkTenantMailgunEmailServiceImpl(apikey, domain);
	}
}
