package com.sap.hana.hibernate.sample.types;

import org.hibernate.dialect.Dialect;
import org.hibernate.type.AbstractSingleColumnStandardBasicType;
import org.hibernate.type.DiscriminatorType;
import org.hibernate.type.descriptor.sql.DoubleTypeDescriptor;
import org.springframework.data.geo.Distance;

/**
 * Custom Hibernate type for handling {@link Distance} objects
 */
public class DistanceType extends AbstractSingleColumnStandardBasicType<Distance>
		implements DiscriminatorType<Distance> {

	private static final long serialVersionUID = -7379378266689572421L;

	public static final DistanceType INSTANCE = new DistanceType();

	public DistanceType() {
		super( DoubleTypeDescriptor.INSTANCE, DistanceTypeDescriptor.INSTANCE );
	}

	@Override
	public Distance stringToObject(String xml) throws Exception {
		return fromString( xml );
	}

	@Override
	public String objectToSQLString(Distance value, Dialect dialect) throws Exception {
		return toString( value );
	}

	@Override
	public String getName() {
		return "distance";
	}

	@Override
	protected boolean registerUnderJavaType() {
		return true;
	}

}
