//The contents of this file are subject to the Mozilla Public License Version 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003. 
//
//All Rights Reserved.

package org.columba.mail.folder.search;

import java.util.Arrays;
import java.util.List;

import org.columba.core.command.WorkerStatusController;
import org.columba.mail.filter.FilterRule;
import org.columba.mail.folder.Folder;
import org.columba.mail.message.AbstractMessage;

/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class LocalSearchEngine extends AbstractSearchEngine {

	private final static String[] caps =
		{
			"Body",
			"Subject",
			"From",
			"To",
			"Cc",
			"Bcc",
			"Custom Headerfield",
			"Date",
			"Flags",
			"Priority",
			"Size" };

	/**
	 * @param folder
	 */
	public LocalSearchEngine(Folder folder) {
		super(folder);

	}

	/* (non-Javadoc)
	 * @see org.columba.mail.folder.search.AbstractSearchEngine#getCaps()
	 */
	public String[] getCaps() {
		return caps;
	}

	/* (non-Javadoc)
	 * @see org.columba.mail.folder.search.AbstractSearchEngine#queryEngine(org.columba.mail.filter.FilterRule, org.columba.core.command.WorkerStatusController)
	 */
	protected List queryEngine(
		FilterRule filter,
		WorkerStatusController worker)
		throws Exception {
			
		Object[] uids = folder.getUids(worker);
		
		return processCriteria(filter, Arrays.asList(uids), worker);
	}

	/* (non-Javadoc)
	 * @see org.columba.mail.folder.search.AbstractSearchEngine#queryEngine(org.columba.mail.filter.FilterRule, java.lang.Object[], org.columba.core.command.WorkerStatusController)
	 */
	protected List queryEngine(
		FilterRule filter,
		Object[] uids,
		WorkerStatusController worker)
		throws Exception {
			
		return processCriteria(filter, Arrays.asList(uids), worker);
	}

	/* (non-Javadoc)
	 * @see org.columba.mail.folder.search.AbstractSearchEngine#sync(org.columba.core.command.WorkerStatusController)
	 */
	public void sync(WorkerStatusController wc) throws Exception {

	}

	/* (non-Javadoc)
	 * @see org.columba.mail.folder.search.AbstractSearchEngine#messageAdded(org.columba.mail.message.AbstractMessage)
	 */
	public void messageAdded(AbstractMessage message) throws Exception {

	}

	/* (non-Javadoc)
	 * @see org.columba.mail.folder.search.AbstractSearchEngine#messageRemoved(java.lang.Object)
	 */
	public void messageRemoved(Object uid) throws Exception {

	}
}
