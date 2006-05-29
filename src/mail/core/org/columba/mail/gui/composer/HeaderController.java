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
import java.util.Vector;

import javax.swing.JOptionPane;

import org.columba.addressbook.facade.IContactFacade;
import org.columba.addressbook.facade.IFolder;
import org.columba.addressbook.facade.IFolderFacade;
import org.columba.addressbook.facade.IHeaderItem;
import org.columba.addressbook.facade.IModelFacade;
import org.columba.api.exception.ServiceNotFoundException;
import org.columba.api.exception.StoreException;
import org.columba.mail.connector.ServiceConnector;
import org.columba.mail.gui.composer.util.AddressCollector;
import org.columba.mail.parser.ListBuilder;
import org.columba.mail.parser.ListParser;
import org.columba.mail.util.MailResourceLoader;

/**
 * Recipients editor component.
 * 
 * @author fdietz
 */
public class HeaderController {

	private ComposerController controller;

	private HeaderView view;

	private AddressCollector addressCollector;

	public HeaderController(ComposerController controller) {
		this.controller = controller;

		view = new HeaderView(this);

		addressCollector = AddressCollector.getInstance();

		if (addressCollector != null) {
			// clear autocomplete hashmap
			addressCollector.clear();

			try {
				IContactFacade facade = ServiceConnector.getContactFacade();
				IFolderFacade folderFacade = ServiceConnector.getFolderFacade();

				// fill hashmap with all available contacts and groups
				try {
					List<IFolder> list = folderFacade.getAllFolders();
					Iterator<IFolder> it = list.iterator();
					while (it.hasNext()) {
						IFolder f = it.next();

						List<IHeaderItem> l = facade.getAllHeaderItems(f
								.getId(), false);
						addressCollector.addAllContacts(l, true);
					}

				} catch (StoreException e) {
					e.printStackTrace();
				}

			} catch (ServiceNotFoundException e) {
			}

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

		JOptionPane.showMessageDialog(null, MailResourceLoader.getString(
				"menu", "mainframe", "composer_no_recipients_found"));

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
		// view.table.getModel().addTableModelListener(this);
	}

	public void updateComponents(boolean b) {
		if (b) {

			String s = ListParser.createStringFromList(controller.getModel()
					.getToList(), ",");
			getView().getToComboBox().setText(s);

			s = ListParser.createStringFromList(controller.getModel()
					.getCcList(), ",");
			getView().getCcComboBox().setText(s);

			s = ListParser.createStringFromList(controller.getModel()
					.getBccList(), ",");
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

	private List<IHeaderItem> getHeaderItemList(int recipient) {

		List<IHeaderItem> list = new Vector<IHeaderItem>();

		String str = null;
		switch (recipient) {
		case 0:
			str = getView().getToComboBox().getText();

			break;
		case 1:
			str = getView().getCcComboBox().getText();

			break;
		case 2:
			str = getView().getBccComboBox().getText();

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
			if (addressCollector != null)
				item = addressCollector.getHeaderItem(s);
			if (item == null) {

				try {
					IModelFacade c = ServiceConnector.getModelFacade();
					item = c.createContactItem();
					item.setName(s);
				} catch (ServiceNotFoundException e) {

					e.printStackTrace();
				}

			}

			list.add(item);
		}

		return list;
	}

	public List<IHeaderItem> getToHeaderItemList() {
		return getHeaderItemList(0);
	}

	public List<IHeaderItem> getCcHeaderItemList() {
		return getHeaderItemList(1);
	}

	public List<IHeaderItem> getBccHeaderItemList() {
		return getHeaderItemList(2);
	}

	public void setToHeaderItemList(List<IHeaderItem> list) {
		List<String> stringList = ListBuilder
				.createStringListFromItemList(list);

		((ComposerModel) controller.getModel()).setToList(stringList);
	}

	public void setCcHeaderItemList(List<IHeaderItem> list) {
		((ComposerModel) controller.getModel()).setCcList(ListBuilder
				.createStringListFromItemList(list));
	}

	public void setBccHeaderItemList(List<IHeaderItem> list) {
		((ComposerModel) controller.getModel()).setBccList(ListBuilder
				.createStringListFromItemList(list));
	}

	// public void setHeaderItemLists(List<IHeaderItem>[] lists) {
	// ((ComposerModel) controller.getModel()).setToList(ListBuilder
	// .createStringListFromItemList(lists[0]));
	//
	// ((ComposerModel) controller.getModel()).setCcList(ListBuilder
	// .createStringListFromItemList(lists[1]));
	//
	// ((ComposerModel) controller.getModel()).setBccList(ListBuilder
	// .createStringListFromItemList(lists[2]));
	//
	// updateComponents(true);
	// }

}