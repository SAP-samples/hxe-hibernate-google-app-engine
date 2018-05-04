package com.sap.hana.hibernate.sample.types;

import org.hibernate.dialect.Dialect;
import org.hibernate.type.AbstractSingleColumnStandardBasicType;
import org.hibernate.type.DiscriminatorType;
import org.hibernate.type.descriptor.sql.VarcharTypeDescriptor;
import org.springframework.data.geo.Point;

public class PointType extends AbstractSingleColumnStandardBasicType<Point> implements DiscriminatorType<Point> {

	private static final long serialVersionUID = -7379378266689572421L;

	public static final PointType INSTANCE = new PointType();

	public PointType() {
		super(VarcharTypeDescriptor.INSTANCE, PointTypeDescriptor.INSTANCE);
	}

	@Override
	public Point stringToObject(String xml) throws Exception {
		return fromString(xml);
	}

	@Override
	public String objectToSQLString(Point value, Dialect dialect) throws Exception {
		return toString(value);
	}

	@Override
	public String getName() {
		return "point";
	}

	@Override
	protected boolean registerUnderJavaType() {
		return true;
	}

}
