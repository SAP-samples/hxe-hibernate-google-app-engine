package com.sap.hana.hibernate.sample.controllers;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sap.hana.hibernate.sample.entities.Incident;
import com.sap.hana.hibernate.sample.repositories.IncidentRepository;
import com.sap.hana.hibernate.sample.util.Constants;

@RestController
public class IncidentRestController {

	@Autowired
	private IncidentRepository repository;

	/**
	 * Returns the list of incidents matching the given criteria
	 * 
	 * @param location The location of the incidents
	 * @param distance The maximum distance of the incidents around the location
	 * @param dateFrom The date of the oldest incident
	 * @param dateTo The date of the most recent incident
	 * @param page The page number
	 * @param size The page size
	 * @return the list of incidents matching the given criteria
	 */
	@RequestMapping(path = Constants.INCIDENT_API_PATH, produces = "application/json", method = RequestMethod.GET)
	Page<Incident> findByLocationNear(@RequestParam("location") Point location,
			@RequestParam("distance") Distance distance, @RequestParam("dateFrom") Date dateFrom,
			@RequestParam("dateTo") Date dateTo, @RequestParam("page") int page, @RequestParam("size") int size) {
		return this.repository.findByLocationNear( location, distance, dateFrom, dateTo, null,
				PageRequest.of( page, size ) );
	}

	/**
	 * Returns the list of incidents matching the given criteria
	 * 
	 * @param location The location of the incidents
	 * @param distance The maximum distance of the incidents around the location
	 * @param dateFrom The date of the oldest incident
	 * @param dateTo The date of the most recent incident
	 * @param category The categories of the incident
	 * @param page The page number
	 * @param size The page size
	 * @return the list of incidents matching the given criteria
	 */
	@RequestMapping(path = Constants.INCIDENT_WITH_CATEGORY_API_PATH, produces = "application/json", method = RequestMethod.GET)
	Page<Incident> findByLocationNear(@RequestParam("location") Point location,
			@RequestParam("distance") Distance distance, @RequestParam("dateFrom") Date dateFrom,
			@RequestParam("dateTo") Date dateTo, @RequestParam("category") List<String> category,
			@RequestParam("page") int page, @RequestParam("size") int size) {
		return this.repository.findByLocationNear( location, distance, dateFrom, dateTo, category,
				PageRequest.of( page, size ) );
	}
}
