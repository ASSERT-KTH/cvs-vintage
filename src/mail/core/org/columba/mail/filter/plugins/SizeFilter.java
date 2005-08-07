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
package org.columba.mail.filter.plugins;

import org.columba.core.filter.AbstractFilter;
import org.columba.core.filter.FilterCriteria;
import org.columba.core.folder.IFolder;
import org.columba.mail.folder.IMailbox;

/**
 * Find messages with certain size.
 * 
 * @author fdietz
 */
public class SizeFilter extends AbstractFilter {

	private String pattern;

	private int condition;

	/**
	 * Constructor for SizeFilter.
	 */
	public SizeFilter() {
		super();
	}

	/**
	 * Transform string to integer representation
	 * 
	 * @param p
	 *            string containing priority
	 * 
	 * @return integer representation of string
	 */
	protected Integer transformSize(String p) {
		Integer searchPattern = Integer.valueOf(p);

		return searchPattern;
	}

	/**
	 * @see org.columba.core.filter.AbstractFilter#process(java.lang.Object,
	 *      org.columba.mail.folder.Folder, java.lang.Object,
	 *      org.columba.api.command.IWorkerStatusController)
	 */
	public boolean process(IFolder folder, Object uid)
			throws Exception {
		boolean result = false;

		Integer size = transformSize((String) pattern);

		Integer s = (Integer) ((IMailbox)folder).getAttribute(uid, "columba.size");

		if (s == null) {
			return false;
		}

		switch (condition) {
		case FilterCriteria.SIZE_SMALLER:

			if (size.compareTo(s) > 0) {
				result = true;
			}

			break;

		case FilterCriteria.SIZE_BIGGER:

			if (size.compareTo(s) < 0) {
				result = true;
			}

			break;
		}

		return result;
	}

	/**
	 * @see org.columba.core.filter.AbstractFilter#setUp(org.columba.mail.filter.FilterCriteria)
	 */
	public void setUp(FilterCriteria f) {

		// string to search
		pattern = f.getPatternString();

		condition = f.getCriteria();
	}
}