package com.sap.hana.hibernate.sample.util;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.geo.Point;
import org.springframework.stereotype.Component;

/**
 * Converter for converting {@link Point} objects to {@link String} objects.
 */
@Component
public class PointToStringConverter implements Converter<Point, String> {

	public PointToStringConverter() {
	}

	@Override
	public String convert(Point point) {
		return point.getX() + "," + point.getY();
	}
}
