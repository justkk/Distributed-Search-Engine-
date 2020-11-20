package edu.upenn.cis555.pagerank.models;

import java.util.List;

public class ParentChildOutputData {

	String parent;
	List<String> children;
	Double pageRank;
	
	public ParentChildOutputData(ParentChild input, Double pageRank) {
		if(input!=null) {
			this.parent = input.getHostname();
			this.children = input.getChildNames();
			this.pageRank = pageRank;
		}
	}
	
	public ParentChildOutputData(String parent, List<String> children, Double pageRank) {
		if(parent!=null) {
			this.parent = parent;
			this.children = children;
			this.pageRank = pageRank;
		}
	}
	
	public String getParent() {
		return parent;
	}
	public void setParent(String parent) {
		this.parent = parent;
	}
	public List<String> getChildren() {
		return children;
	}
	public void setChildren(List<String> children) {
		this.children = children;
	}
	public Double getPageRank() {
		return pageRank;
	}
	public void setPageRank(Double pageRank) {
		this.pageRank = pageRank;
	}
	
}
