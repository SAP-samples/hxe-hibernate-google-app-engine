package com.sap.hana.hibernate.sample.app;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;

import org.geolatte.geom.G2D;
import org.geolatte.geom.Point;
import org.geolatte.geom.codec.Wkt;
import org.geolatte.geom.codec.Wkt.Dialect;
import org.geolatte.geom.codec.WktDecoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.google.common.collect.Streams;
import com.sap.hana.hibernate.sample.entities.Address;
import com.sap.hana.hibernate.sample.entities.Incident;
import com.sap.hana.hibernate.sample.repositories.AddressRepository;
import com.sap.hana.hibernate.sample.repositories.IncidentRepository;

@Component
public class DatabaseInitializer {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger( DatabaseInitializer.class );

	private static final SimpleDateFormat SDF = new SimpleDateFormat( "MM/dd/yyyy" );

	private static final Pattern LOCATION_PATTERN = Pattern.compile( "\\((-?\\d+(\\.\\d+)?), (-?\\d+(\\.\\d+)?)\\)" );

	private static final WktDecoder WKT_DECODER = Wkt.newDecoder( Dialect.HANA_EWKT );

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

	@Value("${csv.cloud.storage.enabled:false}")
	private boolean useCloudStorage;

	@Value("${csv.cloud.storage.bucket}")
	private String cloudStorageBucket;

	@Value("${spring.jpa.properties.hibernate.jdbc.batch_size:50}")
	private int jdbcBatchSize;

	/**
	 * Add term mappings for the address search
	 * 
	 * @param event The context event
	 */
	@EventListener
	@Transactional
	@Order(3)
	public void importTermMappings(ContextRefreshedEvent event) {
		Query query = this.em.createQuery( "delete from TermMapping" );
		query.executeUpdate();

		importTermMapping( "ADDRESS", "street", "st", BigDecimal.valueOf( 1 ) );
		importTermMapping( "ADDRESS", "avenue", "ave", BigDecimal.valueOf( 1 ) );
		importTermMapping( "ADDRESS", "boulevard", "blvd", BigDecimal.valueOf( 1 ) );
		importTermMapping( "ADDRESS", "circle", "cir", BigDecimal.valueOf( 1 ) );
		importTermMapping( "ADDRESS", "court", "ct", BigDecimal.valueOf( 1 ) );
		importTermMapping( "ADDRESS", "drive", "dr", BigDecimal.valueOf( 1 ) );
		importTermMapping( "ADDRESS", "hill", "hl", BigDecimal.valueOf( 1 ) );
		importTermMapping( "ADDRESS", "highway", "hwy", BigDecimal.valueOf( 1 ) );
		importTermMapping( "ADDRESS", "lane", "ln", BigDecimal.valueOf( 1 ) );
		importTermMapping( "ADDRESS", "place", "pl", BigDecimal.valueOf( 1 ) );
		importTermMapping( "ADDRESS", "plaza", "plz", BigDecimal.valueOf( 1 ) );
		importTermMapping( "ADDRESS", "road", "rd", BigDecimal.valueOf( 1 ) );
		importTermMapping( "ADDRESS", "stairway", "stwy", BigDecimal.valueOf( 1 ) );
		importTermMapping( "ADDRESS", "terrace", "ter", BigDecimal.valueOf( 1 ) );

		importTermMapping( "ADDRESS", "wy", "way", BigDecimal.valueOf( 1 ) );
		importTermMapping( "ADDRESS", "prk", "park", BigDecimal.valueOf( 1 ) );
	}

	private void importTermMapping(String listId, String term1, String term2, BigDecimal weight) {
		// TODO
	}

	/**
	 * Trigger the import of incidents if necessary
	 * 
	 * @param event The context event
	 */
	@EventListener
	@Transactional
	@Order(1)
	public void importIncidents(ContextRefreshedEvent event) {
		if ( this.incidentRepository.count() > 0 ) {
			// the incidents are already loaded
			return;
		}

		log.info( "Importing incident sample data" );

		try {
			Path incidentsCsvFileLocationPath;
			if ( this.useCloudStorage ) {
				if ( this.cloudStorageBucket == null || this.cloudStorageBucket.isEmpty() ) {
					log.warn( "Importing incident sample data from cloud storage requires a valid bucket name." );
					return;
				}

				incidentsCsvFileLocationPath = getFileFromCloudStorage( "incidents.csv" );
			}
			else {
				URL incidentsCsvFileLocationUrl = this.getClass().getResource( this.incidentsCsvFileLocation );
				if ( incidentsCsvFileLocationUrl == null ) {
					log.warn( "Unable to import incident sample data: the file " + this.incidentsCsvFileLocation
							+ " does not exist. Please download the incidents data set as CSV from https://data.sfgov.org/Public-Safety/-Change-Notice-Police-Department-Incidents/tmnf-yvry" );
					return;
				}

				incidentsCsvFileLocationPath = Paths.get( incidentsCsvFileLocationUrl.toURI() );
				if ( this.incidentsCsvFileLocation.matches( ".+\\.zip(\\.001)?$" ) ) {
					List<Path> extractedFiles = extractZip( incidentsCsvFileLocationPath );
					if ( extractedFiles.size() != 1 ) {
						throw new IllegalArgumentException( "The ZIP file must contain exactly one CSV file." );
					}
					incidentsCsvFileLocationPath = extractedFiles.get( 0 );
				}
			}

			log.info( "Importing incident sample data from " + incidentsCsvFileLocationPath );

			try ( Stream<String> lines = Files.lines( incidentsCsvFileLocationPath, StandardCharsets.UTF_8 ) ) {
				Long importedRecords = Streams.mapWithIndex( lines.skip( 1 ), (line, idx) -> {
					if ( Thread.currentThread().isInterrupted() ) {
						throw new RuntimeException( new InterruptedException( "The current thread has been interrupted" ) );
					}
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
						if ( !isValidXCoordinate( x, i.getPdId(), "incident" ) ) {
							return Long.valueOf( 0 );
						}
						i.setX( x );
					}
					catch (NumberFormatException e) {
						log.info( "Error parsing x coordinate", e );
					}
					try {
						double y = Double.parseDouble( fields.get( 10 ) );
						if ( !isValidYCoordinate( y, i.getPdId(), "incident" ) ) {
							return Long.valueOf( 0 );
						}
						i.setY( y );
					}
					catch (NumberFormatException e) {
						log.info( "Error parsing y coordinate", e );
					}

					if ( fields.get( 11 ).isEmpty() ) {
						log.info( "Location for incident with ID " + i.getPdId() + " is empty." );
						i.setLocation( createLocation( i.getX(), i.getY() ) );
					}
					else {
						Matcher matcher = LOCATION_PATTERN.matcher( fields.get( 11 ) );
						if ( matcher.matches() ) {
							i.setLocation( createLocation( Double.parseDouble( matcher.group( 3 ) ),
									Double.parseDouble( matcher.group( 1 ) ) ) );

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
						return Long.valueOf( 0 );
					}

					this.em.persist( i );

					if ( idx % this.jdbcBatchSize == 0 ) {
						this.em.flush();
						this.em.clear();
					}

					return Long.valueOf( 1 );
				} ).collect( Collectors.reducing( Long.valueOf( 0 ), e -> e, Long::sum ) );

				log.info( "Imported " + importedRecords + " incidents." );
			}
		}
		catch (IOException | URISyntaxException e) {
			log.warn( "Error reading incidents file.", e );
		}

		log.info( "Importing incident sample data . . . ok" );
	}

	private Path getFileFromCloudStorage(String fileName) throws IOException {
		log.info( "Getting file " + fileName + " from cloud storage" );
		Path tempDirectory = Files.createTempDirectory( "sfpd" );
		Path tempFile = tempDirectory.resolve( fileName );
		URL fileUrl = new URL( "https://storage.googleapis.com/" + this.cloudStorageBucket + "/" + fileName );

		try ( InputStream fileStream = fileUrl.openStream() ) {
			Files.copy( fileStream, tempFile, StandardCopyOption.REPLACE_EXISTING );
		}

		log.info( "File " + fileName + " successfully saved to " + tempFile );

		return tempFile;
	}

	private List<Path> extractZip(Path csvFileLocationPath) throws IOException {
		final String fileName = csvFileLocationPath.getFileName().toString();
		final String fileLocationPrefix = fileName.substring( 0, fileName.lastIndexOf( '.' ) );
		List<Path> extractedFiles = new ArrayList<>();
		try ( Stream<Path> zipFiles = Files.find( csvFileLocationPath.getParent(), 1, (path, attributes) -> {
			String pathFileName = path.getFileName().toString();
			return attributes.isRegularFile() && pathFileName.startsWith( fileLocationPrefix )
					&& pathFileName.matches( fileLocationPrefix + "\\.(zip|z[0-9]{2}|[0-9]{3})" );
		} ).sorted( (path1, path2) -> {
			if ( path1.getFileName().toString().endsWith( ".zip" ) ) {
				return 1;
			}
			else if ( path2.getFileName().toString().endsWith( ".zip" ) ) {
				return -1;
			}
			else {
				return path1.compareTo( path2 );
			}
		} ) ) {
			List<InputStream> pathList = null;
			try {
				pathList = zipFiles.map( path -> {
					try {
						InputStream inputStream = Files.newInputStream( path );
						if ( path.getFileName().toString().endsWith( ".z01" ) ) {
							// Skip an extension header on the first file if present
							byte[] header = new byte[4];
							inputStream.read( header );
							long headerValue = 0 | header[0] | header[1] << 8 | header[2] << 16 | header[3] << 24;
							if ( headerValue == 0x04034b50L ) {
								// regular entry header => close the stream and return new stream starting from
								// the
								// beginning
								inputStream.close();
								inputStream = Files.newInputStream( path );
							}
						}
						return inputStream;
					}
					catch (IOException e) {
						throw new UncheckedIOException( e );
					}
				} ).collect( Collectors.toList() );

				if ( pathList.isEmpty() ) {
					throw new IllegalArgumentException( "No zip files found at " + csvFileLocationPath.getParent() );
				}

				Path tempDirectory = Files.createTempDirectory( "sfpd" );
				try ( ZipInputStream zis = new ZipInputStream(
						new SequenceInputStream( Collections.enumeration( pathList ) ) ) ) {
					ZipEntry entry = null;
					while ( ( entry = zis.getNextEntry() ) != null ) {
						if ( Thread.currentThread().isInterrupted() ) {
							throw new RuntimeException(
									new InterruptedException( "The current thread has been interrupted" ) );
						}
						Path entryPath = Paths.get( entry.getName() );
						if ( entryPath.isAbsolute() ) {
							throw new IllegalArgumentException( "Absolute paths in ZIP files are not supported" );
						}

						Path entryAbsolutePath = tempDirectory.resolve( entryPath );
						try ( OutputStream os = new BufferedOutputStream(
								new FileOutputStream( entryAbsolutePath.toFile() ) ) ) {
							final int bufferSize = 1024;
							byte[] buffer = new byte[bufferSize];
							for ( int readBytes = -1; ( readBytes = zis.read( buffer, 0, bufferSize ) ) > -1; ) {
								os.write( buffer, 0, readBytes );
							}
							os.flush();
						}
						extractedFiles.add( entryAbsolutePath );
					}
				}
			}
			finally {
				if ( pathList != null ) {
					pathList.forEach( is -> {
						if ( is != null ) {
							try {
								is.close();
							}
							catch (IOException e) {
								// ignore
							}
						}
					} );
				}
			}
		}

		return extractedFiles;
	}

	/**
	 * Trigger the import of addresses if necessary
	 * 
	 * @param event The context event
	 */
	@EventListener
	@Transactional
	@Order(2)
	public void importAddresses(ContextRefreshedEvent event) {
		if ( this.addressRepository.count() > 0 ) {
			// the addresses are already loaded
			return;
		}

		log.info( "Importing address sample data" );
		try {
			Path addressesCsvFileLocationPath;
			if ( this.useCloudStorage ) {
				if ( this.cloudStorageBucket == null || this.cloudStorageBucket.isEmpty() ) {
					log.warn( "Importing address sample data from cloud storage requires a valid bucket name." );
					return;
				}

				addressesCsvFileLocationPath = getFileFromCloudStorage( "addresses.csv" );
			}
			else {
				URL addressesCsvFileLocationUrl = this.getClass().getResource( this.addressesCsvFileLocation );
				if ( addressesCsvFileLocationUrl == null ) {
					log.warn( "Unable to import address sample data: the file " + this.addressesCsvFileLocation
							+ " does not exist. Please download the addresses data set from https://data.sfgov.org/Geographic-Locations-and-Boundaries/Addresses-Enterprise-Addressing-System/sr5d-tnui" );
					return;
				}
				addressesCsvFileLocationPath = Paths.get( addressesCsvFileLocationUrl.toURI() );
				if ( this.addressesCsvFileLocation.endsWith( ".zip" ) ) {
					List<Path> extractedFiles = extractZip( addressesCsvFileLocationPath );
					if ( extractedFiles.size() != 1 ) {
						throw new IllegalArgumentException( "The ZIP file must contain exactly one CSV file." );
					}
					addressesCsvFileLocationPath = extractedFiles.get( 0 );
				}
			}

			log.info( "Importing address sample data from " + addressesCsvFileLocationPath );

			try ( Stream<String> lines = Files.lines( addressesCsvFileLocationPath, StandardCharsets.UTF_8 ) ) {
				Long importedRecords = Streams.mapWithIndex( lines.skip( 1 ), (line, idx) -> {
					if ( Thread.currentThread().isInterrupted() ) {
						throw new RuntimeException( new InterruptedException( "The current thread has been interrupted" ) );
					}
					List<String> fields = parseLine( line );
					assert fields.size() == 11;

					Address a = new Address( Long.valueOf( fields.get( 0 ) ) );
					a.setCnn( fields.get( 1 ) );
					a.setAddress( fields.get( 2 ) );
					a.setAddressNumber( fields.get( 3 ) );
					a.setAddressNumberSuffix( fields.get( 4 ) );
					a.setStreetName( fields.get( 5 ) );
					a.setStreetType( fields.get( 6 ) );
					a.setZipCode( fields.get( 7 ) );
					try {
						double x = Double.parseDouble( fields.get( 8 ) );
						if ( !isValidXCoordinate( x, a.getBaseID().longValue(), "address" ) ) {
							return Long.valueOf( 0 );
						}
						a.setX( x );
					}
					catch (NumberFormatException e) {
						log.info( "Error parsing x coordinate", e );
					}
					try {
						double y = Double.parseDouble( fields.get( 9 ) );
						if ( !isValidYCoordinate( y, a.getBaseID().longValue(), "address" ) ) {
							return Long.valueOf( 0 );
						}
						a.setY( y );
					}
					catch (NumberFormatException e) {
						log.info( "Error parsing y coordinate", e );
					}
					if ( fields.get( 10 ).isEmpty() ) {
						log.info( "Location for address with ID " + a.getBaseID() + " is empty." );
						a.setLocation( createLocation( a.getX(), a.getY() ) );
					}
					else {
						Matcher matcher = LOCATION_PATTERN.matcher( fields.get( 10 ) );
						if ( matcher.matches() ) {
							a.setLocation( createLocation( Double.parseDouble( matcher.group( 3 ) ),
									Double.parseDouble( matcher.group( 1 ) ) ) );

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
						return Long.valueOf( 0 );
					}

					this.em.persist( a );

					if ( idx % this.jdbcBatchSize == 0 ) {
						this.em.flush();
						this.em.clear();
					}

					return Long.valueOf( 1 );
				} ).collect( Collectors.reducing( Long.valueOf( 0 ), e -> e, Long::sum ) );

				log.info( "Imported " + importedRecords + " addresses." );
			}

		}
		catch (IOException | URISyntaxException e) {
			log.warn( "Error reading addresses file.", e );
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
			if ( Thread.currentThread().isInterrupted() ) {
				throw new RuntimeException( new InterruptedException( "The current thread has been interrupted" ) );
			}
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

	/**
	 * Trigger the transformation of incident locations if necessary
	 * 
	 * @param event The context event
	 */
	// @EventListener
	@Transactional
	@Order(10)
	public void transformIncidentLocations(ContextRefreshedEvent event) {
		Query query = this.em.createQuery( "select count(i) from Incident i where i.mapLocation is null" );
		long count = ( (Long) query.getSingleResult() ).longValue();
		if ( count == 0 ) {
			return;
		}

		log.info( "Transforming incident locations" );

		query = this.em.createNativeQuery( "set transaction autocommit ddl off" );
		query.executeUpdate();

		String tempTableName = UUID.randomUUID().toString();
		query = this.em.createNativeQuery( "create column table \"" + tempTableName + "\" like Incident" );
		query.executeUpdate();

		query = this.em.createNativeQuery( "insert into \"" + tempTableName + "\" (\"PD_ID\"," + "	 \"ADDRESS\","
				+ "	 \"CATEGORY\"," + "	 \"DATE\"," + "	 \"DAY_OF_WEEK\"," + "	 \"DESCRIPTION\","
				+ "	 \"INCIDENT_NUMBER\"," + "	 \"PD_DISTRICT\"," + "	 \"RESOLUTION\"," + "	 \"TIME\"," + "	 \"X\","
				+ "	 \"Y\"," + "	 \"LOCATION\"," + "	 \"MAP_LOCATION\") select \"PD_ID\"," + "	 \"ADDRESS\","
				+ "	 \"CATEGORY\"," + "	 \"DATE\"," + "	 \"DAY_OF_WEEK\"," + "	 \"DESCRIPTION\","
				+ "	 \"INCIDENT_NUMBER\"," + "	 \"PD_DISTRICT\"," + "	 \"RESOLUTION\"," + "	 \"TIME\"," + "	 \"X\","
				+ "	 \"Y\"," + "	 \"LOCATION\"," + "	 \"LOCATION\".ST_Transform(7131) from Incident;" );
		query.executeUpdate();

		query = this.em.createNativeQuery( "drop table Incident" );
		query.executeUpdate();

		query = this.em.createNativeQuery( "rename table \"" + tempTableName + "\" to Incident" );
		query.executeUpdate();

		log.info( "Transforming incident locations . . . ok" );
	}

	/**
	 * Trigger the creation of incident locations if necessary
	 * 
	 * @param event The context event
	 */
	@EventListener
	@Transactional
	@Order(5)
	public void setIncidentLocations(ContextRefreshedEvent event) {
		Query query = this.em.createQuery( "select count(i) from Incident i where i.location is null" );
		long count = ( (Long) query.getSingleResult() ).longValue();
		if ( count == 0 ) {
			return;
		}

		log.info( "Creating incident locations" );

		query = this.em.createNativeQuery(
				"update Incident i set i.location=ST_GeomFromEWKT('SRID=4326;POINT(' || i.x || ' ' || i.y || ')')" );
		count = query.executeUpdate();

		log.info( "Created " + count + " incident locations" );

		log.info( "Creating incident locations . . . ok" );
	}

	/**
	 * Trigger the creation of address locations if necessary
	 * 
	 * @param event The context event
	 */
	@EventListener
	@Transactional
	@Order(5)
	public void setAddressLocations(ContextRefreshedEvent event) {
		Query query = this.em.createQuery( "select count(a) from Address a where a.location is null" );
		long count = ( (Long) query.getSingleResult() ).longValue();
		if ( count == 0 ) {
			return;
		}

		log.info( "Creating address locations" );

		query = this.em.createNativeQuery(
				"update Address a set a.location=ST_GeomFromEWKT('SRID=4326;POINT(' || a.x || ' ' || a.y || ')')" );
		count = query.executeUpdate();

		log.info( "Created " + count + " address locations" );

		log.info( "Creating address locations . . . ok" );
	}

	private boolean isValidXCoordinate(double x, long id, String type) {
		// Check bounds of SRS 7131 (see http://epsg.io/7131)
		if ( x < -123.56 || x > -121.2 ) {
			// ignore if out of bounds
			log.info( "X coordinate  of " + type + " with ID " + id + " is out of bounds: " + x );
			return false;
		}

		return true;
	}

	private boolean isValidYCoordinate(double y, long id, String type) {
		// Check bounds of SRS 7131 (see http://epsg.io/7131)
		if ( y < 36.85 || y > 38.87 ) {
			// ignore if out of bounds
			log.info( "Y coordinate  of " + type + " with ID " + id + " is out of bounds: " + y );
			return false;
		}

		return true;
	}

	private Point<G2D> createLocation(double x, double y) {
		return (Point<G2D>) WKT_DECODER.decode( "SRID=4326;POINT(" + x + " " + y + ")" );
	}
}
