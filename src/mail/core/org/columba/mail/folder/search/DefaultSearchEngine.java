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

import java.util.LinkedList;
import java.util.Vector;

import org.columba.core.command.WorkerStatusController;
import org.columba.mail.filter.FilterRule;
import org.columba.mail.folder.Folder;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class DefaultSearchEngine extends AbstractSearchEngine {

	private static final String[] noCaps = {};
	

	public DefaultSearchEngine(Folder folder) {
		super(folder);
	}

	/********************** helper classes ****************************/

	// only for debugging
	protected void printList(Vector v) {
		System.out.println("print list:");

		for (int i = 0; i < v.size(); i++) {
			System.out.println(i + "->" + v.get(i));
		}
	}

	/**
	 * @see org.columba.mail.folder.DefaultSearchEngine#getCaps()
	 */
	public String[] getCaps() {
		return noCaps;
	}


	/**
	 * @see org.columba.mail.folder.AbstractSearchEngine#queryEngine(org.columba.mail.filter.FilterRule, java.lang.Object, org.columba.core.command.WorkerStatusController)
	 */
	protected LinkedList queryEngine(
		FilterRule filter,
		Object[] uids,
		WorkerStatusController worker)
		throws Exception {
		return null;
	}

	/**
	 * @see org.columba.mail.folder.AbstractSearchEngine#queryEngine(org.columba.mail.filter.FilterRule, org.columba.core.command.WorkerStatusController)
	 */
	protected LinkedList queryEngine(
		FilterRule filter,
		WorkerStatusController worker)
		throws Exception {
		return null;
	}

}
