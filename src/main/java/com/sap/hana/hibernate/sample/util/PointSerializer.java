package com.sap.hana.hibernate.sample.util;

import java.io.IOException;

import org.springframework.data.geo.Point;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * JSON serializer for {@link Point} objects
 */
public class PointSerializer extends StdSerializer<Point> {

	private static final long serialVersionUID = -8649120804219364571L;

	protected PointSerializer() {
		this( null );
	}

	protected PointSerializer(Class<Point> t) {
		super( t );
	}

	@Override
	public void serialize(Point value, JsonGenerator gen, SerializerProvider provider) throws IOException {
		gen.writeStartObject();
		gen.writeNumberField( "x", value.getX() );
		gen.writeNumberField( "y", value.getY() );
		gen.writeEndObject();
	}

}
