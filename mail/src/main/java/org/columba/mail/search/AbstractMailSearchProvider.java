package org.columba.mail.search;

import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.tree.TreePath;

import org.columba.core.filter.Filter;
import org.columba.core.filter.FilterCriteria;
import org.columba.core.filter.FilterFactory;
import org.columba.core.resourceloader.ImageLoader;
import org.columba.core.search.api.ISearchCriteria;
import org.columba.core.search.api.ISearchProvider;
import org.columba.core.search.api.ISearchResult;
import org.columba.mail.folder.IMailbox;
import org.columba.mail.resourceloader.MailImageLoader;
import org.columba.ristretto.message.Address;
import org.columba.ristretto.message.Flags;

public abstract class AbstractMailSearchProvider implements ISearchProvider {

	private static final Logger LOG = Logger
			.getLogger("org.columba.mail.search.AbstractMailSearchProvider");

	private Hashtable<Integer, IMailbox> folderTable = new Hashtable<Integer, IMailbox>();

	private Vector<SearchIndex> indizes = new Vector<SearchIndex>();

	private int totalResultCount = 0;

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
				LOG.info("searching in "+new TreePath(folder.getPath()).toString());
				
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
		totalResultCount = indizes.size();

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

			ImageIcon statusIcon = null;
			Flags flags = folder.getFlags(messageId);
			if (flags.getDeleted()) {
				statusIcon = ImageLoader.getSmallIcon("user-trash.png");

			} else if (flags.getAnswered()) {
				statusIcon = MailImageLoader
						.getSmallIcon("message-mail-replied.png");
			} else if (flags.getDraft()) {
				statusIcon = MailImageLoader.getSmallIcon("edit.png");
			} else if (!flags.getSeen()) {
				statusIcon = MailImageLoader
						.getSmallIcon("message-mail-unread.png");
			} else if (flags.getSeen()) {
				statusIcon = MailImageLoader
						.getSmallIcon("message-mail-read.png");
			}

			String dateString = null;
			int diff = getLocalDaysDiff(date.getTime());

			// if ( today
			if ((diff >= 0) && (diff < 7)) {
				dateString = dfWeek.format(date);
			} else {
				dateString = dfCommon.format(date);
			}

			result.add(new MailSearchResult(title, description, uri,
					dateString, from, statusIcon, flags.getFlagged()));
		}
		return result;
	}

	static SimpleDateFormat dfWeek = new SimpleDateFormat("EEE HH:mm", Locale
			.getDefault());

	// use local date settings
	DateFormat dfCommon = DateFormat.getDateInstance();

	static final long OneDay = 24 * 60 * 60 * 1000;

	static TimeZone localTimeZone = TimeZone.getDefault();

	public static int getLocalDaysDiff(long t) {
		return (int) (((System.currentTimeMillis() + localTimeZone
				.getRawOffset()) - (((t + localTimeZone.getRawOffset()) / OneDay) * OneDay)) / OneDay);
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
