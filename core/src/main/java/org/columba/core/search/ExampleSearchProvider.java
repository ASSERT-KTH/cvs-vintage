// The contents of this file are subject to the Mozilla Public License Version
// 1.1
//(the "License"); you may not use this file except in compliance with the
//License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
//for the specific language governing rights and
//limitations under the License.
//
//The Original Code is "The Columba Project"
//
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.core.search;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Vector;

import org.columba.core.search.api.ISearchCriteria;
import org.columba.core.search.api.ISearchProvider;
import org.columba.core.search.api.ISearchResult;


public class ExampleSearchProvider implements ISearchProvider {

	private int count = -1;
	
	public ExampleSearchProvider() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.columba.core.search.api.ISearchProvider#getCriteria(java.lang.String)
	 */
	public ISearchCriteria getCriteria(String searchTerm) {
		return new SearchCriteria("test: "+searchTerm, "test description", null);
	}

	/**
	 * @see org.columba.core.search.api.ISearchProvider#query(java.lang.String, int, int)
	 */
	public List<ISearchResult> query(String searchTerm, int startIndex, int resultCount) {
		
		URI url=null;
		try {
			url = new URI("columba://org.columba.core.example/test");
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
		
		List<ISearchResult> result = new Vector<ISearchResult>();
		result.add(new SearchResult("item1", "item1 description", url));
		result.add(new SearchResult("item2", "item2 description", url));
		result.add(new SearchResult("item3", "item3 description", url));
		
		count = result.size();
		
		return result;
	}

	/* (non-Javadoc)
	 * @see org.columba.core.search.api.ISearchProvider#getName()
	 */
	public String getName() {
		return "example";
	}

	/* (non-Javadoc)
	 * @see org.columba.core.search.api.ISearchProvider#getNamespace()
	 */
	public String getNamespace() {
		return "org.columba.core.example";
	}

	/* (non-Javadoc)
	 * @see org.columba.core.search.api.ISearchProvider#getTotalResultCount()
	 */
	public int getTotalResultCount() {
		return count;
	}
}
