package com.sap.hana.hibernate.sample.app;

import org.hibernate.boot.SessionFactoryBuilder;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.boot.spi.SessionFactoryBuilderFactory;
import org.hibernate.boot.spi.SessionFactoryBuilderImplementor;
import org.slf4j.LoggerFactory;
import org.springframework.data.geo.Distance;

import com.sap.hana.hibernate.sample.types.DistanceType;

public class CustomDataTypesRegistration implements SessionFactoryBuilderFactory {

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger( CustomDataTypesRegistration.class );

	/**
	 * Register Hibernate types for handling {@link Distance} objects
	 */
	@Override
	public SessionFactoryBuilder getSessionFactoryBuilder(final MetadataImplementor metadata,
			final SessionFactoryBuilderImplementor defaultBuilder) {
		logger.info( "Registering custom Hibernate data types" );
		metadata.getTypeResolver().registerTypeOverride( DistanceType.INSTANCE );
		return defaultBuilder;
	}
}
