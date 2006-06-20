package org.columba.mail.search;

import java.net.URI;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.columba.core.filter.Filter;
import org.columba.core.filter.FilterCriteria;
import org.columba.core.filter.FilterFactory;
import org.columba.core.search.SearchResult;
import org.columba.core.search.api.ISearchCriteria;
import org.columba.core.search.api.ISearchProvider;
import org.columba.core.search.api.ISearchResult;
import org.columba.mail.folder.IMailbox;
import org.columba.ristretto.message.Address;

public abstract class AbstractMailSearchProvider implements ISearchProvider {

	private Hashtable<Integer, IMailbox> folderTable = new Hashtable<Integer, IMailbox>();

	private Vector<SearchIndex> indizes = new Vector<SearchIndex>();

	private int totalResultCount = -1;
	
	public AbstractMailSearchProvider() {
		super();
	}

	public abstract String getName();

	public abstract String getNamespace();

	public abstract ISearchCriteria getCriteria(String searchTerm);

	protected abstract FilterCriteria createFilterCriteria(String searchTerm);

	/**
	 * Algorithm first retrieves all message id representing search results.
	 * Then it retrieves the search results.
	 * <p>
	 * All the message id are cached to support paging.
	 * 
	 * @see org.columba.core.search.api.ISearchProvider#query(java.lang.String,
	 *      int, int)
	 */
	public List<ISearchResult> query(String searchTerm, int startIndex,
			int resultCount) {

		List<ISearchResult> result = new Vector<ISearchResult>();

		List<IMailbox> sourceFolders = SearchFolderFactory
				.getAllSourceFolders();

		// create search criteria
		Filter filter = FilterFactory.createEmptyFilter();
		FilterCriteria criteria = createFilterCriteria(searchTerm);
		if (criteria == null)
			throw new IllegalArgumentException("criteria == null");

		filter.getFilterRule().add(criteria);

		try {
			Iterator<IMailbox> it = sourceFolders.iterator();
			while (it.hasNext()) {
				IMailbox folder = it.next();

				// skip if this folder was already queried
				if (folderTable.containsKey(folder.getUid()))
					continue;

				// memorize folders
				folderTable.put(folder.getUid(), folder);

				// do the search
				Object[] uids = folder.searchMessages(filter);

				// memorize message IDs
				for (int i = 0; i < uids.length; i++) {
					SearchIndex idx = new SearchIndex(folder, uids[i]);
					indizes.add(idx);
				}

			}

			// retrieve the actual search result data
			List<ISearchResult> l = retrieveResultData(indizes, startIndex,
					resultCount);
			result.addAll(l);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		// memorize total result count
		totalResultCount = result.size();
		
		return result;
	}

	private List<ISearchResult> retrieveResultData(Vector<SearchIndex> indizes,
			int startIndex, int resultCount) throws Exception {
		// ensure we are in existing result range
		int count = (startIndex + resultCount <= indizes.size()) ? resultCount
				: indizes.size();
		List<ISearchResult> result = new Vector<ISearchResult>();
		// gather result results
		for (int i = startIndex; i < count; i++) {
			SearchIndex idx = indizes.get(i);
			IMailbox folder = idx.folder;
			Object messageId = idx.messageId;

			// TODO @author fdietz: ensure that we don't fetch individual
			// headers
			// to reduce client/server roundtrips
			String title = (String) folder.getAttribute(messageId,
					"columba.subject");
			Address from = (Address) folder.getAttribute(messageId,
					"columba.from");
			Date date = (Date) folder.getAttribute(messageId, "columba.date");
			String description = from.toString() + " " + date;
			URI uri = SearchResultBuilder.createURI(folder.getUid(), messageId);

			result.add(new SearchResult(title, description, uri));
		}
		return result;
	}

	class SearchIndex {
		IMailbox folder;

		Object messageId;

		SearchIndex(IMailbox folder, Object messageId) {
			this.folder = folder;
			this.messageId = messageId;
		}
	}

	public int getTotalResultCount() {
		return totalResultCount;
	}

}
