package org.columba.mail.folder;

import java.util.Date;

import org.columba.core.command.WorkerStatusController;
import org.columba.mail.filter.FilterCriteria;
import org.columba.mail.message.AbstractMessage;
import org.columba.mail.message.ColumbaHeader;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class LocalSearchEngine extends AbstractLocalSearchEngine {

	public LocalSearchEngine(Folder folder) {
		super(folder);
	}

	/**************************** filter methods ***********************/

	public boolean processHeader(
		AbstractMessage message,
		String headerField,
		String pattern,
		int condition,
		WorkerStatusController worker)
		throws Exception {
		boolean result = false;

		ColumbaHeader header = (ColumbaHeader) message.getHeader();

		String headerItem = (String) header.get(headerField);
		if (headerItem == null)
			return false;

		headerItem = headerItem.toLowerCase();

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

	public boolean processDate(
		AbstractMessage message,
		Date date,
		int condition,
		WorkerStatusController worker)
		throws Exception {
		boolean result = false;

		ColumbaHeader header = (ColumbaHeader) message.getHeader();

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

		return result;
	}

	public boolean processSize(
		AbstractMessage message,
		Integer size,
		int condition,
		WorkerStatusController worker)
		throws Exception {
		boolean result = false;

		ColumbaHeader header = (ColumbaHeader) message.getHeader();

		Integer s = (Integer) header.get("columba.size");
		if (s == null)
			return false;

		switch (condition) {
			case FilterCriteria.SIZE_SMALLER :
				{
					if (size.compareTo(s) < 0)
						result = true;
					break;
				}
			case FilterCriteria.SIZE_BIGGER :
				{
					if (size.compareTo(s) > 0)
						result = true;
					break;
				}
		}

		return result;
	}

	public boolean processFlags(
		AbstractMessage message,
		String pattern,
		int condition,
		WorkerStatusController worker)
		throws Exception {

		boolean result = false;

		ColumbaHeader header = (ColumbaHeader) message.getHeader();

		Boolean flags = (Boolean) header.get(pattern);
		if (flags == null)
			return false;

		switch (condition) {

			case FilterCriteria.IS :
				{
					if (flags.equals(Boolean.TRUE))
						result = true;

					break;

				}
			case FilterCriteria.IS_NOT :
				{
					if (flags.equals(Boolean.FALSE))
						result = true;

					break;

				}

		}

		return result;
	}

	public boolean processPriority(
		AbstractMessage message,
		Integer searchPattern,
		int condition,
		WorkerStatusController worker)
		throws Exception {
		boolean result = false;

		ColumbaHeader header = (ColumbaHeader) message.getHeader();

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

	public boolean processBody(
		AbstractMessage message,
		String pattern,
		int condition,
		WorkerStatusController worker)
		throws Exception {
		boolean result = false;
		String bodyText = pattern;

		ColumbaHeader header = (ColumbaHeader) message.getHeader();
		Object uid = header.get("columba.uid");

		String body = message.getSource();
		if (body == null) {
			try {
				body = folder.getMessageSource(uid, null);
			} catch (Exception ex) {

				ex.printStackTrace();
			}
		}

		if (body == null)
			return false;

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