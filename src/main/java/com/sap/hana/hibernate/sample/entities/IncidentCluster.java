package com.sap.hana.hibernate.sample.entities;

import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.SqlResultSetMappings;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.geolatte.geom.C2D;
import org.geolatte.geom.G2D;
import org.geolatte.geom.Geometry;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.sap.hana.hibernate.sample.util.GeometryDeserializer;
import com.sap.hana.hibernate.sample.util.GeometrySerializer;

@Entity
@Table(name = "Incident")
@SqlResultSetMappings(@SqlResultSetMapping(name = "incidentCluster", classes = {
		@ConstructorResult(targetClass = IncidentCluster.class, columns = {
				@ColumnResult(name = "clusterId", type = Long.class),
				@ColumnResult(name = "convexHull", type = Geometry.class),
				@ColumnResult(name = "numberOfIncidents", type = Long.class),
				@ColumnResult(name = "category", type = String.class)
		})
}))
@JsonIgnoreProperties(value = { "pdId" })
public class IncidentCluster {

	@Id
	private long pdId;
	@Transient
	private long clusterId;
	@Transient
	private long numberOfIncidents;
	@Transient
	@JsonSerialize(using = GeometrySerializer.class)
	@JsonDeserialize(using = GeometryDeserializer.class)
	private Geometry<G2D> convexHull;
	@Transient
	private String clusterCategory;

	public IncidentCluster(long id, Geometry<G2D> convexHull, long numberOfIncidents, String clusterCategory) {
		this.clusterId = id;
		this.convexHull = convexHull;
		this.numberOfIncidents = numberOfIncidents;
		this.clusterCategory = clusterCategory;
	}

	public long getPdId() {
		return this.pdId;
	}

	public long getClusterId() {
		return this.clusterId;
	}

	public void setClusterId(long clusterId) {
		this.clusterId = clusterId;
	}

	public Geometry<G2D> getConvexHull() {
		return this.convexHull;
	}

	public long getNumberOfIncidents() {
		return this.numberOfIncidents;
	}

	public String getClusterCategory() {
		return this.clusterCategory;
	}

	public void setClusterCategory(String clusterCategory) {
		this.clusterCategory = clusterCategory;
	}

}
