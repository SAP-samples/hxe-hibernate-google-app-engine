package com.sap.hana.hibernate.sample.repositories;

import java.util.Date;
import java.util.List;

import javax.persistence.TypedQuery;

import org.geolatte.geom.G2D;
import org.geolatte.geom.Point;
import org.springframework.data.geo.Distance;
import org.springframework.stereotype.Repository;

import com.sap.hana.hibernate.sample.entities.IncidentLocationAndCount;

@Repository
public class IncidentLocationAndCountRepository extends AbstractRepository {

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
	public List<IncidentLocationAndCount> findByLocationAndCategory(Point<G2D> location, Distance distance,
			Date dateFrom, Date dateTo, List<String> category) {
		TypedQuery<IncidentLocationAndCount> query;
		if ( category == null || category.isEmpty() ) {
			query = this.em.createQuery(
					"select new com.sap.hana.hibernate.sample.entities.IncidentLocationAndCount(i.x, i.y, count(*)) "
							+ "from Incident i "
							+ "where i.date between :dateFrom and :dateTo "
							+ "  and i.x between (cast(substring(:location, 0, locate(',', :location)-1) as double) - cast(:distance as double)/111319) "
							+ "    and (cast(substring(:location, 0, locate(',', :location)-1) as double) + cast(:distance as double)/111319) "
							+ "  and i.y between (cast(substring(:location, locate(',', :location)+1) as double) - cast(:distance as double)/111319) "
							+ "    and (cast(substring(:location, locate(',', :location)+1) as double) + cast(:distance as double)/111319) "
							+ "group by i.x, i.y",
					IncidentLocationAndCount.class );
		}
		else {
			query = this.em.createQuery(
					"select new com.sap.hana.hibernate.sample.entities.IncidentLocationAndCount(i.x, i.y, count(*)) "
							+ "from Incident i "
							+ "where i.category in :category "
							+ "  and i.date between :dateFrom and :dateTo "
							+ "  and i.x between (cast(substring(:location, 0, locate(',', :location)-1) as double) - cast(:distance as double)/111319) "
							+ "    and (cast(substring(:location, 0, locate(',', :location)-1) as double) + cast(:distance as double)/111319) "
							+ "  and i.y between (cast(substring(:location, locate(',', :location)+1) as double) - cast(:distance as double)/111319) "
							+ "    and (cast(substring(:location, locate(',', :location)+1) as double) + cast(:distance as double)/111319) "
							+ "group by i.x, i.y",
					IncidentLocationAndCount.class );
			query.setParameter( "category", category );
		}

		query.setParameter( "dateFrom", dateFrom );
		query.setParameter( "dateTo", dateTo );
		query.setParameter( "location", location );
		query.setParameter( "distance", distance );

		return query.getResultList();
	}

}
