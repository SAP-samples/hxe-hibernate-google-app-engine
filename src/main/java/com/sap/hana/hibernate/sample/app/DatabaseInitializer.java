package com.sap.hana.hibernate.sample.app;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

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

	private static final SimpleDateFormat SDF = new SimpleDateFormat( "MM/dd/yyyy" );

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
				Path incidentsCsvFileLocationPath = Paths.get( incidentsCsvFileLocationUrl.toURI() );
				if ( this.incidentsCsvFileLocation.matches( ".+\\.zip(\\.001)?$" ) ) {
					List<Path> extractedFiles = extractZip( incidentsCsvFileLocationPath );
					if ( extractedFiles.size() != 1 ) {
						throw new IllegalArgumentException( "The ZIP file must contain exactly one CSV file." );
					}
					incidentsCsvFileLocationPath = extractedFiles.get( 0 );
				}

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

						Matcher matcher = LOCATION_PATTERN.matcher( fields.get( 11 ) );
						if ( matcher.matches() ) {
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
		}
		catch (IOException | URISyntaxException e) {
			log.warn( "Error reading incidents file.", e );
		}

		log.info( "Importing incident sample data . . . ok" );
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
								// regular entry header => close the stream and return new stream starting from the
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
				try ( ZipInputStream zis = new ZipInputStream( new SequenceInputStream( Collections.enumeration( pathList ) ) ) ) {
					ZipEntry entry = null;
					while ( ( entry = zis.getNextEntry() ) != null ) {
						if ( Thread.currentThread().isInterrupted() ) {
							throw new RuntimeException( new InterruptedException( "The current thread has been interrupted" ) );
						}
						Path entryPath = Paths.get( entry.getName() );
						if ( entryPath.isAbsolute() ) {
							throw new IllegalArgumentException( "Absolute paths in ZIP files are not supported" );
						}

						Path entryAbsolutePath = tempDirectory.resolve( entryPath );
						try ( OutputStream os = new BufferedOutputStream( new FileOutputStream( entryAbsolutePath.toFile() ) ) ) {
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
				Path addressesCsvFileLocationPath = Paths.get( addressesCsvFileLocationUrl.toURI() );
				if ( this.addressesCsvFileLocation.endsWith( ".zip" ) ) {
					List<Path> extractedFiles = extractZip( addressesCsvFileLocationPath );
					if ( extractedFiles.size() != 1 ) {
						throw new IllegalArgumentException( "The ZIP file must contain exactly one CSV file." );
					}
					addressesCsvFileLocationPath = extractedFiles.get( 0 );
				}

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
						Matcher matcher = LOCATION_PATTERN.matcher( fields.get( 10 ) );
						if ( matcher.matches() ) {
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

	private boolean isValidXCoordinate(double x, long id, String type) {
		if ( x < -123 || x > -122 ) {
			// ignore if out of bounds
			log.info( "X coordinate  of " + type + " with ID " + id + " is out of bounds: " + x );
			return false;
		}

		return true;
	}

	private boolean isValidYCoordinate(double y, long id, String type) {
		if ( y < 37 || y > 38 ) {
			// ignore if out of bounds
			log.info( "Y coordinate  of " + type + " with ID " + id + " is out of bounds: " + y );
			return false;
		}

		return true;
	}
}
