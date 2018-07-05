package com.sap.hana.hibernate.sample.repositories;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.geolatte.geom.G2D;
import org.geolatte.geom.Point;
import org.springframework.data.geo.Distance;
import org.springframework.stereotype.Repository;

import com.sap.hana.hibernate.sample.entities.IncidentCluster;

@Repository
public class IncidentClusterRepository extends AbstractRepository {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger( IncidentClusterRepository.class );

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
	@Transactional
	public List<IncidentCluster> findClusters(Point<G2D> location, Distance distance, Date dateFrom, Date dateTo, List<String> category) {
		List<IncidentCluster> resultList = new ArrayList<>();

		return resultList;
	}
}
