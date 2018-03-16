package com.sap.hana.hibernate.sample.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sap.hana.hibernate.sample.entities.Address;
import com.sap.hana.hibernate.sample.repositories.AddressRepository;
import com.sap.hana.hibernate.sample.util.Constants;

@RestController
public class AddressRestController {

	@Autowired
	private AddressRepository repository;

	/**
	 * Return the list of addresses matching the given address
	 * 
	 * @param address The address to search for
	 * @return The list of addresses matching the given address
	 */
	@RequestMapping(path = Constants.ADDRESS_API_PATH, produces = "application/json", method = RequestMethod.GET)
	public List<Address> findByAddressContaining(@RequestParam("address") String address) {
		return this.repository.findByAddressContaining( address );
	}
}
