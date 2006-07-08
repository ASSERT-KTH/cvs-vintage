package org.columba.contact.search;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.ImageIcon;

import org.columba.addressbook.folder.AddressbookFolder;
import org.columba.addressbook.gui.search.BasicResultPanel;
import org.columba.addressbook.gui.tree.AddressbookTreeModel;
import org.columba.addressbook.model.IContactModel;
import org.columba.core.gui.search.api.IResultPanel;
import org.columba.core.resourceloader.IconKeys;
import org.columba.core.resourceloader.ImageLoader;
import org.columba.core.search.SearchCriteria;
import org.columba.core.search.SearchResult;
import org.columba.core.search.api.ISearchCriteria;
import org.columba.core.search.api.ISearchProvider;
import org.columba.core.search.api.ISearchResult;

public class ContactSearchProvider implements ISearchProvider {
	private static final String CRITERIA_DISPLAYNAME_CONTAINS = "displayname_contains";

	private static final String CRITERIA_EMAIL_CONTAINS = "email_contains";

	private ResourceBundle bundle;

	private int totalResultCount = 0;

	public ContactSearchProvider() {
		bundle = ResourceBundle
				.getBundle("org.columba.addressbook.i18n.search");
	}

	public String getTechnicalName() {
		return "ContactSearchProvider";
	}

	public String getName() {
		return bundle.getString("provider_title");
	}

	public String getDescription() {
		return bundle.getString("provider_description");
	}

	public ImageIcon getIcon() {
		return ImageLoader.getSmallIcon(IconKeys.ADDRESSBOOK);
	}

	public List<ISearchCriteria> getAllCriteria(String searchTerm) {
		List<ISearchCriteria> list = new Vector<ISearchCriteria>();

		list.add(getCriteria(ContactSearchProvider.CRITERIA_EMAIL_CONTAINS, searchTerm));
		list.add(getCriteria(ContactSearchProvider.CRITERIA_DISPLAYNAME_CONTAINS, searchTerm));
		return list;
	}

	public IResultPanel getResultPanel(String searchCriteriaTechnicalName) {
		return new BasicResultPanel(getTechnicalName(),
				searchCriteriaTechnicalName);
	}

	public ISearchCriteria getCriteria(String searchCriteriaTechnicalName, String searchTerm) {
		String title = MessageFormat.format(bundle.getString(searchCriteriaTechnicalName
				+ "_title"), new Object[] { searchTerm });

		String description = MessageFormat.format(bundle
				.getString(searchCriteriaTechnicalName + "_description"),
				new Object[] { searchTerm });

		return new SearchCriteria(searchCriteriaTechnicalName, title, description);
	}

	
	
	public List<ISearchResult> query(String searchTerm,
			String criteriaTechnicalName, int startIndex, int resultCount) {
		if ( searchTerm == null ) throw new IllegalArgumentException("searchTerm == null");
		if ( criteriaTechnicalName == null ) throw new IllegalArgumentException("criteriaTechnicalName == null");
		
		List<ISearchResult> result = new Vector<ISearchResult>();

		// create list of contact folders
		List<AddressbookFolder> v = createContactFolderList();

		Iterator<AddressbookFolder> it = v.iterator();
		while (it.hasNext()) {
			AddressbookFolder f = it.next();
			String id = null;
			
			if (criteriaTechnicalName.equals(ContactSearchProvider.CRITERIA_DISPLAYNAME_CONTAINS)) {
				id = f.findByName(searchTerm);
			} else if (criteriaTechnicalName.equals(ContactSearchProvider.CRITERIA_EMAIL_CONTAINS)) {
				id = f.findByEmailAddress(searchTerm);
			}

			if (id != null) {
				IContactModel model = f.get(id);

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
