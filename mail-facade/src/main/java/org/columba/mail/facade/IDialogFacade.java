package org.columba.mail.facade;

import java.net.URI;

public interface IDialogFacade {

	/**
	 * Open message in new window.
	 * 
	 * @param location	example: "columba://org.columba.mail/<folder-id>/<message-id>"
	 */
	public void openMessage(URI location);
	
	/**
	 * Open up composer window.
	 */
	void openComposer();
}
