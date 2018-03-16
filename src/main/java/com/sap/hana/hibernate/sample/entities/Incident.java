package com.sap.hana.hibernate.sample.entities;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.geolatte.geom.G2D;
import org.geolatte.geom.Point;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.sap.hana.hibernate.sample.util.PointDeserializer;
import com.sap.hana.hibernate.sample.util.PointSerializer;

@Entity
@Table(name = "Incident")
@JsonIgnoreProperties(value = { "pdId", "time", "dayOfWeek", "pdDistrict" })
public class Incident {

	private String incidentNumber;
	private String category;
	private String description;
	private String dayOfWeek;
	@Temporal(TemporalType.TIMESTAMP)
	private Date date;
	private String time;
	private String pdDistrict;
	private String resolution;
	private String address;
	private double x = Double.NaN;
	private double y = Double.NaN;
	@JsonSerialize(using = PointSerializer.class)
	@JsonDeserialize(using = PointDeserializer.class)
	private Point<G2D> location;
	@Id
	private long pdId;

	@SuppressWarnings("unused")
	private Incident() {
	}

	public Incident(long id) {
		this.pdId = id;
	}

	public String getIncidentNumber() {
		return this.incidentNumber;
	}

	public void setIncidentNumber(String incidentNumber) {
		this.incidentNumber = incidentNumber;
	}

	public String getCategory() {
		return this.category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDayOfWeek() {
		return this.dayOfWeek;
	}

	public void setDayOfWeek(String dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}

	public Date getDate() {
		return this.date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getTime() {
		return this.time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getPdDistrict() {
		return this.pdDistrict;
	}

	public void setPdDistrict(String pdDistrict) {
		this.pdDistrict = pdDistrict;
	}

	public String getResolution() {
		return this.resolution;
	}

	public void setResolution(String resolution) {
		this.resolution = resolution;
	}

	public String getAddress() {
		return this.address;
	}

	public void setAddress(String address) {
		this.address = address;
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

	public Point<G2D> getLocation() {
		return this.location;
	}

	public void setLocation(Point<G2D> location) {
		this.location = location;
	}

	public long getPdId() {
		return this.pdId;
	}

	@Override
	public boolean equals(Object obj) {
		if ( !( obj instanceof Incident ) ) {
			return false;
		}

		Incident other = (Incident) obj;
		return this.pdId == other.pdId;
	}

	@Override
	public int hashCode() {
		return Long.hashCode( this.pdId );
	}
}
