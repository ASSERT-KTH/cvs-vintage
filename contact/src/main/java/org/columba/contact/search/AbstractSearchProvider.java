package org.columba.contact.search;

import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;

import org.columba.core.resourceloader.IconKeys;
import org.columba.core.resourceloader.ImageLoader;
import org.columba.core.search.SearchCriteria;
import org.columba.core.search.api.ISearchCriteria;
import org.columba.core.search.api.ISearchProvider;
import org.columba.core.search.api.ISearchResult;

public abstract class AbstractSearchProvider implements ISearchProvider {

	private ResourceBundle bundle;

	public AbstractSearchProvider() {
		super();

		bundle = ResourceBundle
				.getBundle("org.columba.addressbook.i18n.search");
	}

	public abstract String getName();

	public abstract String getNamespace();

	public ISearchCriteria getCriteria(String searchTerm) {

		String title = MessageFormat.format(bundle.getString(getName()
				+ "_title"), new Object[] { searchTerm });

		String description = MessageFormat.format(bundle.getString(getName()
				+ "_description"), new Object[] { searchTerm });

		return new SearchCriteria(title, description, ImageLoader
				.getSmallIcon(IconKeys.ADDRESSBOOK));
	}

	public abstract List<ISearchResult> query(String searchTerm,
			int startIndex, int resultCount);

	public abstract int getTotalResultCount();
}
