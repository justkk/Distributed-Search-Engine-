package edu.upenn.cis555.pagerank.models;

import java.util.ArrayList;
import java.util.List;

public class ParentChild {

	String hostname;
	List<String> childNames;
	
	public ParentChild(String parent, List<String> childNames) {
		this.hostname=parent;
		this.childNames = childNames;
	}
	
	public String getHostname() {
		return hostname;
	}
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
	public List<String> getChildNames() {
		return childNames;
	}
	public void setChildNames(List<String> childNames) {
		this.childNames = childNames;
	}
	
	
	public void addChildNames(String childName) {
		if(childNames==null) {
			childNames = new ArrayList<String>();
		}
		if(!childNames.contains(childName)) {
			childNames.add(childName);
		}
	}
	
}
