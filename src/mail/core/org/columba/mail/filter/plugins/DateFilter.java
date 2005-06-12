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

import java.util.Date;
import java.util.logging.Logger;

import org.columba.core.filter.AbstractFilter;
import org.columba.core.filter.FilterCriteria;
import org.columba.core.folder.IFolder;
import org.columba.mail.folder.IMailbox;
import org.columba.mail.gui.config.filter.plugins.DateCriteriaRow;

/**
 * 
 * Search for a certain absolute Date
 * 
 * @author fdietz
 */
public class DateFilter extends AbstractFilter {

	/** JDK 1.4+ logging framework logger, used for logging. */
	private static final Logger LOG = Logger
			.getLogger("org.columba.mail.filter.plugins");

	private String pattern;

	private int condition;

	protected Date transformDate(String pattern) {
		Date searchPattern = null;

		try {
			searchPattern = DateCriteriaRow.dateFormat.parse(pattern);
		} catch (java.text.ParseException ex) {
			// should never happen
			LOG.severe("Date unparsable: "+pattern);
			searchPattern = new Date();
		}

		return searchPattern;
	}

	/**
	 * @see org.columba.core.filter.AbstractFilter#process(java.lang.Object,
	 *      org.columba.mail.folder.Folder, java.lang.Object,
	 *      org.columba.core.command.WorkerStatusController)
	 */
	public boolean process(IFolder folder, Object uid)
			throws Exception {

		// transform string to Date representation
		Date date = transformDate(pattern);
		if (date == null)
			return false;

		boolean result = false;

		// get date
		Date d = (Date) ((IMailbox)folder).getAttribute(uid, "columba.date");

		if (d == null) {
			LOG.fine("field date not found");

			return false;
		}

		switch (condition) {
		case FilterCriteria.DATE_BEFORE:

			if (d.before(date)) {
				result = true;
			}

			break;

		case FilterCriteria.DATE_AFTER:

			if (d.after(date)) {
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

		//      convert criteria into int-value
		condition = f.getCriteria();
	}
}