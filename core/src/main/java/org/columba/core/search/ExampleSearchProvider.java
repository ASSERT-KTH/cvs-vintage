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

import javax.swing.ImageIcon;

import org.columba.core.gui.search.api.IResultPanel;
import org.columba.core.search.api.ISearchCriteria;
import org.columba.core.search.api.ISearchProvider;
import org.columba.core.search.api.ISearchResult;

public class ExampleSearchProvider implements ISearchProvider {

	private int count = -1;

	public ExampleSearchProvider() {
		super();
	}

	public String getTechnicalName() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getName() {
		return "example";
	}

	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	public ImageIcon getIcon() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<ISearchCriteria> getAllCriteria(String searchTerm) {
		List<ISearchCriteria> l = new Vector<ISearchCriteria>();
		l.add(new SearchCriteria("id1", "test: " + searchTerm,
				"test description"));
		l.add(new SearchCriteria("id2", "test 2: " + searchTerm,
				"test 2 description"));
		return l;
	}

	public int getTotalResultCount() {
		return count;
	}

	public List<ISearchResult> query(String searchTerm,
			ISearchCriteria searchCriteria, int startIndex, int resultCount) {
		URI url = null;
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

	public ISearchCriteria getCriteria(String technicalName, String searchTerm) {
		return new SearchCriteria("id1", "test: " + searchTerm,
				"test description");
	}

	public List<ISearchResult> query(String searchTerm,
			String searchCriteriaTechnicalName, int startIndex, int resultCount) {
		// TODO Auto-generated method stub
		return null;
	}

	public IResultPanel getResultPanel(String searchCriteriaTechnicalName) {
		// TODO Auto-generated method stub
		return null;
	}

}
