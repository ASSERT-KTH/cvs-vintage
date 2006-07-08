package org.columba.core.search.api;

import java.util.List;

import javax.swing.ImageIcon;

import org.columba.api.plugin.IExtensionInterface;
import org.columba.core.gui.search.api.IResultPanel;

/**
 * Provider does the actual search and furthermore contains a description
 * of the search criteria.
 * 
 * @author frd
 */
public interface ISearchProvider extends IExtensionInterface{

	/**
	 * Returns technical name. Should be unique.
	 * @return
	 */
	public String getTechnicalName();
	
	/**
	 * Return provider human-readable name
	 * @return
	 */
	public String getName();
	
	/**
	 * Return provider human-readable description
	 * @return
	 */
	public String getDescription();
	
	/**
	 * Return provider icon
	 * @return
	 */
	public ImageIcon getIcon();
	
	
	/**
	 * Retrieve search criteria for given search term.
	 * 
	 * @param searchTerm
	 * @return
	 */
	public List<ISearchCriteria> getAllCriteria(String searchTerm);

	
	/**
	 * Get result panel for given search criteria.
	 * 
	 * @param searchCriteriaTechnicalName
	 * @return
	 */
	public IResultPanel getResultPanel(String searchCriteriaTechnicalName);
	
	/**
	 * Retrieve single search criteria for given search term.
	 * 
	 * @param technicalName
	 * @param searchTerm
	 * @return
	 */
	public ISearchCriteria getCriteria(String technicalName, String searchTerm);
	
	/**
	 * Execute query and retrieve pageable search result for given search term 
	 * across all search criteria this provider provides.
	 * <p>
	 * The query returns <code>resultCount</code> individual results, from
	 * a given <code>startIndex</code>. Paging should be supported, so its
	 * up to the underlying implementation to use an intelligent caching 
	 * strategy or whatever necessary to make repetitive calls to this
	 * method fast.
	 *  
	 * @param searchTerm		search term
	 * @param startIndex		start index of search results
	 * @param resultCount		total count of results
	 * 
	 * @return	
	 */
	//public List<ISearchResult> query(String searchTerm, int startIndex, int resultCount);
	
	/**
	 * Execute query and retrieve pageable search result for given search term and
	 * a single search criteria.
	 * <p>
	 * The query returns <code>resultCount</code> individual results, from
	 * a given <code>startIndex</code>. Paging should be supported, so its
	 * up to the underlying implementation to use an intelligent caching 
	 * strategy or whatever necessary to make repetitive calls to this
	 * method fast.
	 *  
	 * @param searchTerm					search term
	 * @param searchCriteriaTechnicalName	search criteria technical name
	 * @param startIndex					start index of search results
	 * @param resultCount					total count of results
	 * 
	 * @return	
	 */
	public List<ISearchResult> query(String searchTerm, String searchCriteriaTechnicalName, int startIndex, int resultCount);
	
	/**
	 * Return total number of search results. Method only returns valid result after calling
	 * <code>query</code> first.
	 * 
	 * @return	total number of search results. <code>-1</code>, in case <code>query</code> was not called, yet
	 */
	public int getTotalResultCount();
}
