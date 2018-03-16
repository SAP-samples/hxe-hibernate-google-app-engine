package com.sap.hana.hibernate.sample.util;

import org.geolatte.geom.G2D;
import org.geolatte.geom.Point;
import org.geolatte.geom.codec.Wkt;
import org.geolatte.geom.codec.Wkt.Dialect;
import org.geolatte.geom.codec.WktDecoder;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Converter for converting {@link String} objects to {@link Point} objects.
 */
@Component
public class StringToPointConverter implements Converter<String, Point<G2D>> {

	private final WktDecoder decoder = Wkt.newDecoder( Dialect.HANA_EWKT );

	public StringToPointConverter() {
	}

	@SuppressWarnings("unchecked")
	@Override
	public Point<G2D> convert(String location) {
		String[] locationComponents = location.split( "," );
		assert locationComponents.length == 2;
		return (Point<G2D>) this.decoder.decode( "SRID=4326;POINT (" + locationComponents[1].trim() + " " + locationComponents[0].trim() + ")" );
	}
}
