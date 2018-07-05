package com.sap.hana.hibernate.sample.util;

import java.io.IOException;

import org.geolatte.geom.C2D;
import org.geolatte.geom.Point;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * JSON serializer for {@link Point} objects
 */
public class PointSerializer extends StdSerializer<Point<C2D>> {

	private static final long serialVersionUID = -8649120804219364571L;

	protected PointSerializer() {
		this( null );
	}

	protected PointSerializer(Class<Point<C2D>> t) {
		super( t );
	}

	@Override
	public void serialize(Point<C2D> value, JsonGenerator gen, SerializerProvider provider) throws IOException {
		gen.writeStartObject();
		gen.writeNumberField( "x", value.getPosition().getX() );
		gen.writeNumberField( "y", value.getPosition().getY() );
		gen.writeEndObject();
	}

}
