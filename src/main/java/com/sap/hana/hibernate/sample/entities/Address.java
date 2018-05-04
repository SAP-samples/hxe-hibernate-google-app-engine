package com.sap.hana.hibernate.sample.entities;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.springframework.data.geo.Point;

@Entity
public class Address {

	@Id
	private Long baseId;
	private String cnn;
	private String address;
	private String addressNumber;
	private String addressNumberSuffix;
	private String streetName;
	private String streetType;
	private String zipCode;
	private double x = Double.NaN;
	private double y = Double.NaN;
	private Point location;

	@SuppressWarnings("unused")
	private Address() {
	}

	public Address(Long id) {
		this.baseId = id;
	}

	public Long getBaseID() {
		return this.baseId;
	}

	public String getCnn() {
		return this.cnn;
	}

	public void setCnn(String cnn) {
		this.cnn = cnn;
	}

	public String getAddress() {
		return this.address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getAddressNumber() {
		return this.addressNumber;
	}

	public void setAddressNumber(String addressNumber) {
		this.addressNumber = addressNumber;
	}

	public String getAddressNumberSuffix() {
		return this.addressNumberSuffix;
	}

	public void setAddressNumberSuffix(String addressNumberSuffix) {
		this.addressNumberSuffix = addressNumberSuffix;
	}

	public String getStreetName() {
		return this.streetName;
	}

	public void setStreetName(String streetName) {
		this.streetName = streetName;
	}

	public String getStreetType() {
		return this.streetType;
	}

	public void setStreetType(String streetType) {
		this.streetType = streetType;
	}

	public String getZipCode() {
		return this.zipCode;
	}

	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}

	public double getX() {
		return this.x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return this.y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public Point getLocation() {
		return this.location;
	}

	public void setLocation(Point location) {
		this.location = location;
	}

}
