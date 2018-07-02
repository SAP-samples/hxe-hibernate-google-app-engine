package com.sap.hana.hibernate.sample.util;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.geo.Point;
import org.springframework.stereotype.Component;

/**
 * Converter for converting {@link String} objects to {@link Point} objects.
 */
@Component
public class StringToPointConverter implements Converter<String, Point> {

	public StringToPointConverter() {
	}

	@Override
	public Point convert(String location) {
		String[] locationComponents = location.split( "," );
		assert locationComponents.length == 2;
		return new Point( Double.parseDouble( locationComponents[1].trim() ), Double.parseDouble( locationComponents[0].trim() ) );
	}
}
