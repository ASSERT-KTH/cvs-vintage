package org.columba.contact.search;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.columba.addressbook.folder.AddressbookFolder;
import org.columba.addressbook.gui.tree.AddressbookTreeModel;
import org.columba.addressbook.model.IContactModel;
import org.columba.core.search.SearchCriteria;
import org.columba.core.search.SearchResult;
import org.columba.core.search.api.ISearchCriteria;
import org.columba.core.search.api.ISearchProvider;
import org.columba.core.search.api.ISearchResult;

public class EmailContainsSearchProvider implements ISearchProvider {

	private int totalResultCount = 0;

	public EmailContainsSearchProvider() {
		super();
	}

	public String getName() {
		return "email_contains";
	}

	public String getNamespace() {
		return "org.columba.contact";
	}

	public ISearchCriteria getCriteria(String searchTerm) {
		return new SearchCriteria("Address contains " + searchTerm,
				"Email Address contains " + searchTerm);
	}

	public List<ISearchResult> query(String searchTerm, int startIndex,
			int resultCount) {

		List<ISearchResult> result = new Vector<ISearchResult>();

		// create list of contact folders
		List<AddressbookFolder> v = createContactFolderList();

		Iterator<AddressbookFolder> it = v.iterator();
		while (it.hasNext()) {
			AddressbookFolder f = it.next();
			String id = f.findByEmailAddress(searchTerm);
			if ( id == null ) continue;
			
			IContactModel model = f.get(id);

			if (id != null) {
				result.add(new SearchResult(model.getSortString(), model
						.getPreferredEmail(), SearchResultBuilder.createURI(f
						.getId(), id)));
				
			}
		}

		totalResultCount = result.size();
		
		return result;
	}

	private List<AddressbookFolder> createContactFolderList() {
		List<AddressbookFolder> v = new Vector<AddressbookFolder>();
		AddressbookTreeModel treeModel = AddressbookTreeModel.getInstance();
		v.add((AddressbookFolder) treeModel.getFolder("101"));
		v.add((AddressbookFolder) treeModel.getFolder("102"));
		return v;
	}

	public int getTotalResultCount() {
		return totalResultCount;
	}

}
