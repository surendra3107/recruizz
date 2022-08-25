package com.bbytes.recruiz;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.TimeZone;

import org.javers.spring.boot.sql.JaversSqlAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.bbytes.recruiz.utils.SSLUtil;

@EnableAutoConfiguration(exclude = { FlywayAutoConfiguration.class, ElasticsearchAutoConfiguration.class,
		JaversSqlAutoConfiguration.class })
@SpringBootApplication
@EnableScheduling
public class RecruizApplication {

	public static void main(String[] args) throws KeyManagementException, NoSuchAlgorithmException {
		System.setProperty("spring.devtools.restart.enabled", "false");
		TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
		SpringApplication.run(RecruizApplication.class, args);
		
		// this should be removed once a proper solution is found for this issue
		SSLUtil.turnOffSslChecking();
	}
}
