package org.columba.mail.search;

import org.columba.core.filter.FilterCriteria;
import org.columba.core.search.api.ISearchCriteria;
import org.columba.mail.filter.MailFilterFactory;

public class SizeSmallerThanSearchProvider extends AbstractMailSearchProvider{

	public SizeSmallerThanSearchProvider() {
		super();

	}

	@Override
	public String getName() {
		return "size_smaller_than";
	}

	@Override
	public String getNamespace() {
		return "org.columba.mail";
	}

	/**
	 * @see org.columba.core.search.api.ISearchProvider#getCriteria(java.lang.String)
	 */
	public ISearchCriteria getCriteria(String searchTerm) {
		if (searchTerm.length() == 0)
			return super.getCriteria(searchTerm);

		try {
			int searchTermInt = Integer.parseInt(searchTerm);
			return super.getCriteria(searchTerm);
		} catch (NumberFormatException e) {
		}

		return null;
	}

	@Override
	protected FilterCriteria createFilterCriteria(String searchTerm) {

		try {
			int searchTermInt = Integer.parseInt(searchTerm);
			FilterCriteria criteria = MailFilterFactory
					.createSizeIsSmaller(searchTermInt);
			return criteria;
		} catch (NumberFormatException e) {
		}

		return null;
	}
}
