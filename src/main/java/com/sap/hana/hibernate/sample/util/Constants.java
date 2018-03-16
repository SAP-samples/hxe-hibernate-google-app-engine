package com.sap.hana.hibernate.sample.util;

public class Constants {

	public static final String API_PREFIX = "/api";

	public static final String ADDRESS_API_PATH = API_PREFIX + "/address";

	public static final String ADDRESS_API_PATH_TEMPLATE = ADDRESS_API_PATH + "{?address}";

	public static final String INCIDENT_API_PATH = API_PREFIX + "/incident";

	public static final String INCIDENT_API_PATH_TEMPLATE = INCIDENT_API_PATH + "{?location,distance,dateFrom,dateTo,page,size}";

	public static final String INCIDENT_WITH_CATEGORY_API_PATH = API_PREFIX + "/incidentWithCategory";

	public static final String INCIDENT_WITH_CATEGORY_API_PATH_TEMPLATE = INCIDENT_WITH_CATEGORY_API_PATH
			+ "{?location,distance,dateFrom,dateTo,category,page,size}";

	public static final String INCIDENT_LOCATION_AND_COUNT_API_PATH = API_PREFIX + "/incidentLocationAndCounts";

	public static final String INCIDENT_LOCATION_AND_COUNT_API_PATH_TEMPLATE = INCIDENT_LOCATION_AND_COUNT_API_PATH + "{?location,distance,dateFrom,dateTo}";

	public static final String INCIDENT_LOCATION_AND_COUNT_WITH_CATEGORY_API_PATH = API_PREFIX + "/incidentLocationAndCountsWithCategory";

	public static final String INCIDENT_LOCATION_AND_COUNT_WITH_CATEGORY_API_PATH_TEMPLATE = INCIDENT_LOCATION_AND_COUNT_WITH_CATEGORY_API_PATH
			+ "{?location,distance,dateFrom,dateTo,category}";
}
