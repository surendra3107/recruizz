package com.bbytes.recruiz.web.config;

import java.util.List;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;

/**
 * 
 * @author Thanneer
 *
 */
@Configuration
public class WebMvcConfig extends WebMvcConfigurerAdapter {

	@Value("${multipart.max-file-size}")
	private String fileSize;

	@Value("${multipart.max-request-size}")
	private String request_size;

	@Value("${file.public.access.folder.path}")
	private String publicFolder;

	private static Logger logger = LoggerFactory.getLogger(WebMvcConfig.class);

	/*
	 * Here we register the Hibernate4Module into an ObjectMapper, then set this
	 * custom-configured ObjectMapper to the MessageConverter and return it to
	 * be added to the HttpMessageConverters of our application
	 */
	public MappingJackson2HttpMessageConverter jacksonMessageConverter() {
		MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();

		ObjectMapper mapper = new ObjectMapper();
		// Registering Hibernate4Module to support lazy objects
		mapper.registerModule(new Hibernate4Module());
		mapper.registerModule(new Hibernate5Module());

		messageConverter.setObjectMapper(mapper);
		return messageConverter;
	}

	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		// Here we add our custom-configured HttpMessageConverter
		converters.add(jacksonMessageConverter());
		super.configureMessageConverters(converters);
	}

	@Bean
	public static StandardServletMultipartResolver multipartResolver() {
		StandardServletMultipartResolver resolver = new StandardServletMultipartResolver();
		return resolver;
	}

	@Bean
	public MultipartResolver multipartResolvers() {
		CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
		long maxSize = 5000000; // appx 5 MB, -1 is unlimited
		multipartResolver.setMaxUploadSizePerFile(maxSize);
		logger.debug("Max multi part file size : " + fileSize + "\t Max multi request Size : " + request_size);
		return multipartResolver;
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		if (!registry.hasMappingForPattern("/pubset/**")) {
			if (SystemUtils.IS_OS_LINUX) {
				publicFolder = "file:" + publicFolder;
			} else {
				publicFolder = "file:///" + publicFolder;
			}
			registry.addResourceHandler("/pubset/**").addResourceLocations(publicFolder);

			registry.addResourceHandler("/index.html").addResourceLocations("classpath:static/index.html").setCachePeriod(0);

			super.addResourceHandlers(registry);
		}
	}

}