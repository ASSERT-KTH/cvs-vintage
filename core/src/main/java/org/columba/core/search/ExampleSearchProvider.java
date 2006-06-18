package org.columba.core.search;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Vector;

import org.columba.core.search.api.ISearchCriteria;
import org.columba.core.search.api.ISearchProvider;
import org.columba.core.search.api.ISearchResult;


public class ExampleSearchProvider implements ISearchProvider {

	public ExampleSearchProvider() {
		super();
	}

	public ISearchCriteria getCriteria(String searchTerm) {
		return new SearchCriteria("test: "+searchTerm, "test description");
	}

	public List<ISearchResult> query(String searchTerm) {
		
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
		return result;
	}

	public String getTechname() {
		return "example";
	}

	public String getNamespace() {
		return "org.columba.core.example";
	}
}
