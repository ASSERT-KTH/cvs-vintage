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
public class SizeFilter extends AbstractFilter {

	/**
	 * Constructor for SizeFilter.
	 */
	public SizeFilter() {
		super();
	}

	/**
	 * @see org.columba.mail.filter.plugins.AbstractFilter#getAttributes()
	 */
	public Object[] getAttributes() {
		Object[] args = { "criteria", "pattern" };

		return args;
	}

	protected Integer transformSize(String pattern) {
		Integer searchPattern = Integer.valueOf(pattern);
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
		Integer size = transformSize((String) args[1]);

		
		Integer s = (Integer) header.get("columba.size");
		
		
		if (s == null)
			return false;

		switch (condition) {
			case FilterCriteria.SIZE_SMALLER :
				{
					if (size.compareTo(s) > 0)
						result = true;
					break;
				}
			case FilterCriteria.SIZE_BIGGER :
				{
					if (size.compareTo(s) < 0)
						result = true;
					break;
				}
		}

		return result;
	}

}
