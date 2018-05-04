package com.sap.hana.hibernate.sample.controllers;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sap.hana.hibernate.sample.entities.IncidentLocationAndCount;
import com.sap.hana.hibernate.sample.repositories.IncidentLocationAndCountRepository;
import com.sap.hana.hibernate.sample.util.Constants;

@RestController
public class IncidentLocationAndCountRestController {

	@Autowired
	private IncidentLocationAndCountRepository repository;

	/**
	 * Return the number of incidents per location matching the given criteria
	 * 
	 * @param location The location of the incidents
	 * @param distance The maximum distance of the incidents around the location
	 * @param dateFrom The date of the oldest incident
	 * @param dateTo The date of the most recent incident
	 * @param category The categories of the incident
	 * @return A list of locations matching the criteria and the associated number of incidents
	 */
	@RequestMapping(path = Constants.INCIDENT_LOCATION_AND_COUNT_WITH_CATEGORY_API_PATH, produces = "application/json", method = RequestMethod.GET)
	List<IncidentLocationAndCount> findByLocationAndCategoryWithWeight(@RequestParam("location") Point location,
			@RequestParam("distance") Distance distance, @RequestParam("dateFrom") Date dateFrom,
			@RequestParam("dateTo") Date dateTo, @RequestParam("category") List<String> category) {
		return this.repository.findByLocationAndCategory( location, distance, dateFrom, dateTo, category );
	}

	/**
	 * Return the number of incidents per location matching the given criteria
	 * 
	 * @param location The location of the incidents
	 * @param distance The maximum distance of the incidents around the location
	 * @param dateFrom The date of the oldest incident
	 * @param dateTo The date of the most recent incident
	 * @return A list of locations matching the criteria and the associated number of incidents
	 */
	@RequestMapping(path = Constants.INCIDENT_LOCATION_AND_COUNT_API_PATH, produces = "application/json", method = RequestMethod.GET)
	List<IncidentLocationAndCount> findByLocationWithWeight(@RequestParam("location") Point location,
			@RequestParam("distance") Distance distance, @RequestParam("dateFrom") Date dateFrom,
			@RequestParam("dateTo") Date dateTo) {
		return this.repository.findByLocationAndCategory( location, distance, dateFrom, dateTo, null );
	}
}
