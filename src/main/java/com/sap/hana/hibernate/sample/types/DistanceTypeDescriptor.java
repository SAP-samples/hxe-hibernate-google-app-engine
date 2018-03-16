package com.sap.hana.hibernate.sample.types;

import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.AbstractTypeDescriptor;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metric;

/**
 * Custom Hibernate Java type descriptor for handling {@link Distance} objects
 */
public class DistanceTypeDescriptor extends AbstractTypeDescriptor<Distance> {

	private static final long serialVersionUID = 6161131477142733727L;

	private static final Metric METERS = new Metric() {

		private static final long serialVersionUID = -4238148401016711959L;

		@Override
		public double getMultiplier() {
			return 6378137.0;
		}

		@Override
		public String getAbbreviation() {
			return "m";
		}
	};

	public static final DistanceTypeDescriptor INSTANCE = new DistanceTypeDescriptor();

	public DistanceTypeDescriptor() {
		super( Distance.class );
	}

	@Override
	public String toString(Distance value) {
		return String.valueOf( value.in( METERS ).getValue() );
	}

	@Override
	public Distance fromString(String string) {
		if ( string == null || string.isEmpty() ) {
			return null;
		}
		return new Distance( Double.parseDouble( string ), METERS );
	}

	@Override
	@SuppressWarnings({ "unchecked" })
	public <X> X unwrap(Distance value, Class<X> type, WrapperOptions options) {
		if ( value == null ) {
			return null;
		}
		if ( Double.class.isAssignableFrom( type ) ) {
			return (X) Double.valueOf( value.in( METERS ).getValue() );
		}
		if ( String.class.isAssignableFrom( type ) ) {
			return (X) toString( value );
		}
		throw unknownUnwrap( type );
	}

	@Override
	public <X> Distance wrap(X value, WrapperOptions options) {
		if ( value == null ) {
			return null;
		}
		if ( String.class.isInstance( value ) ) {
			return fromString( (String) value );
		}
		if ( Double.class.isInstance( value ) ) {
			return new Distance( ( (Double) value ).doubleValue() );
		}
		throw unknownWrap( value.getClass() );
	}

}
