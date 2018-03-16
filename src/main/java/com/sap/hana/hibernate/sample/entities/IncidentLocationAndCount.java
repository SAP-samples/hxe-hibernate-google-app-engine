package com.sap.hana.hibernate.sample.entities;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.geolatte.geom.C2D;
import org.geolatte.geom.Geometry;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "Incident")
@JsonIgnoreProperties(value = { "pdId" })
public class IncidentLocationAndCount {

	private double x = Double.NaN;
	private double y = Double.NaN;
	@Id
	private long pdId;
	@Transient
	private long weight;

	public IncidentLocationAndCount(Geometry<C2D> location, long weight) {
		C2D positions = location.getPositions().getPositionN( 0 );
		this.x = positions.getX();
		this.y = positions.getY();
		this.weight = weight;
	}

	public long getPdId() {
		return this.pdId;
	}

	public void setPdId(long pdId) {
		this.pdId = pdId;
	}

	public long getWeight() {
		return this.weight;
	}

	public void setWeight(long weight) {
		this.weight = weight;
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

}
