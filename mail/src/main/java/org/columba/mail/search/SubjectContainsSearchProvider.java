/**
 * 
 */
package org.columba.mail.search;

import org.columba.core.filter.FilterCriteria;
import org.columba.core.search.api.ISearchProvider;
import org.columba.mail.filter.MailFilterFactory;

/**
 * @author frd
 * 
 */
public class SubjectContainsSearchProvider extends AbstractMailSearchProvider implements
		ISearchProvider {

	public SubjectContainsSearchProvider() {
		super();
	}

	/**
	 * @see org.columba.core.search.api.ISearchProvider#getName()
	 */
	public String getName() {
		return "subject_contains";
	}

	/**
	 * @see org.columba.core.search.api.ISearchProvider#getNamespace()
	 */
	public String getNamespace() {
		return "org.columba.mail";
	}


	@Override
	protected FilterCriteria createFilterCriteria(String searchTerm) {
		FilterCriteria criteria = MailFilterFactory
				.createSubjectContains(searchTerm);
		return criteria;
	}

}
