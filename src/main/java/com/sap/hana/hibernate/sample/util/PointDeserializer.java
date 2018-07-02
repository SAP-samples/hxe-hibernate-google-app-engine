package com.sap.hana.hibernate.sample.util;

import java.io.IOException;

import org.springframework.data.geo.Point;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.DoubleNode;

/**
 * JSON deserializer for {@link Point} objects
 */
public class PointDeserializer extends StdDeserializer<Point> {

	private static final long serialVersionUID = 6845533929020801084L;

	protected PointDeserializer(Class<?> vc) {
		super( vc );
	}

	@Override
	public Point deserialize(JsonParser p, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		JsonNode node = p.getCodec().readTree( p );
		Double x = (Double) ( (DoubleNode) node.get( "x" ) ).numberValue();
		Double y = (Double) ( (DoubleNode) node.get( "y" ) ).numberValue();
		return new Point( x.doubleValue(), y.doubleValue() );
	}

}
