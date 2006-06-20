package org.columba.core.search;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.columba.core.search.api.ISearchResult;

public class DistinctResultHelper {

	/**
	 * Remove duplicates by searching for duplicate location URIs
	 */
	public static void removeDuplicated(List<ISearchResult> list) {
		// temporary hashtable to check for duplicated
		Hashtable<String, String> hashtable = new Hashtable<String, String>();
		
		Iterator<ISearchResult> it = list.iterator();
		while (it.hasNext()) {
			ISearchResult r = it.next();
			
			// if result is already in hashtable remove from result set
			if ( hashtable.containsKey(r.getLocation().toString()))
				it.remove();
			else
				// memorize location URI
				hashtable.put(r.getLocation().toString(), r.getLocation().toString());
		}
	}
}
