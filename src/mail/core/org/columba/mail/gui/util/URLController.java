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
package org.columba.mail.gui.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.columba.addressbook.folder.ContactCard;
import org.columba.addressbook.gui.tree.util.SelectAddressbookFolderDialog;
import org.columba.core.main.MainInterface;
import org.columba.mail.gui.composer.ComposerController;
import org.columba.mail.gui.composer.ComposerModel;
import org.columba.mail.gui.mimetype.MimeTypeViewer;

public class URLController implements ActionListener {

	private String address;
	private URL link;

	public JPopupMenu createContactMenu(String contact) {
		JPopupMenu popup = new JPopupMenu();
		JMenuItem menuItem = new JMenuItem("Add Contact to Addressbook");
		menuItem.addActionListener(this);
		menuItem.setActionCommand("CONTACT");
		popup.add(menuItem);
		menuItem = new JMenuItem("Compose Message for " + contact);
		menuItem.setActionCommand("COMPOSE");
		menuItem.addActionListener(this);
		popup.add(menuItem);

		return popup;
	}

	public JPopupMenu createLinkMenu() {
		JPopupMenu popup = new JPopupMenu();
		JMenuItem menuItem = new JMenuItem("Open");
		menuItem.addActionListener(this);
		menuItem.setActionCommand("OPEN");
		popup.add(menuItem);
		menuItem = new JMenuItem("Open with...");
		menuItem.setActionCommand("OPEN_WITH");
		menuItem.addActionListener(this);
		popup.add(menuItem);
		popup.addSeparator();
		menuItem = new JMenuItem("Open with internal browser");
		menuItem.setActionCommand("OPEN_WITHINTERNALBROWSER");
		menuItem.addActionListener(this);
		popup.add(menuItem);

		return popup;
	}

	public void setAddress(String s) {
		this.address = s;
	}

	public String getAddress() {
		return address;
	}

	public URL getLink() {
		return link;
	}

	public void setLink(URL u) {
		this.link = u;
	}

	public void compose(String address) {
		ComposerModel model = new ComposerModel();

		ComposerController controller = new ComposerController();

		((ComposerModel) controller.getModel()).setTo(address);

		controller.setComposerModel(model);
	}

	public void contact(String address) {
		SelectAddressbookFolderDialog dialog =
			MainInterface
				.addressbookTreeModel
				.getSelectAddressbookFolderDialog();

		org.columba.addressbook.folder.Folder selectedFolder =
			dialog.getSelectedFolder();

		if (selectedFolder == null)
			return;

		try {

			ContactCard card = new ContactCard();
			card.set("displayname", address);
			card.set("email", "internet", address);

			selectedFolder.add(card);

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	public JPopupMenu createMenu(URL url) {
		if (url.getProtocol().equalsIgnoreCase("mailto")) {
			// found email address

			setAddress(url.getFile());
			JPopupMenu menu = createContactMenu(url.getFile());
			return menu;

		} else {

			setLink(url);
			JPopupMenu menu = createLinkMenu();
			return menu;
		}
	}

	public void open(URL url) {
		MimeTypeViewer viewer = new MimeTypeViewer();
		viewer.openURL(url);
	}

	public void openWith(URL url) {
		MimeTypeViewer viewer = new MimeTypeViewer();
		viewer.openWithURL(url);
	}

	/*
	public void openWithBrowser(URL url) {
		MimeTypeViewer viewer = new MimeTypeViewer();
		viewer.openWithBrowserURL(url);
	}
	*/
	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();
		if (action.equals("COMPOSE")) {
			compose(getAddress());
		} else if (action.equals("CONTACT")) {
			contact(getAddress());
		} else if (action.equals("OPEN")) {
			open(getLink());
		} else if (action.equals("OPEN_WITH")) {
			openWith(getLink());
		} else if (action.equals("OPEN_WITHINTERNALBROWSER")) {
			//openWithBrowser(getLink());
		}
	}
}
