package org.columba.addressbook.gui.search;

import java.util.List;

import javax.swing.JComponent;

import org.columba.core.gui.search.api.IResultPanel;
import org.columba.core.search.api.IResultEvent;
import org.columba.core.search.api.ISearchResult;

public class BasicResultPanel implements IResultPanel {

	private String providerTechnicalName;

	private String criteriaTechnicalName;

	private SearchResultList list;
	
	public BasicResultPanel(String providerTechnicalName,
			String criteriaTechnicalName) {
		super();

		this.criteriaTechnicalName = criteriaTechnicalName;
		this.providerTechnicalName = providerTechnicalName;

		list = new SearchResultList();
	}

	public String getSearchCriteriaTechnicalName() {
		return criteriaTechnicalName;
	}

	public String getProviderTechnicalName() {
		return providerTechnicalName;
	}

	public JComponent getView() {
		return list;
	}

	public void resultArrived(IResultEvent event) {
		if (!event.getProviderName().equals(providerTechnicalName))
			return;
		if (!event.getSearchCriteria().getTechnicalName().equals(
				this.criteriaTechnicalName))
			return;

		List<ISearchResult> result = event.getSearchResults();

		list.addAll(result);
		
	}

	public void clearSearch(IResultEvent event) {
		list.clear();
	}

	public void reset(IResultEvent event) {
		list.clear();
	}

	
}