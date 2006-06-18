package org.columba.core.search;

import java.net.URI;
import java.net.URL;

import org.columba.core.search.api.ISearchResult;

public class SearchResult implements ISearchResult {

	private String title;

	private String description;

	private URI location;

	public SearchResult(String title, String description, URI location) {
		this.title = title;
		this.description = description;
		this.location = location;

	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public URI getLocation() {
		return location;
	}

}
