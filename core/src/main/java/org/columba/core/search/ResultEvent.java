package org.columba.core.search;

import java.util.EventObject;
import java.util.List;

import org.columba.core.search.api.IResultEvent;
import org.columba.core.search.api.ISearchCriteria;
import org.columba.core.search.api.ISearchResult;

public class ResultEvent extends EventObject implements IResultEvent {

	private List<ISearchResult> result;
	private String searchTerm;
	private ISearchCriteria criteria;
	private int totalResultCount;
	private String name;

	
	public ResultEvent(Object source) {
		super(source);
	}
	
	public ResultEvent(Object source, String searchTerm) {
		super(source);
		this.searchTerm = searchTerm;
	}
	
	
	public ResultEvent(Object source, String searchTerm, String name,  ISearchCriteria criteria, List<ISearchResult> result, int totalResultCount) {
		super(source);
		
		this.searchTerm = searchTerm;
		this.name = name;

		this.criteria = criteria;
		this.result = result;
		this.totalResultCount = totalResultCount;
	}

	public List<ISearchResult> getSearchResults() {
		return result;
	}

	public String getSearchTerm() {
		return searchTerm;
	}

	public ISearchCriteria getSearchCriteria() {
		return criteria;
	}

	public int getTotalResultCount() {
		return totalResultCount;
	}

	public String getProviderName() {
		return name;
	}

	

}
