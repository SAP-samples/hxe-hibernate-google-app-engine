package com.sap.hana.hibernate.sample.repositories;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public abstract class AbstractRepository {

	@PersistenceContext
	protected EntityManager em;

	/**
	 * Returns the list of available incident categories
	 * 
	 * @return the list of available incident categories
	 */
	public List<String> findCategories() {
		Query query = this.em.createQuery( "select distinct(i.category) from Incident i" );
		@SuppressWarnings("unchecked")
		List<String> resultList = query.getResultList();
		return resultList;
	}

	/**
	 * Returns the date of the oldest incident
	 * 
	 * @return the date of the oldest incident
	 */
	public Date findMinDate() {
		Query query = this.em.createQuery( "select min(i.date) from Incident i" );
		return (Date) query.getSingleResult();
	}

	/**
	 * Returns the total number of incidents
	 * 
	 * @return the total number of incidents
	 */
	public long count() {
		Query query = this.em.createQuery( "select count(i) from Incident i" );
		return ( (Long) query.getSingleResult() ).longValue();
	}
}
