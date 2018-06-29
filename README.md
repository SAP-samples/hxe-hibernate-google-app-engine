# Description
This project is a demo application illustrating how to build Java applications with [Spring](https://spring.io/) and [Hibernate](http://hibernate.org/) on SAP HANA. It can serve as a starting point for developing other Java applications or simply provide insights on how to set up an application and how to run queries against the database.

In addition to standard SQL queries this application also contains examples for geospatial queries. The geospatial queries are sent to the database via the HANA Hibernate Spatial dialect and executed by SAP HANA's geospatial engine.

# Requirements
## Hardware
* A computer that meets the [Machine Requirements](https://help.sap.com/viewer/32c9e0c8afba4c87814e61d6a1141280/2.0.02/en-US/c3807913b0a340a99822bf0d97a01da6.html) for SAP HANA, Express Edition

## Software
* A Java runtime environment for Java 8 or greater
* [Apache Maven](http://maven.apache.org/download.cgi)
* A SAP HANA database instance, for example, a SAP HANA, Express Edition instance which can be obtained via the [SAP HANA, Express Edition download page](https://www.sap.com/cmp/ft/crm-xu16-dat-hddedft/index.html)
* A [Google Cloud Platform](https://cloud.google.com/) account for deploying the application to the cloud. 
* A [Google Cloud Platform API key](https://cloud.google.com/docs/authentication/api-keys) for using the [Google Maps Javascript API](https://developers.google.com/maps/documentation/javascript/) and the [Google Cloud Translation API](https://cloud.google.com/translate/docs/reference/libraries#client-libraries-install-java).
* The [San Francisco Police Department Incidents](https://data.sfgov.org/Public-Safety/-Change-Notice-Police-Department-Incidents/tmnf-yvry) data set as [CSV](https://data.sfgov.org/api/views/tmnf-yvry/rows.csv?accessType=DOWNLOAD)
* The [San Francisco Addresses](https://data.sfgov.org/Geographic-Locations-and-Boundaries/Addresses-Enterprise-Addressing-System/sr5d-tnui) data set as [CSV](https://data.sfgov.org/api/views/sr5d-tnui/rows.csv?accessType=DOWNLOAD)

# Download and Installation
## Download
Clone this repository to your local computer.

## Installation
> NOTE: all relative paths are relative to the project root directory. All commands are expected to be run from the project root directory.

### Copy the data sets
Place the incidents and addresses data sets into the `src/main/resources/csv` directory as `incidents.csv` and `addresses.csv`, respectively. If you prefer a different location or different file names you can adjust the configuration properties `csv.incidents.file.location` and `csv.addresses.file.location` in the file `src/main/resources/application.properties`.
### Add the API key
Edit the file `pom.xml` and add your Google Cloud Platform API key as the value of the property `google.api.key`.

```
...
<properties>
	...
	<google.api.key>REPLACE_WITH_YOUR_API_KEY</google.api.key>
	...
</properties>
...
```

### Database setup
The database tables used for storing the application data are automatically created by the application using the SQL script located at `src/main/resources/db/migration/V1__CreateSchema.sql`. 


### Adjust the JDBC connection data
Edit the file `pom.xml` and adjust the properties `jdbc.username`, `jdbc.password`, `jdbc.host`, and `jdbc.port` to match the connection data of your SAP HANA instance.

```
...
<properties>
	...
		<jdbc.username>DEMO_USER</jdbc.username>
		<jdbc.password>D3m0-U$eR</jdbc.password>
		<jdbc.host>HOSTNAME OR IP</jdbc.host>
		<jdbc.port>SQL PORT</jdbc.port>
	...
</properties>
...
```

For local testing make sure to also adjust these properties in the `m2e` profile.

```
...
<profiles>
	<profile>
		<id>m2e</id>
		...
		<properties>
			...
				<jdbc.username>DEMO_USER</jdbc.username>
				<jdbc.password>D3m0-U$eR</jdbc.password>
				<jdbc.host>HOSTNAME OR IP</jdbc.host>
				<jdbc.port>SQL PORT</jdbc.port>
			...
		</properties>
	</profile>
</profiles>
...
```

### Run the application locally
You can run the application by executing the following Maven command from the application's root directory:

```
> mvn spring-boot:run
```

If you have separated the configuration into a default cloud configuration and a local configuration using the `m2e` profile, you have to specify the profile ID when running the Maven command:

```
> mvn spring-boot:run -P m2e
```

> NOTE: the first startup of the application can take a while, because the applications needs to load all the data from the CSV files. Subsequent startups will be much faster.

Once the application has started you can access it by navigating to [http://localhost:8080](http://localhost:8080]) in a web browser.

### Deploy the application to the Google Cloud Platform
Deploying the application to the cloud can be done using the configured [Google App Engine Maven Plugin](https://cloud.google.com/appengine/docs/standard/java/tools/maven).

Before the application can be deployed to the cloud you must [create a GCP project](https://cloud.google.com/appengine/docs/standard/java/console/) and add it's ID to the Maven build descriptor. To do this, edit the file `pom.xml` and replace the value of the property `google.project.id` with your actual project ID.

```
...
<properties>
	...
		<google.project.id>REPLACE_WITH_YOUR_PROJECT_ID</google.project.id>
	...
</properties>
...
```

After that you can deploy the application to the cloud by running the following Maven command:

```
> mvn appengine:update
```

Once the application has been deployed you can access it by navigating to `http://<your-project-id>.appspot.com` in a web browser.

# Configuration
The application can be configured using the properties in the file `src/main/resources/application.properties`.

## Google Cloud Translation API
Translating the application UI using the Google Cloud Translation API is disabled by default. It can be enabled by setting the property `translation.enabled` to `true`.

```
translation.enabled=true
```

## CSV file locations
The locations and names of the CSV files containing the incident and address data can be configured by setting the properties `csv.incidents.file.location` and `csv.addresses.file.location`, respectively.

```
csv.incidents.file.location=/csv/incidents.csv
csv.addresses.file.location=/csv/addresses.csv
```

## JDBC batch size
If the database tables are empty, the application tries to load the data from the specified CSV files. Since there are many records to be imported, the import can be significantly sped up by using batch inserts. The size of a batch can be controlled by setting the property `spring.jpa.properties.hibernate.jdbc.batch_size`.

```
spring.jpa.properties.hibernate.jdbc.batch_size=50
```

The default batch size is 50, but you may want to experiment with larger values for the initial load.

## Debugging SQL statements
If you want to debug the SQL statements that are sent to the database you can set the property `spring.jpa.show-sql` to `true`. Then the SQL statements are logged to the configured logger. If you also want to see the values of the statement parameters that are sent to or retrieved from the database you can set the logging level of `org.hibernate.type.descriptor.sql.BasicBinder` and `org.hibernate.type.descriptor.sql.BasicExtractor` to `info`.

```
spring.jpa.show-sql=true

logging.level.org.hibernate.type.descriptor.sql.BasicBinder=info
logging.level.org.hibernate.type.descriptor.sql.BasicExtractor=info
```

# Limitations
There are currently no limitations

# Known Issues
There are currently no known issues

# How to obtain support
This project is provided as-is. New features may be added occasionally.

There is no support for this project. You may raise issues via the [issue tracker](https://github.com/SAP/hxe-hibernate-google-app-engine/issues), but there is no guarantee they will be processed.

# To-Do (upcoming changes)
## Address search using SAP HANA full-text search
Currently the address search implementation uses standard SQL LIKE queries. This has several drawbacks:
* No ordering of results
* Only exact matches

These drawbacks can be addressed by using SAP HANA fuzzy text search.

# License
Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
This file is licensed under the Apache Software License, v. 2 except as noted otherwise in the [LICENSE](LICENSE) file