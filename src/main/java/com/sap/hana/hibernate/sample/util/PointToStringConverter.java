package com.sap.hana.hibernate.sample.util;

import org.geolatte.geom.G2D;
import org.geolatte.geom.Point;
import org.geolatte.geom.codec.Wkt;
import org.geolatte.geom.codec.Wkt.Dialect;
import org.geolatte.geom.codec.WktEncoder;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Converter for converting {@link Point} objects to {@link String} objects.
 */
@Component
public class PointToStringConverter implements Converter<Point<G2D>, String> {

	private final WktEncoder encoder = Wkt.newEncoder( Dialect.HANA_EWKT );

	public PointToStringConverter() {
	}

	@Override
	public String convert(Point<G2D> point) {
		return this.encoder.encode( point );
	}
}
