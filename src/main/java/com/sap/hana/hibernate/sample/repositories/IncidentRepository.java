package com.sap.hana.hibernate.sample.repositories;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;

import org.geolatte.geom.G2D;
import org.geolatte.geom.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.geo.Distance;
import org.springframework.stereotype.Repository;

import com.sap.hana.hibernate.sample.entities.Incident;

@Repository
public class IncidentRepository extends AbstractRepository {

	@PersistenceContext
	private EntityManager em;

	/**
	 * Returns the list of incidents matching the given criteria
	 * 
	 * @param location The location of the incidents
	 * @param distance The maximum distance of the incidents around the location
	 * @param dateFrom The date of the oldest incident
	 * @param dateTo The date of the most recent incident
	 * @param category The categories of the incident
	 * @param pageable The page information
	 * @return the list of incidents matching the given criteria
	 */
	@Transactional
	public Page<Incident> findByLocationNear(Point<G2D> location, Distance distance, Date dateFrom, Date dateTo,
			List<String> category, Pageable pageable) {
		TypedQuery<Incident> query;
		if ( category == null || category.isEmpty() ) {
			query = this.em.createQuery(
					"select i from Incident i "
							+ "where i.date between :dateFrom and :dateTo "
							+ "  and i.x between (x(:location) - (cast(:distance as double) / 111319)) "
							+ "    and (x(:location) + (cast(:distance as double) / 111319)) "
							+ "  and i.y between (y(:location) - (cast(:distance as double) / 111319)) "
							+ "    and (y(:location) + (cast(:distance as double) / 111319)) "
							+ "order by i.date desc",
					Incident.class );
		}
		else {
			query = this.em.createQuery(
					"select i from Incident i "
							+ "where i.category in :category "
							+ "  and i.date between :dateFrom and :dateTo "
							+ "  and i.x between (x(:location) - (cast(:distance as double) / 111319)) "
							+ "    and (x(:location) + (cast(:distance as double) / 111319)) "
							+ "  and i.y between (y(:location) - (cast(:distance as double) / 111319)) "
							+ "    and (y(:location) + (cast(:distance as double) / 111319)) "
							+ "order by i.date desc",
					Incident.class );
			query.setParameter( "category", category );
		}

		query.setParameter( "dateFrom", dateFrom );
		query.setParameter( "dateTo", dateTo );
		query.setParameter( "location", location );
		query.setParameter( "distance", distance );

		query.setFirstResult( (int) pageable.getOffset() );
		query.setMaxResults( pageable.getPageSize() );

		Query countQuery;
		if ( category == null || category.isEmpty() ) {
			countQuery = this.em.createQuery(
					"select count(i) from Incident i "
							+ "where i.date between :dateFrom and :dateTo "
							+ "  and i.x between (x(:location) - (cast(:distance as double) / 111319)) "
							+ "    and (x(:location) + (cast(:distance as double) / 111319)) "
							+ "  and i.y between (y(:location) - (cast(:distance as double) / 111319)) "
							+ "    and (y(:location) + (cast(:distance as double) / 111319)) " );

		}
		else {
			countQuery = this.em.createQuery(
					"select count(i) from Incident i "
							+ "where i.category in :category "
							+ "  and i.date between :dateFrom and :dateTo "
							+ "  and i.x between (x(:location) - (cast(:distance as double) / 111319)) "
							+ "    and (x(:location) + (cast(:distance as double) / 111319)) "
							+ "  and i.y between (y(:location) - (cast(:distance as double) / 111319)) "
							+ "    and (y(:location) + (cast(:distance as double) / 111319)) " );
			countQuery.setParameter( "category", category );
		}

		countQuery.setParameter( "dateFrom", dateFrom );
		countQuery.setParameter( "dateTo", dateTo );
		countQuery.setParameter( "location", location );
		countQuery.setParameter( "distance", distance );

		long count = ( (Long) countQuery.getSingleResult() ).longValue();

		return new PageImpl<>( query.getResultList(), pageable, count );
	}
}
