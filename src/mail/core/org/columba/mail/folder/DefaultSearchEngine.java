package org.columba.mail.folder;

import java.util.LinkedList;
import java.util.Vector;

import org.columba.core.command.WorkerStatusController;
import org.columba.mail.filter.FilterRule;

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
