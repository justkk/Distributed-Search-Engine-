package edu.upenn.cis555.pagerank.models;

import java.util.List;

public class PageRankIntermediate {

	String parent;
	Double parentPageRank;
	Integer parentOutgoingLinks;
	List<String> outgoingList;
	
	public PageRankIntermediate(ParentChildOutputData data) {
		parent = data.getParent();
		parentPageRank = data.getPageRank();
		parentOutgoingLinks = data.getChildren().size();
		outgoingList = null;
	}
	
	public PageRankIntermediate(List<String> data) {
		parent = null;
		parentPageRank = null;
		parentOutgoingLinks = null;
		outgoingList = data;
	}
	
	public String getParent() {
		return parent;
	}
	public void setParent(String parent) {
		this.parent = parent;
	}
	public Double getParentPageRank() {
		return parentPageRank;
	}
	public void setParentPageRank(Double parentPageRank) {
		this.parentPageRank = parentPageRank;
	}
	public Integer getParentOutgoingLinks() {
		return parentOutgoingLinks;
	}
	public void setParentOutgoingLinks(Integer parentOutgoingLinks) {
		this.parentOutgoingLinks = parentOutgoingLinks;
	}
	public List<String> getOutgoingList() {
		return outgoingList;
	}
	public void setOutgoingList(List<String> outgoingList) {
		this.outgoingList = outgoingList;
	}
	
	
}
