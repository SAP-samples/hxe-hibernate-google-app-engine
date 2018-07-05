package com.sap.hana.hibernate.sample.util;

import java.io.IOException;

import org.geolatte.geom.G2D;
import org.geolatte.geom.Geometry;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * JSON serializer for {@link Geometry} objects
 */
public class GeometrySerializer extends StdSerializer<Geometry<G2D>> {

	private static final long serialVersionUID = -8999726420608330915L;

	protected GeometrySerializer() {
		this( null );
	}

	protected GeometrySerializer(Class<Geometry<G2D>> t) {
		super( t );
	}

	@Override
	public void serialize(Geometry<G2D> value, JsonGenerator gen, SerializerProvider provider) throws IOException {
		gen.writeStartObject();
		gen.writeStringField( "type", value.getGeometryType().name() );
		gen.writeFieldName( "positions" );
		gen.writeStartArray();
		for ( G2D position : value.getPositions() ) {
			gen.writeStartObject();
			gen.writeNumberField( "lat", position.getLat() );
			gen.writeNumberField( "lon", position.getLon() );
			gen.writeEndObject();
		}
		gen.writeEndArray();
		gen.writeEndObject();
	}

}
