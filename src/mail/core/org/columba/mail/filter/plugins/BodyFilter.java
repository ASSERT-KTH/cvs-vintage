package org.columba.mail.filter.plugins;

import org.columba.core.command.WorkerStatusController;
import org.columba.mail.filter.FilterCriteria;
import org.columba.mail.folder.Folder;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class BodyFilter extends AbstractFilter {

	/**
	 * Constructor for BodyFilter.
	 */
	public BodyFilter() {
		super();
	}

	/**
	 * @see org.columba.mail.filter.plugins.AbstractFilter#getAttributes()
	 */
	public Object[] getAttributes() {
		Object[] args = { "criteria", "pattern" };

		return args;
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

		String body = folder.getMessageSource(uid, worker);
		int condition = FilterCriteria.getCriteria((String) args[0]);
		String bodyText = (String) args[0];

		boolean result = false;

		switch (condition) {
			case FilterCriteria.CONTAINS :
				{
					if (body.indexOf(bodyText) != -1)
						result = true;

					break;

				}
			case FilterCriteria.CONTAINS_NOT :
				{
					if (body.indexOf(bodyText) == -1)
						result = true;

					break;
				}

		}

		return result;

	}

}
