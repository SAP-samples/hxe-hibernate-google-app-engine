package com.sap.hana.hibernate.sample.repositories;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.geolatte.geom.G2D;
import org.geolatte.geom.GeometryType;
import org.geolatte.geom.Point;
import org.springframework.data.geo.Distance;
import org.springframework.stereotype.Repository;

import com.sap.hana.hibernate.sample.entities.IncidentCluster;

@Repository
public class IncidentClusterRepository extends AbstractRepository {

	/**
	 * Returns the list of incident clusters matching the given criteria
	 * 
	 * @param location The location around which clusters are to be found
	 * @param distance The maximum distance of the incident clusters around the location
	 * @param dateFrom The date of the oldest incident
	 * @param dateTo The date of the most recent incident
	 * @param categoryList The categories of the incidents
	 * @return the list of incident clusters matching the given criteria
	 */
	public List<IncidentCluster> findClusters(Point<G2D> location, Distance distance, Date dateFrom, Date dateTo, List<String> categoryList) {
		List<IncidentCluster> resultList = new ArrayList<>();
		
		

		return resultList;
	}
}
