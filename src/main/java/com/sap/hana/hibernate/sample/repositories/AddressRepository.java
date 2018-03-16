package com.sap.hana.hibernate.sample.repositories;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;

import com.sap.hana.hibernate.sample.entities.Address;

@Repository
public class AddressRepository {

	@PersistenceContext
	private EntityManager em;

	/**
	 * Return the list of addresses matching the given address
	 * 
	 * @param address The address to search for
	 * @return The list of addresses matching the given address
	 */
	public List<Address> findByAddressContaining(String address) {
		TypedQuery<Address> query = this.em
				.createQuery( "select a from Address a where upper(a.address) like upper(:address)", Address.class );

		query.setParameter( "address", "%" + address + "%" );

		query.setMaxResults( 20 );

		List<Address> addresses = query.getResultList();

		return addresses;
	}

	/**
	 * Returns the total number of addresses
	 * 
	 * @return the total number of addresses
	 */
	public long count() {
		Query query = this.em.createQuery( "select count(a) from Address a" );
		return ( (Long) query.getSingleResult() ).longValue();
	}
}
