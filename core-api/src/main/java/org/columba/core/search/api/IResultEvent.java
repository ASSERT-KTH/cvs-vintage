package org.columba.core.search.api;

import java.util.List;

public interface IResultEvent {

	public Object getSource();
	public String getProviderName();
	public String getSearchTerm();
	public ISearchCriteria getSearchCriteria();
	public List<ISearchResult> getSearchResults();
	public int getTotalResultCount();
}
