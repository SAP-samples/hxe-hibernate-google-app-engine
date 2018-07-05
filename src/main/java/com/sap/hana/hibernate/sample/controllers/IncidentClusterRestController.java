package com.sap.hana.hibernate.sample.controllers;

import java.util.Date;
import java.util.List;

import org.geolatte.geom.G2D;
import org.geolatte.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Distance;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sap.hana.hibernate.sample.entities.IncidentCluster;
import com.sap.hana.hibernate.sample.repositories.IncidentClusterRepository;
import com.sap.hana.hibernate.sample.util.Constants;

@RestController
public class IncidentClusterRestController {

	@Autowired
	private IncidentClusterRepository repository;

	/**
	 * Returns the list of incident clusters matching the given criteria
	 * 
	 * @param location The location around which clusters are to be found
	 * @param distance The maximum distance of the incident clusters around the location
	 * @param dateFrom The date of the oldest incident
	 * @param dateTo The date of the most recent incident
	 * @return the list of incident clusters matching the given criteria
	 */
	@RequestMapping(path = Constants.CLUSTER_API_PATH, produces = "application/json", method = RequestMethod.GET)
	List<IncidentCluster> findClusters(@RequestParam("location") Point<G2D> location,
			@RequestParam("distance") Distance distance, @RequestParam("dateFrom") Date dateFrom,
			@RequestParam("dateTo") Date dateTo) {
		return this.repository.findClusters( location, distance, dateFrom, dateTo, null );
	}

	/**
	 * Returns the list of incident clusters matching the given criteria
	 * 
	 * @param location The location around which clusters are to be found
	 * @param distance The maximum distance of the incident clusters around the location
	 * @param dateFrom The date of the oldest incident
	 * @param dateTo The date of the most recent incident
	 * @param category The categories of the incidents
	 * @return the list of incident clusters matching the given criteria
	 */
	@RequestMapping(path = Constants.CLUSTER_WITH_CATEGORY_API_PATH, produces = "application/json", method = RequestMethod.GET)
	List<IncidentCluster> findClusters(@RequestParam("location") Point<G2D> location,
			@RequestParam("distance") Distance distance, @RequestParam("dateFrom") Date dateFrom,
			@RequestParam("dateTo") Date dateTo, @RequestParam("category") List<String> category) {
		return this.repository.findClusters( location, distance, dateFrom, dateTo, category );
	}
}
