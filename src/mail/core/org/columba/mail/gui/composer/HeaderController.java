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
package org.columba.mail.gui.composer;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.columba.addressbook.facade.IContactFacade;
import org.columba.addressbook.facade.IModelFacade;
import org.columba.addressbook.gui.autocomplete.IAddressCollector;
import org.columba.addressbook.model.IHeaderItem;
import org.columba.addressbook.model.IHeaderItemList;
import org.columba.api.exception.ServiceNotFoundException;
import org.columba.core.gui.dialog.NotifyDialog;
import org.columba.mail.connector.ServiceConnector;
import org.columba.mail.parser.ListBuilder;
import org.columba.mail.parser.ListParser;
import org.columba.mail.util.MailResourceLoader;

/**
 * Recipients editor component.
 * 
 * @author fdietz
 */
public class HeaderController {

	/** JDK 1.4+ logging framework logger, used for logging. */
	private static final Logger LOG = Logger
			.getLogger("org.columba.mail.gui.composer");

	private ComposerController controller;

	private HeaderView view;

	private IAddressCollector addressCollector;

	public HeaderController(ComposerController controller) {
		this.controller = controller;

		view = new HeaderView(this);

		//view.getTable().addKeyListener(this);

		IContactFacade facade;
		try {
			facade = ServiceConnector.getContactFacade();
			addressCollector = facade.getAddressCollector();
		} catch (ServiceNotFoundException e) {
			e.printStackTrace();
		}

		if (addressCollector != null) {
			// clear autocomplete hashmap
			addressCollector.clear();

			// fill hashmap with all available contacts and groups
			addressCollector.addAllContacts(101, true);
			addressCollector.addAllContacts(102, true);
		}
		view.initAutocompletion();

	}

	public ComposerController getComposerController() {
		return controller;
	}

	public HeaderView getView() {
		return view;
	}

	public boolean checkState() {

		Iterator it = getHeaderItemList(0).iterator();

		while (it.hasNext()) {
			IHeaderItem item = (IHeaderItem) it.next();
			if (isValid(item))
				return true;
		}

		NotifyDialog dialog = new NotifyDialog();
		dialog.showDialog(MailResourceLoader.getString("menu", "mainframe",
				"composer_no_recipients_found")); //$NON-NLS-1$

		return false;
	}

	protected boolean isValid(IHeaderItem headerItem) {
		if (headerItem.isContact()) {
			/*
			 * String address = (String) headerItem.get("email;internet");
			 * 
			 * if (AddressParser.isValid(address)) { return true; }
			 * 
			 * address = (String) headerItem.get("displayname");
			 * 
			 * if (AddressParser.isValid(address)) { return true; }
			 */
			return true;
		} else {
			return true;
		}

	}

	public void installListener() {
		//view.table.getModel().addTableModelListener(this);
	}

	public void updateComponents(boolean b) {
		if (b) {

			String s = ListParser.createStringFromList(controller.getModel()
					.getToList());
			getView().getToComboBox().setText(s);

			s = ListParser.createStringFromList(controller.getModel()
					.getCcList());
			getView().getCcComboBox().setText(s);

			s = ListParser.createStringFromList(controller.getModel()
					.getBccList());
			getView().getBccComboBox().setText(s);

		} else {

			String s = getView().getToComboBox().getText();
			List list = ListParser.createListFromString(s);
			controller.getModel().setToList(list);

			s = getView().getCcComboBox().getText();
			list = ListParser.createListFromString(s);
			controller.getModel().setCcList(list);

			s = getView().getBccComboBox().getText();
			list = ListParser.createListFromString(s);
			controller.getModel().setBccList(list);

		}
	}

	private IHeaderItemList getHeaderItemList(int recipient) {
		
		IHeaderItemList list=null;
		try {
			IModelFacade c = ServiceConnector.getModelFacade();
			list = c.createHeaderItemList();
		} catch (ServiceNotFoundException e1) {
			e1.printStackTrace();
		}
		
		String header = null;
		String str = null;
		switch (recipient) {
		case 0:
			str = getView().getToComboBox().getText();
			header = "To";
			break;
		case 1:
			str = getView().getCcComboBox().getText();
			header = "Cc";
			break;
		case 2:
			str = getView().getBccComboBox().getText();
			header = "Bcc";
			break;

		}

		List l = ListParser.createListFromString(str);
		if (l == null)
			return list;

		Iterator it = l.iterator();

		while (it.hasNext()) {
			String s = (String) it.next();
			// skip empty strings
			if (s.length() == 0)
				continue;

				
			IHeaderItem item = null;
			if ( addressCollector != null) item = addressCollector.getHeaderItem(s);
			if (item == null) {
				
				try {
					IModelFacade c = ServiceConnector.getModelFacade();
					item = c.createContactItem();
					item.setDisplayName(s);
					item.setHeader(header);
				} catch (ServiceNotFoundException e) {
					
					e.printStackTrace();
				}
				
				
				
			} else {
				item.setHeader(header);
			}

			list.add(item);
		}

		return list;
	}

	public IHeaderItemList[] getHeaderItemLists() {
		IHeaderItemList[] lists = new IHeaderItemList[3];
		lists[0] = getHeaderItemList(0);
		lists[1] = getHeaderItemList(1);
		lists[2] = getHeaderItemList(2);

		return lists;
	}

	public void setHeaderItemLists(IHeaderItemList[] lists) {
		((ComposerModel) controller.getModel()).setToList(ListBuilder
				.createStringListFromItemList(lists[0]));

		((ComposerModel) controller.getModel()).setCcList(ListBuilder
				.createStringListFromItemList(lists[1]));

		((ComposerModel) controller.getModel()).setBccList(ListBuilder
				.createStringListFromItemList(lists[2]));

		updateComponents(true);
	}

}