package org.columba.mail.mailchecking;

import java.util.EventListener;

/**
 * Get notified if new messages arrive.
 * 
 * @author frd
 */
public interface IMailCheckingListener extends EventListener{

	/**
	 * Method is executed whenever a new messages arrives, being it POP3 or
	 * IMAP.
	 * 
	 * @param event	mail checking event provides context information
	 */
	public void newMessageArrived(IMailCheckingEvent event);
}
