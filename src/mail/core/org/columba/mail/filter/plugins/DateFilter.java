package org.columba.mail.filter.plugins;

import java.util.Date;

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
public class DateFilter extends AbstractFilter {

	/**
	 * Constructor for DateFilter.
	 */
	public DateFilter() {
		super();
		System.out.println("test");
	}
	
	public void test()
	{
		System.out.println("tes2");
	}

	/**
	 * @see org.columba.mail.filter.plugins.AbstractFilter#getAttributes()
	 */
	public Object[] getAttributes() {
		Object[] args = { "criteria", "pattern" };

		return args;
	}

	protected Date transformDate(String pattern) {
		java.text.DateFormat df = java.text.DateFormat.getDateInstance();
		Date searchPattern = null;
		try {
			searchPattern = df.parse(pattern);
		} catch (java.text.ParseException ex) {
			System.out.println("exception: " + ex.getMessage());
			ex.printStackTrace();

			//return new Vector();
		}
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
		HeaderInterface header = folder.getMessageHeader(uid, worker);

		int condition = FilterCriteria.getCriteria((String) args[0]);
		Date date = transformDate((String) args[1]);

		boolean result = false;

		Date d = (Date) header.get("columba.date");
		if (d == null)
			return false;

		switch (condition) {
			case FilterCriteria.DATE_BEFORE :
				{
					if (date.before(d))
						result = true;
					break;
				}
			case FilterCriteria.DATE_AFTER :
				{
					if (date.after(d))
						result = true;
					break;
				}
		}

		return false;
	}

}
