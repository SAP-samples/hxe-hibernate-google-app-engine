package com.sap.hana.hibernate.sample.entities;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class TermMapping {

	@Id
	private String mappingId;
	private String listId;
	private String languageCode;
	@Column(name = "term_1")
	private String term1;
	@Column(name = "term_2")
	private String term2;
	private BigDecimal weight;

	public TermMapping() {
		// TODO Auto-generated constructor stub
	}

	public String getMappingId() {
		return this.mappingId;
	}

	public void setMappingId(String mappingId) {
		this.mappingId = mappingId;
	}

	public String getListId() {
		return this.listId;
	}

	public void setListId(String listId) {
		this.listId = listId;
	}

	public String getLanguageCode() {
		return this.languageCode;
	}

	public void setLanguageCode(String languageCode) {
		this.languageCode = languageCode;
	}

	public String getTerm1() {
		return this.term1;
	}

	public void setTerm1(String term1) {
		this.term1 = term1;
	}

	public String getTerm2() {
		return this.term2;
	}

	public void setTerm2(String term2) {
		this.term2 = term2;
	}

	public BigDecimal getWeight() {
		return this.weight;
	}

	public void setWeight(BigDecimal weight) {
		this.weight = weight;
	}

}
