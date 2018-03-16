package com.sap.hana.hibernate.sample.repositories;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
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
		javax.persistence.Query query;

		final UUID uuid = UUID.randomUUID();
		final String tableName = "#" + uuid.toString();

		final List<Incident> resultList;
		long resultCount;
		try {
			query = this.em.createNativeQuery(
					"create local temporary column table \"" + tableName + "\" (X DOUBLE, Y DOUBLE, C BIGINT)" );
			query.executeUpdate();

			if ( category == null || category.isEmpty() ) {
				query = this.em.createNativeQuery(
						"insert into \"" + tableName + "\" (X, Y, C) "
								+ "select i.location.ST_X(), i.location.ST_Y(), count(*) "
								+ "from Incident i "
								+ "where i.date between :dateFrom and :dateTo "
								+ "  and i.location.ST_WithinDistance(ST_GeomFromEWKB(:location), :distance) = 1 "
								+ "group by i.location" );
			}
			else {
				query = this.em.createNativeQuery(
						"insert into \"" + tableName + "\" (X, Y, C) "
								+ "select i.location.ST_X(), i.location.ST_Y(), count(*) "
								+ "from Incident i "
								+ "where i.category in :category "
								+ "  and i.date between :dateFrom and :dateTo "
								+ "  and i.location.ST_WithinDistance(ST_GeomFromEWKB(:location), :distance) = 1 "
								+ "group by i.location" );
				query.setParameter( "category", category );
			}

			query.setParameter( "dateFrom", dateFrom );
			query.setParameter( "dateTo", dateTo );
			query.setParameter( "distance", distance );
			query.setParameter( "location", location );

			resultCount = query.executeUpdate();

			if ( resultCount == 0 ) {
				resultList = Collections.emptyList();
			}
			else {
				if ( category == null || category.isEmpty() ) {
					query = this.em.createNativeQuery(
							"select * from Incident i "
									+ "right outer join \"" + tableName + "\" t on i.x=t.x and i.y=t.y "
									+ "where i.date between :dateFrom and :dateTo "
									+ "order by i.date desc",
							Incident.class );
				}
				else {
					query = this.em.createNativeQuery(
							"select * from Incident i "
									+ "right outer join \"" + tableName + "\" t on i.x=t.x and i.y=t.y "
									+ "where i.category in :category "
									+ "  and  i.date between :dateFrom and :dateTo "
									+ "order by i.date desc",
							Incident.class );
					query.setParameter( "category", category );
				}

				query.setParameter( "dateFrom", dateFrom );
				query.setParameter( "dateTo", dateTo );
				query.setFirstResult( (int) pageable.getOffset() );
				query.setMaxResults( pageable.getPageSize() );

				resultList = query.getResultList();

				query = this.em.createNativeQuery( "select sum(c) from \"" + tableName + "\"" );
				resultCount = ( (BigInteger) query.getSingleResult() ).longValue();
			}
		}
		finally {
			query = this.em.createNativeQuery( "drop table \"" + tableName + "\" cascade" );
			query.executeUpdate();
		}

		return new PageImpl<Incident>( resultList, pageable, resultCount );
	}
}
