package com.sap.hana.hibernate.sample.types;

import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.AbstractTypeDescriptor;
import org.springframework.data.geo.Point;

public class PointTypeDescriptor extends AbstractTypeDescriptor<Point> {

	private static final long serialVersionUID = 6161131477142733727L;

	public static final PointTypeDescriptor INSTANCE = new PointTypeDescriptor();

	public PointTypeDescriptor() {
		super(Point.class);
	}

	@Override
	public String toString(Point value) {
		return String.valueOf(value.getX()) + ',' + String.valueOf(value.getY());
	}

	@Override
	public Point fromString(String string) {
		if (string == null || string.isEmpty()) {
			return null;
		}
		String[] locations = string.split(",");
		assert locations.length == 2;
		return new Point(Double.parseDouble(locations[0]), Double.parseDouble(locations[1]));
	}

	@Override
	@SuppressWarnings({ "unchecked" })
	public <X> X unwrap(Point value, Class<X> type, WrapperOptions options) {
		if (value == null) {
			return null;
		}
		if (String.class.isAssignableFrom(type)) {
			return (X) toString(value);
		}
		throw unknownUnwrap(type);
	}

	@Override
	public <X> Point wrap(X value, WrapperOptions options) {
		if (value == null) {
			return null;
		}
		if (String.class.isInstance(value)) {
			return fromString((String) value);
		}
		throw unknownWrap(value.getClass());
	}

}
