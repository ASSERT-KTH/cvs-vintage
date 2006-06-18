package org.columba.core.search.api;

import java.net.URI;

/**
 * Search Result
 * 
 * @author frd
 */
public interface ISearchResult {

	/**
	 * Returns title of search result.
	 * 
	 * @return
	 */
	public String getTitle();
	
	/**
	 * Returns description of search result.
	 * 
	 * @return
	 */
	public String getDescription();
	
	/**
	 * Returns location of search result object. This location can be used to navigate
	 * directly in the component.
	 * 
	 * @return
	 */
	public URI getLocation();
}
