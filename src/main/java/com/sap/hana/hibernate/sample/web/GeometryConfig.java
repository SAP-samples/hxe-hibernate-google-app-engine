package com.sap.hana.hibernate.sample.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.sap.hana.hibernate.sample.util.PointToStringConverter;
import com.sap.hana.hibernate.sample.util.StringToDateConverter;
import com.sap.hana.hibernate.sample.util.StringToPointConverter;

@Configuration
public class GeometryConfig implements WebMvcConfigurer {

	/**
	 * Register custom converters
	 */
	@Override
	public void addFormatters(FormatterRegistry registry) {
		registry.addConverter( new PointToStringConverter() );
		registry.addConverter( new StringToPointConverter() );
		registry.addConverter( new StringToDateConverter() );
	}

}
