package edu.upenn.cis555.pagerank.models;

import java.util.List;

public class SinksIntermediate {

	Integer numLinks;
	List<String> links;
	
	public SinksIntermediate(Integer numLinks, List<String> links) {
		super();
		this.numLinks = numLinks;
		this.links = links;
	}
	public Integer getNumLinks() {
		return numLinks;
	}
	public void setNumLinks(Integer numLinks) {
		this.numLinks = numLinks;
	}
	public List<String> getLinks() {
		return links;
	}
	public void setLinks(List<String> links) {
		this.links = links;
	}
	
}
