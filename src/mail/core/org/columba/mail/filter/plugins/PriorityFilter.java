package org.columba.mail.filter.plugins;

import org.columba.core.command.WorkerStatusController;
import org.columba.mail.filter.FilterCriteria;
import org.columba.mail.folder.Folder;
import org.columba.mail.message.HeaderInterface;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class PriorityFilter extends AbstractFilter {

	/**
	 * Constructor for PriorityFilter.
	 */
	public PriorityFilter() {
		super();
	}

	/**
	 * @see org.columba.mail.filter.plugins.AbstractFilter#getAttributes()
	 */
	public Object[] getAttributes() {
		Object[] args = { "criteria", "pattern" };

		return args;
	}

	protected Integer transformPriority(String pattern) {
		Integer searchPattern = new Integer(3);

		if (pattern.equalsIgnoreCase("Highest")) {
			searchPattern = new Integer(1);
		} else if (pattern.equalsIgnoreCase("High")) {
			searchPattern = new Integer(2);
		} else if (pattern.equalsIgnoreCase("Normal")) {
			searchPattern = new Integer(3);
		} else if (pattern.equalsIgnoreCase("Low")) {
			searchPattern = new Integer(4);
		} else if (pattern.equalsIgnoreCase("Lowest")) {
			searchPattern = new Integer(5);
		}

		//Integer priority = Integer.valueOf(pattern);
		//return priority;
		return searchPattern;
	}

	/**
	 * @see org.columba.mail.filter.plugins.AbstractFilter#process(java.lang.Object, org.columba.mail.folder.Folder, java.lang.Object, org.columba.core.command.WorkerStatusController)
	 */
	public boolean process(
		Object[] args,
		Folder folder,
		Object uid,
		WorkerStatusController worker)
		throws Exception {

		boolean result = false;

		HeaderInterface header = folder.getMessageHeader(uid, worker);

		int condition = FilterCriteria.getCriteria((String) args[0]);
		String s = (String) args[1];
		Integer searchPattern = transformPriority(s);

		Integer priority = (Integer) header.get("columba.priority");
		if (priority == null)
			return false;

		switch (condition) {

			case FilterCriteria.IS :
				{
					if (priority.compareTo(searchPattern) == 0)
						result = true;

					break;

				}
			case FilterCriteria.IS_NOT :
				{
					if (priority.compareTo(searchPattern) != 0)
						result = true;

					break;

				}

		}

		return result;
	}

}
