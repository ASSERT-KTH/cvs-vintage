package org.columba.mail.folder;

import org.columba.core.command.WorkerStatusController;
import org.columba.mail.filter.Filter;
import org.columba.mail.message.AbstractMessage;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public interface SearchEngineInterface {
	public Object[] searchMessages(
		Filter filter,
		Object[] uids,
		WorkerStatusController worker)
		throws Exception;
		
	public Object[] searchMessages(
		Filter filter,
		WorkerStatusController worker)
		throws Exception;
		
	public void messageAdded( AbstractMessage message);
	
	public void messageRemoved( Object uid);
}
