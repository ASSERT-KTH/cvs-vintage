/**
 * 
 */
package org.columba.mail.search;

import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.columba.core.filter.FilterCriteria;
import org.columba.core.search.SearchCriteria;
import org.columba.core.search.SearchResult;
import org.columba.core.search.api.ISearchCriteria;
import org.columba.core.search.api.ISearchProvider;
import org.columba.core.search.api.ISearchResult;
import org.columba.mail.filter.MailFilterFactory;
import org.columba.mail.folder.IMailbox;
import org.columba.mail.folder.virtual.VirtualFolder;
import org.columba.mail.gui.tree.FolderTreeModel;
import org.columba.mail.message.IColumbaHeader;
import org.columba.mail.message.IHeaderList;

/**
 * @author frd
 * 
 */
public class SubjectSearchProvider implements ISearchProvider {

	public SubjectSearchProvider() {
		super();
	}

	/**
	 * @see org.columba.core.search.api.ISearchProvider#getTechname()
	 */
	public String getTechname() {
		return "Subject_Contains";
	}

	/**
	 * @see org.columba.core.search.api.ISearchProvider#getNamespace()
	 */
	public String getNamespace() {
		return "org.columba.mail";
	}

	/**
	 * @see org.columba.core.search.api.ISearchProvider#getCriteria(java.lang.String)
	 */
	public ISearchCriteria getCriteria(String searchTerm) {
		return new SearchCriteria("Subject contains " + searchTerm,
				"Subject contains " + searchTerm);
	}

	/**
	 * @see org.columba.core.search.api.ISearchProvider#query(java.lang.String)
	 */
	public List<ISearchResult> query(String searchTerm) {
		IMailbox inboxFolder = (IMailbox) FolderTreeModel.getInstance()
				.getFolder(101);

		// create search criteria
		FilterCriteria criteria = MailFilterFactory
				.createSubjectContains(searchTerm);

		IHeaderList headerList = null;
		try {
			// set criteria for search folder
			VirtualFolder searchFolder = SearchFolderFactory
					.prepareSearchFolder(criteria, inboxFolder);
			// do the search
			headerList = searchFolder.getHeaderList();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		// gather result results
		List<ISearchResult> list = new Vector<ISearchResult>();
		Iterator it = headerList.headerIterator();
		while (it.hasNext()) {
			IColumbaHeader h = (IColumbaHeader) it.next();

			String title = SearchResultBuilder.createSubject(h);
			String description = SearchResultBuilder.createFrom(h) + " "
					+ SearchResultBuilder.createDate(h);
			URI uri = SearchResultBuilder.createURI(h, inboxFolder);

			list.add(new SearchResult(title, description, uri));
		}
		return list;
	}

}
