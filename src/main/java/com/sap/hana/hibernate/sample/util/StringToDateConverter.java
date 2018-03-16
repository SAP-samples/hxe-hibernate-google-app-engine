package com.sap.hana.hibernate.sample.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Converter for converting {@link String} objects to {@link Date} objects.
 */
@Component
public class StringToDateConverter implements Converter<String, Date> {

	private final SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd" );

	public StringToDateConverter() {
	}

	@Override
	public Date convert(String date) {
		try {
			return this.sdf.parse( date );
		}
		catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}
}
