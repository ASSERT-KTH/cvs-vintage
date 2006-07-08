package org.columba.core.search.api;


public interface ISearchCriteria {

	/**
	 * Returns technical name. Should be unique in the provider context.
	 * 
	 * @return
	 */
	String getTechnicalName();
	
	/**
	 * Returns human-readable name of search criteria.
	 * 
	 * @return
	 */
	String getTitle();
	
	/**
	 * Returns human-readable description of search criteria.
	 * 
	 * @return
	 */
	String getDescription();
	
}
