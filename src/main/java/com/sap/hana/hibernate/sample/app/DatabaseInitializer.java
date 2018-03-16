package com.sap.hana.hibernate.sample.app;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.geolatte.geom.G2D;
import org.geolatte.geom.Point;
import org.geolatte.geom.codec.Wkt;
import org.geolatte.geom.codec.Wkt.Dialect;
import org.geolatte.geom.codec.WktDecoder;
import org.geolatte.geom.crs.CoordinateReferenceSystems;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.google.common.collect.Streams;
import com.sap.hana.hibernate.sample.entities.Address;
import com.sap.hana.hibernate.sample.entities.Incident;
import com.sap.hana.hibernate.sample.repositories.AddressRepository;
import com.sap.hana.hibernate.sample.repositories.IncidentRepository;

@Component
public class DatabaseInitializer {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger( DatabaseInitializer.class );

	private static final SimpleDateFormat SDF = new SimpleDateFormat( "dd/MM/yyyy" );

	private static final WktDecoder WKT_DECODER = Wkt.newDecoder( Dialect.HANA_EWKT );

	private static final Pattern LOCATION_PATTERN = Pattern.compile( "\\((-?\\d+(\\.\\d+)?), (-?\\d+(\\.\\d+)?)\\)" );

	@PersistenceContext
	private EntityManager em;

	@Autowired
	private IncidentRepository incidentRepository;

	@Autowired
	private AddressRepository addressRepository;

	@Value("${csv.incidents.file.location:/csv/incidents.csv}")
	private String incidentsCsvFileLocation;

	@Value("${csv.addresses.file.location:/csv/addresses.csv}")
	private String addressesCsvFileLocation;

	@Value("${spring.jpa.properties.hibernate.jdbc.batch_size:50}")
	private int jdbcBatchSize;

	/**
	 * Trigger the import of incidents if necessary
	 * 
	 * @param event The context event
	 */
	@EventListener
	@Transactional
	public void importIncidents(ContextRefreshedEvent event) {
		if ( this.incidentRepository.count() > 0 ) {
			// the incidents are already loaded
			return;
		}

		log.info( "Importing incident sample data" );

		try {
			URL incidentsCsvFileLocationUrl = this.getClass().getResource( this.incidentsCsvFileLocation );
			if ( incidentsCsvFileLocationUrl == null ) {
				log.warn( "Unable to import incident sample data: the file " + this.incidentsCsvFileLocation
						+ " does not exist. Please download the incidents data set as CSV from https://data.sfgov.org/Public-Safety/-Change-Notice-Police-Department-Incidents/tmnf-yvry" );
			}
			else {
				Streams.mapWithIndex( Files.lines( Paths.get( incidentsCsvFileLocationUrl.toURI() ),
						Charset.forName( "UTF-8" ) ).skip( 1 ), (line, idx) -> {
							List<String> fields = parseLine( line );
							assert fields.size() == 13;

							Incident i = new Incident( Long.parseLong( fields.get( 12 ) ) );
							i.setIncidentNumber( fields.get( 0 ) );
							i.setCategory( fields.get( 1 ) );
							i.setDescription( fields.get( 2 ) );
							i.setDayOfWeek( fields.get( 3 ) );
							try {
								i.setDate( SDF.parse( fields.get( 4 ) ) );
							}
							catch (ParseException e) {
								log.info( "Unable to parse incident date: " + fields.get( 4 ), e );
							}
							i.setTime( fields.get( 5 ) );
							i.setPdDistrict( fields.get( 6 ) );
							i.setResolution( fields.get( 7 ) );
							i.setAddress( fields.get( 8 ) );
							try {
								double x = Double.parseDouble( fields.get( 9 ) );
								i.setX( x );
							}
							catch (NumberFormatException e) {
								log.info( "Error parsing x coordinate", e );
							}
							try {
								double y = Double.parseDouble( fields.get( 10 ) );
								i.setY( y );
							}
							catch (NumberFormatException e) {
								log.info( "Error parsing y coordinate", e );
							}
							if ( fields.get( 11 ).isEmpty() ) {
								log.info( "Location for incident with ID " + i.getPdId() + " is empty." );
								i.setLocation(
										(Point<G2D>) WKT_DECODER.decode( "SRID=4326;POINT(" + i.getX() + " " + i.getY() + ")",
												CoordinateReferenceSystems.WGS84 ) );
							}
							else {
								Matcher matcher = LOCATION_PATTERN.matcher( fields.get( 11 ) );
								if ( matcher.matches() ) {
									i.setLocation(
											(Point<G2D>) WKT_DECODER.decode( "SRID=4326;POINT(" + matcher.group( 3 ) + " " + matcher.group( 1 ) + ")",
													CoordinateReferenceSystems.WGS84 ) );
									if ( Double.isNaN( i.getX() ) ) {
										i.setX( Double.parseDouble( matcher.group( 3 ) ) );
									}

									if ( Double.isNaN( i.getY() ) ) {
										i.setY( Double.parseDouble( matcher.group( 1 ) ) );
									}
								}
								else {
									log.warn( "Unable to parse location string " + fields.get( 11 ) );
								}
							}

							if ( Double.isNaN( i.getX() ) || Double.isNaN( i.getY() ) ) {
								log.warn( "The incident with ID " + i.getPdId() + " doesn't have a valid location." );
								return new AbstractMap.SimpleEntry<>( idx, (Incident) null );
							}

							return new AbstractMap.SimpleEntry<>( idx, i );
						} ).forEach( entry -> {
							if ( entry.getValue() == null ) {
								return;
							}

							this.em.persist( entry.getValue() );

							if ( entry.getKey() % this.jdbcBatchSize == 0 ) {
								this.em.flush();
								this.em.clear();
							}
						} );
			}
		}
		catch (IOException | URISyntaxException e) {
			log.warn( "Error reading incidents file.", e );
		}

		log.info( "Importing incident sample data . . . ok" );
	}

	/**
	 * Trigger the import of addresses if necessary
	 * 
	 * @param event The context event
	 */
	@EventListener
	@Transactional
	public void importAddresses(ContextRefreshedEvent event) {
		if ( this.addressRepository.count() > 0 ) {
			// the addresses are already loaded
			return;
		}

		log.info( "Importing address sample data" );
		try {
			URL addressesCsvFileLocationUrl = this.getClass().getResource( this.addressesCsvFileLocation );
			if ( addressesCsvFileLocationUrl == null ) {
				log.warn( "Unable to import address sample data: the file " + this.addressesCsvFileLocation
						+ " does not exist. Please download the addresses data set from https://data.sfgov.org/Geographic-Locations-and-Boundaries/Addresses-Enterprise-Addressing-System/sr5d-tnui" );
			}
			else {
				Streams.mapWithIndex( Files.lines( Paths.get( addressesCsvFileLocationUrl.toURI() ),
						Charset.forName( "UTF-8" ) ).skip( 1 ), (line, idx) -> {
							List<String> fields = parseLine( line );
							assert fields.size() == 11;

							Address a = new Address( Long.parseLong( fields.get( 0 ) ) );
							a.setCnn( fields.get( 1 ) );
							a.setAddress( fields.get( 2 ) );
							a.setAddressNumber( fields.get( 3 ) );
							a.setAddressNumberSuffix( fields.get( 4 ) );
							a.setStreetName( fields.get( 5 ) );
							a.setStreetType( fields.get( 6 ) );
							a.setZipCode( fields.get( 7 ) );
							try {
								double x = Double.parseDouble( fields.get( 8 ) );
								a.setX( x );
							}
							catch (NumberFormatException e) {
								log.info( "Error parsing x coordinate", e );
							}
							try {
								double y = Double.parseDouble( fields.get( 9 ) );
								a.setY( y );
							}
							catch (NumberFormatException e) {
								log.info( "Error parsing y coordinate", e );
							}
							if ( fields.get( 10 ).isEmpty() ) {
								log.info( "Location for address with ID " + a.getBaseID() + " is empty." );
								a.setLocation(
										(Point<G2D>) WKT_DECODER.decode( "SRID=4326;POINT(" + a.getX() + " " + a.getY() + ")",
												CoordinateReferenceSystems.WGS84 ) );
							}
							else {
								Matcher matcher = LOCATION_PATTERN.matcher( fields.get( 10 ) );
								if ( matcher.matches() ) {
									a.setLocation(
											(Point<G2D>) WKT_DECODER.decode( "SRID=4326;POINT(" + matcher.group( 3 ) + " " + matcher.group( 1 ) + ")",
													CoordinateReferenceSystems.WGS84 ) );
									if ( Double.isNaN( a.getX() ) ) {
										a.setX( Double.parseDouble( matcher.group( 3 ) ) );
									}

									if ( Double.isNaN( a.getY() ) ) {
										a.setY( Double.parseDouble( matcher.group( 1 ) ) );
									}
								}
								else {
									log.warn( "Unable to parse location string " + fields.get( 10 ) );
								}
							}

							if ( Double.isNaN( a.getX() ) || Double.isNaN( a.getY() ) ) {
								log.warn( "The address with ID " + a.getBaseID() + " doesn't have a valid location." );
								return new AbstractMap.SimpleEntry<>( idx, (Address) null );
							}

							return new AbstractMap.SimpleEntry<>( idx, a );
						} ).forEach( entry -> {
							if ( entry.getValue() == null ) {
								return;
							}

							this.em.persist( entry.getValue() );

							if ( entry.getKey() % this.jdbcBatchSize == 0 ) {
								this.em.flush();
								this.em.clear();
							}
						} );
			}
		}
		catch (IOException | URISyntaxException e) {
			log.warn( "Error reading incidents file.", e );
		}

		log.info( "Importing address sample data . . . ok" );
	}

	/**
	 * Parse a line of the CSV files
	 * 
	 * @param line The line to parse
	 * @return A list of fields parsed from the line
	 */
	private List<String> parseLine(String line) {
		boolean quoted = false;
		List<String> fields = new ArrayList<>();
		StringBuilder currentField = new StringBuilder();

		for ( char c : line.toCharArray() ) {
			switch ( c ) {
				case '"':
					quoted = !quoted;
					break;
				case ',':
					if ( quoted ) {
						currentField.append( c );
					}
					else {
						fields.add( currentField.toString() );
						currentField = new StringBuilder();
					}
					break;
				default:
					currentField.append( c );

			}
		}

		fields.add( currentField.toString() );

		return fields;
	}
}
