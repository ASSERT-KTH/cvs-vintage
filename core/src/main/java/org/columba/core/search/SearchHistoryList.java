package org.columba.core.search;

import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.columba.core.search.api.ISearchCriteria;
import org.columba.core.search.api.ISearchProvider;

public class SearchHistoryList {
	Map<String, HistoryItem> cache;

	final int MAX_ENTRIES = 5;

	private static SearchHistoryList instance;

	public static SearchHistoryList getInstance() {
		if (instance == null)
			instance = new SearchHistoryList();

		return instance;
	}

	private SearchHistoryList() {
		super();
		cache = new LinkedHashMap<String, HistoryItem>(MAX_ENTRIES + 1, .75F,
				true) {
			// This method is called just after a new entry has been added
			public boolean removeEldestEntry(Map.Entry eldest) {
				return size() > MAX_ENTRIES;
			}
		};

		// ensure map can be used by multiple threads
		cache = Collections.synchronizedMap(cache);
	}

	public void add(String searchTerm, ISearchProvider provider, ISearchCriteria criteria) {
		String key = searchTerm + "/" + provider.getTechnicalName() + "/" + criteria.getTechnicalName();
		cache.put(key, new HistoryItem(searchTerm, provider));
	}

	/**
	 * Return map of history items.
	 * 
	 * @return map key is the search term. Map value is the searchprovider.
	 */
	public Map<String, ISearchProvider> getHistoryMap() {
		Map<String, ISearchProvider> map = new Hashtable<String, ISearchProvider>();
		Iterator<HistoryItem> it = cache.values().iterator();
		while (it.hasNext()) {
			HistoryItem item = it.next();
			map.put(item.getSearchTerm(), item.getProvider());
		}
		return map;
	}

	public class HistoryItem {
		String searchTerm;

		ISearchProvider provider;
		ISearchCriteria criteria;
		
		HistoryItem(String searchTerm, ISearchProvider provider) {
			this.searchTerm = searchTerm;
			this.provider = provider;
		}

		public ISearchProvider getProvider() {
			return provider;
		}

		public String getSearchTerm() {
			return searchTerm;
		}

		public ISearchCriteria getCriteria() {
			return criteria;
		}
	}
}
