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
public class HeaderfieldFilter extends AbstractFilter{

	/**
	 * Constructor for SubjectFilter.
	 * @param filter
	 */
	public HeaderfieldFilter() {

	}

	public Object[] getAttributes() {
		Object[] args = { "headerfield", "criteria", "pattern" };

		return args;
	}

	/**
	 * @see org.columba.mail.filter.plugins.AbstractFilter#process(org.columba.mail.folder.Folder, java.lang.Object, org.columba.mail.filter.Filter)
	 */
	public boolean process(
		Object[] args,
		Folder folder,
		Object uid,
		WorkerStatusController worker)
		throws Exception {
		HeaderInterface header = folder.getMessageHeader(uid, worker);

		String headerItem = (String) header.get((String) args[0]);
		int condition = FilterCriteria.getCriteria((String) args[1]);
		String pattern = (String) args[2];

		boolean result = match(headerItem, condition, pattern);

		return result;

	}

	protected boolean match(String headerItem, int condition, String pattern) {
		boolean result = false;

		if ( headerItem == null ) return false;
	
		switch (condition) {
			case FilterCriteria.CONTAINS :
				{
					if (headerItem.indexOf(pattern.toLowerCase()) != -1)
						result = true;

					break;

				}
			case FilterCriteria.CONTAINS_NOT :
				{
					if (headerItem.indexOf(pattern.toLowerCase()) == -1)
						result = true;

					break;
				}
			case FilterCriteria.IS :
				{
					if (headerItem.equalsIgnoreCase(pattern))
						result = true;

					break;

				}
			case FilterCriteria.IS_NOT :
				{
					if (!headerItem.equalsIgnoreCase(pattern))
						result = true;

					break;

				}
			case FilterCriteria.BEGINS_WITH :
				{
					if (headerItem.startsWith(pattern.toLowerCase()))
						result = true;

					break;

				}
			case FilterCriteria.ENDS_WITH :
				{
					if (headerItem.endsWith(pattern.toLowerCase()))
						result = true;

					break;

				}
		}

		return result;
	}

}
