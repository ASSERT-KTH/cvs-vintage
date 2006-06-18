package org.columba.core.search.api;

import java.util.List;

/**
 * Provider does the actual search and furthermore contains a description
 * of the search criteria.
 * 
 * @author frd
 */
public interface ISearchProvider {

	/**
	 * Return Unique technical name.
	 * @return
	 */
	public String getTechname();
	
	/**
	 * Return namespace. This is used to group search criteria.
	 * 
	 * @return
	 */
	public String getNamespace();
	
	/**
	 * Retrieve search criteria for given search term.
	 * 
	 * @param searchTerm
	 * @return
	 */
	public ISearchCriteria getCriteria(String searchTerm);

	/**
	 * Execute query and retrieve search result for given search term.
	 * 
	 * @param searchTerm
	 * @return
	 */
	public List<ISearchResult> query(String searchTerm);
}
