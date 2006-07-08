package org.columba.core.search;

import org.columba.core.search.api.ISearchCriteria;

public class SearchCriteria implements ISearchCriteria {

	private String name;
	private String description;
	private String technicalName;
	
	public SearchCriteria(String technicalName, String name, String description) {
		this.technicalName = technicalName;
		this.name = name;
		this.description = description;
	}

	public String getTitle() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getTechnicalName() {
		return technicalName;
	}

}
