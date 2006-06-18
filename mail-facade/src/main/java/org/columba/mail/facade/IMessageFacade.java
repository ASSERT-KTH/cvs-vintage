package org.columba.mail.facade;

import java.net.URI;

public interface IMessageFacade {

	/**
	 * Open message in new window.
	 * 
	 * @param location	example: "columba://org.columba.mail/<folder-id>/<message-id>"
	 */
	public void openMessage(URI location);
}
