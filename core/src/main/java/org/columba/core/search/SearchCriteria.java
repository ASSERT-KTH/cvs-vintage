package org.columba.core.search;

import org.columba.core.search.api.ISearchCriteria;

public class SearchCriteria implements ISearchCriteria {

	private String name;
	private String description;
	
	public SearchCriteria(String name, String description) {
		this.name = name;
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

}
