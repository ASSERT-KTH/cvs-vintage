package org.columba.addressbook.gui.context;

import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

import org.columba.addressbook.gui.search.SearchResultList;
import org.columba.contact.search.ContactSearchProvider;
import org.columba.core.context.base.api.IStructureValue;
import org.columba.core.context.semantic.api.ISemanticContext;
import org.columba.core.gui.context.api.IContextProvider;
import org.columba.core.resourceloader.IconKeys;
import org.columba.core.resourceloader.ImageLoader;
import org.columba.core.search.api.ISearchResult;

// TODO: replace view with more detailed info view
public class ContactContextualProvider implements
		IContextProvider {

	private ResourceBundle bundle;

	private SearchResultList list;

	private ContactSearchProvider searchProvider;

	private List<ISearchResult> result;

	public ContactContextualProvider() {
		super();

		bundle = ResourceBundle
				.getBundle("org.columba.addressbook.i18n.search");

		list = new SearchResultList();

		result = new Vector<ISearchResult>();
		
		searchProvider = new ContactSearchProvider();
	}

	public String getName() {
		return bundle.getString("provider_title");
	}

	public String getDescription() {
		return bundle.getString("provider_title");
	}

	public ImageIcon getIcon() {
		return ImageLoader.getSmallIcon(IconKeys.ADDRESSBOOK);
	}

	public int getTotalResultCount() {
		return searchProvider.getTotalResultCount();
	}

	public void search(ISemanticContext context, int startIndex, int resultCount) {

		IStructureValue value = context.getValue();
		if ( value == null ) return;
		
		Iterator<IStructureValue> it = value.getChildIterator(
				ISemanticContext.CONTEXT_NODE_IDENTITY,
				ISemanticContext.CONTEXT_NAMESPACE_CORE);
		// can be only one
		IStructureValue identity = it.next();
		if (identity == null)
			return;

		String emailAddress = identity.getString(
				ISemanticContext.CONTEXT_ATTR_EMAIL_ADDRESS,
				ISemanticContext.CONTEXT_NAMESPACE_CORE);
		String displayname = identity.getString(
				ISemanticContext.CONTEXT_ATTR_DISPLAY_NAME,
				ISemanticContext.CONTEXT_NAMESPACE_CORE);

		if (emailAddress == null && displayname == null)
			return;

		List<ISearchResult> temp;

		if (emailAddress != null) {
			temp = searchProvider.query(emailAddress,
					ContactSearchProvider.CRITERIA_EMAIL_CONTAINS, 0, 5);
			result.addAll(temp);
		}

		if (displayname != null) {
			temp = searchProvider.query(displayname,
			ContactSearchProvider.CRITERIA_DISPLAYNAME_CONTAINS, 0, 5);
			result.addAll(temp);
		}

	}

	public void showResult() {
		list.addAll(result);
	}

	public JComponent getView() {
		return list;
	}

	public void clear() {
		result.clear();
		list.clear();
	}

}
