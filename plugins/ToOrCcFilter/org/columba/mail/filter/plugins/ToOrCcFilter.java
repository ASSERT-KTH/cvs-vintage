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
public class ToOrCcFilter extends HeaderfieldFilter {

	/**
	 * Constructor for ToOrCcFilter.
	 */
	public ToOrCcFilter() {
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

		HeaderInterface header = folder.getMessageHeader(uid, worker);

		int condition = FilterCriteria.getCriteria((String) args[0]);
		String pattern = (String) args[1];

		String to = (String) header.get("To");
		String cc = (String) header.get("Cc");
				
		boolean result = match(to, condition, pattern);

		result |= match(cc, condition, pattern);

		return result;
	}

	
}
