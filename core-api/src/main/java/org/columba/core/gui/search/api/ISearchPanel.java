package org.columba.core.gui.search.api;

import javax.swing.JComponent;

public interface ISearchPanel {

	public void searchAll(String searchTerm);

	public void searchInProvider(String searchTerm, String providerName);

	public void searchInCriteria(String searchTerm, String providerName,
			String criteriaName);
	
	public JComponent getView();
}