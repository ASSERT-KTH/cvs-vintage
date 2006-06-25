package org.columba.core.search.api;

import javax.swing.ImageIcon;

public interface ISearchCriteria {

	/**
	 * Returns name of search criteria.
	 * 
	 * @return
	 */
	String getTitle();
	
	/**
	 * Returns description of search criteria.
	 * 
	 * @return
	 */
	String getDescription();
	   
	ImageIcon getIcon();
}
