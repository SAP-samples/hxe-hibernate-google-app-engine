package com.sap.hana.hibernate.sample.util;

import java.io.IOException;

import org.geolatte.geom.G2D;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.codec.Wkt;
import org.geolatte.geom.codec.Wkt.Dialect;
import org.geolatte.geom.codec.WktDecoder;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.TextNode;

/**
 * JSON deserializer for {@link Geometry} objects
 */
public class GeometryDeserializer extends StdDeserializer<Geometry<G2D>> {

	private static final long serialVersionUID = -6685794523469443917L;

	private final WktDecoder decoder = Wkt.newDecoder( Dialect.HANA_EWKT );

	protected GeometryDeserializer(Class<?> vc) {
		super( vc );
	}

	@SuppressWarnings("unchecked")
	@Override
	public Geometry<G2D> deserialize(JsonParser p, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		StringBuilder wkt = new StringBuilder();
		wkt.append( "SRID=4326;" );

		JsonNode node = p.getCodec().readTree( p );

		String type = ( (TextNode) node.get( "type" ) ).textValue();
		wkt.append( type ).append( "(" );

		ArrayNode positions = (ArrayNode) node.get( "positions" );
		for ( int i = 0; i < positions.size(); i++ ) {
			JsonNode positionNode = positions.get( i );
			Double lat = (Double) ( (DoubleNode) positionNode.get( "lat" ) ).numberValue();
			Double lon = (Double) ( (DoubleNode) positionNode.get( "lon" ) ).numberValue();

			if ( i > 0 ) {
				wkt.append( "," );
			}
			wkt.append( lat ).append( " " ).append( lon );
		}
		wkt.append( ")" );

		return (Geometry<G2D>) this.decoder.decode( wkt.toString() );
	}

}
