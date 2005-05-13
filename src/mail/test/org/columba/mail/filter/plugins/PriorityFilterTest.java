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

import org.columba.mail.filter.MailFilterCriteria;
import org.columba.mail.filter.MailFilterFactory;
import org.columba.mail.folder.MailboxTstFactory;

/**
 * @author fdietz
 *  
 */
public class PriorityFilterTest extends AbstractFilterTst {

	/**
	 *  
	 */
	public PriorityFilterTest(String arg0) {
		super(arg0);

	}

	/**
	 *  
	 */
	public PriorityFilterTest(MailboxTstFactory factory, String arg0) {
		super(factory, arg0);

	}

	public void testHighest() throws Exception {
		// add message to folder
		Object uid = addMessage();

		getSourceFolder().setAttribute(uid, "columba.priority", new Integer(1));

		// create filter configuration
		MailFilterCriteria criteria = MailFilterFactory.createHighestPriority();

		// create filter
		PriorityFilter filter = new PriorityFilter();

		// init configuration
		filter.setUp(criteria);

		// execute filter
		boolean result = filter.process(getSourceFolder(), uid);
		assertEquals("filter result", true, result);
	}

	public void testNormal() throws Exception {
		// add message to folder
		Object uid = addMessage();

		getSourceFolder().setAttribute(uid, "columba.priority", new Integer(3));

		// create filter configuration
		MailFilterCriteria criteria = MailFilterFactory.createNormalPriority();

		// create filter
		PriorityFilter filter = new PriorityFilter();

		// init configuration
		filter.setUp(criteria);

		// execute filter
		boolean result = filter.process(getSourceFolder(), uid);
		assertEquals("filter result", true, result);
	}

	public void testLowest() throws Exception {
		// add message to folder
		Object uid = addMessage();

		getSourceFolder().setAttribute(uid, "columba.priority", new Integer(5));

		// create filter configuration
		MailFilterCriteria criteria = MailFilterFactory.createLowestPriority();

		// create filter
		PriorityFilter filter = new PriorityFilter();

		// init configuration
		filter.setUp(criteria);

		// execute filter
		boolean result = filter.process(getSourceFolder(), uid);
		assertEquals("filter result", true, result);
	}
}