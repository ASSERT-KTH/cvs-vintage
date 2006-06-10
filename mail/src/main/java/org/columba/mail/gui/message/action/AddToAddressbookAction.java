//The contents of this file are subject to the Mozilla Public License Version 1.1
//(the "License"); you may not use this file except in compliance with the
//License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
//for the specific language governing rights and
//limitations under the License.
//
//The Original Code is "The Columba Project"
//
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.mail.gui.message.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.columba.addressbook.facade.IContactFacade;
import org.columba.addressbook.facade.IContactItem;
import org.columba.addressbook.facade.IModelFacade;
import org.columba.api.exception.ServiceNotFoundException;
import org.columba.api.exception.StoreException;
import org.columba.core.resourceloader.IconKeys;
import org.columba.core.resourceloader.ImageLoader;
import org.columba.mail.connector.FacadeUtil;
import org.columba.mail.connector.ServiceConnector;
import org.columba.mail.gui.message.util.ColumbaURL;
import org.columba.mail.util.MailResourceLoader;
import org.columba.ristretto.message.Address;
import org.columba.ristretto.parser.ParserException;

/**
 * Add address to addressbook.
 * 
 * @author fdietz
 */
public class AddToAddressbookAction extends AbstractAction {
	private String emailAddress;

	private ColumbaURL url = null;

	/**
	 * 
	 */
	public AddToAddressbookAction(ColumbaURL url) {
		super(MailResourceLoader.getString("menu", "mainframe",
				"viewer_addressbook"));

		putValue(SMALL_ICON, ImageLoader.getSmallIcon(IconKeys.CONTACT_NEW));
		
		this.url = url;
		setEnabled( url != null);
		if ( url != null)
			setEnabled( url.isMailTo());
	}

	/**
	 * 
	 */
	public AddToAddressbookAction(String emailAddress) {
		super(MailResourceLoader.getString("menu", "mainframe",
				"viewer_addressbook"));

		this.emailAddress = emailAddress;
		setEnabled(emailAddress != null);
		
		putValue(SMALL_ICON, ImageLoader.getSmallIcon(IconKeys.CONTACT_NEW));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {

		IContactFacade contactFacade = null;
		IModelFacade modelFacade = null;
		try {
			contactFacade = ServiceConnector.getContactFacade();
			modelFacade = ServiceConnector.getModelFacade();
		} catch (ServiceNotFoundException e) {
			e.printStackTrace();
			return;
		}

		try {
			Address address = null;
			if (emailAddress != null)
				address = Address.parse(emailAddress);
			else
				// create Address from URL
				address = Address.parse(url.getSender());

			// add contact to addressbook
			IContactItem contactItem = modelFacade.createContactItem();
			FacadeUtil.getInstance().initContactItem(contactItem,
					address.getDisplayName(), address.getMailAddress());
			contactFacade.addContact(contactItem);
		} catch (ParserException e) {
			e.printStackTrace();
		} catch (StoreException e) {
			e.printStackTrace();
		}
	}

}