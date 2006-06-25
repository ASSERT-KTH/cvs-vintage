package org.columba.addressbook.facade;

import java.net.URI;

public interface IDialogFacade {

	/**
	 * 
	 * @param location	example: "columba://org.columba.contact/<folder-id>/<contact-id>"
	 */
	public void openContactDialog(URI location);
}
