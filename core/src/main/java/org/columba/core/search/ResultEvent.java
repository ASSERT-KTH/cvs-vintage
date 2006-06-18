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
	
	public ResultEvent(Object source) {
		super(source);
	}
	
	public ResultEvent(Object source, String searchTerm, ISearchCriteria criteria, List<ISearchResult> result) {
		super(source);
		
		this.searchTerm = searchTerm;
		this.criteria = criteria;
		this.result = result;
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

}
