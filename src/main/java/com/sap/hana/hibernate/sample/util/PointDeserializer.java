package com.sap.hana.hibernate.sample.util;

import java.io.IOException;

import org.geolatte.geom.C2D;
import org.geolatte.geom.Point;
import org.geolatte.geom.codec.Wkt;
import org.geolatte.geom.codec.Wkt.Dialect;
import org.geolatte.geom.codec.WktDecoder;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.DoubleNode;

/**
 * JSON deserializer for {@link Point} objects
 */
public class PointDeserializer extends StdDeserializer<Point<C2D>> {

	private static final long serialVersionUID = 6845533929020801084L;

	private final WktDecoder decoder = Wkt.newDecoder( Dialect.HANA_EWKT );

	protected PointDeserializer(Class<?> vc) {
		super( vc );
	}

	@Override
	public Point<C2D> deserialize(JsonParser p, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		JsonNode node = p.getCodec().readTree( p );
		Double x = (Double) ( (DoubleNode) node.get( "x" ) ).numberValue();
		Double y = (Double) ( (DoubleNode) node.get( "y" ) ).numberValue();
		return (Point<C2D>) this.decoder.decode( "SRID=4326;POINT (" + x + " " + y + ")" );
	}

}
