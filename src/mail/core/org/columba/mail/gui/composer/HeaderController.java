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

import javax.swing.JComponent;

import org.columba.addressbook.gui.autocomplete.AddressCollector;
import org.columba.addressbook.model.ContactItem;
import org.columba.addressbook.model.HeaderItem;
import org.columba.addressbook.model.HeaderItemList;
import org.columba.addressbook.parser.ListParser;
import org.columba.core.gui.focus.FocusOwner;
import org.columba.core.gui.util.NotifyDialog;
import org.columba.core.main.MainInterface;
import org.columba.mail.util.MailResourceLoader;

/**
 * @author frd
 */
public class HeaderController implements FocusOwner {

	/** JDK 1.4+ logging framework logger, used for logging. */
	private static final Logger LOG = Logger
			.getLogger("org.columba.mail.gui.composer");

	ComposerController controller;

	HeaderView view;

	public HeaderController(ComposerController controller) {
		this.controller = controller;

		view = new HeaderView(this);

		//view.getTable().addKeyListener(this);
		// register at focus manager
		MainInterface.focusManager.registerComponent(this);

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
			HeaderItem item = (HeaderItem) it.next();
			if (isValid(item))
				return true;
		}

		NotifyDialog dialog = new NotifyDialog();
		dialog.showDialog(MailResourceLoader.getString("menu", "mainframe",
				"composer_no_recipients_found")); //$NON-NLS-1$

		return false;
	}

	protected boolean isValid(HeaderItem headerItem) {
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

	private HeaderItemList getHeaderItemList(int recipient) {
		HeaderItemList list = new HeaderItemList();

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

		Iterator it = ListParser.createListFromString(str).iterator();

		while (it.hasNext()) {
			String s = (String) it.next();
			// skip empty strings
			if (s.length() == 0)
				continue;

			HeaderItem item = AddressCollector.getInstance().getHeaderItem(s);
			if ( item == null) {
				item = new ContactItem();
				item.setDisplayName(s);
				item.setHeader(header);
			} else {
				item.setHeader(header);
			}
			
			list.add(item);
		}

		return list;
	}

	public HeaderItemList[] getHeaderItemLists() {
		HeaderItemList[] lists = new HeaderItemList[3];
		lists[0] = getHeaderItemList(0);
		lists[1] = getHeaderItemList(1);
		lists[2] = getHeaderItemList(2);

		return lists;
	}

	public void setHeaderItemLists(HeaderItemList[] lists) {
		((ComposerModel) controller.getModel()).setToList(ListParser
				.createStringListFromItemList(lists[0]));

		((ComposerModel) controller.getModel()).setCcList(ListParser
				.createStringListFromItemList(lists[1]));

		((ComposerModel) controller.getModel()).setBccList(ListParser
				.createStringListFromItemList(lists[2]));

		updateComponents(true);
	}

	/** *************** FocusOwner implementation ************************** */
	public void copy() {
		// not supported by gui component
	}

	public void cut() {
		//view.removeSelected();
	}

	public void delete() {
		//view.removeSelected();
	}

	public JComponent getComponent() {
		return getView();
	}

	public boolean isCopyActionEnabled() {
		// not supported by gui component
		return false;
	}

	public boolean isCutActionEnabled() {
		/*
		 * if (view.getSelectedRowCount() > 0) { return true; }
		 */

		return false;
	}

	public boolean isDeleteActionEnabled() {
		/*
		 * if (view.getSelectedRowCount() > 0) { return true; }
		 */

		return false;
	}

	public boolean isPasteActionEnabled() {
		// not supported by gui component
		return false;
	}

	public boolean isRedoActionEnabled() {
		// not supported by gui component
		return false;
	}

	public boolean isSelectAllActionEnabled() {
		return true;
	}

	public boolean isUndoActionEnabled() {
		// not supported by gui component
		return false;
	}

	public void paste() {
		// not supported by gui component
	}

	public void redo() {
		// not supported by gui component
	}

	public void selectAll() {
		/*
		 * view.selectAll();
		 */
	}

	public void undo() {
		// not supported by gui component
	}
}