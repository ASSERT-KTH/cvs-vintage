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

import org.columba.addressbook.gui.tree.AddressbookTreeModel;
import org.columba.addressbook.gui.tree.util.SelectAddressbookFolderDialog;
import org.columba.addressbook.model.Contact;
import org.columba.addressbook.model.VCARD;
import org.columba.core.gui.frame.FrameModel;
import org.columba.core.plugin.PluginLoadingFailedException;
import org.columba.core.xml.XmlElement;
import org.columba.mail.gui.composer.ComposerController;
import org.columba.mail.gui.composer.ComposerModel;
import org.columba.mail.gui.mimetype.MimeTypeViewer;
import org.columba.mail.main.MailInterface;

public class URLController implements ActionListener {
    private String address;
    private URL link;

    //TODO (@author fdietz): i18n
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

    //TODO (@author fdietz): i18n
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
        model.setTo(address);

        // init model to html or text according to stored option		
        XmlElement optionsElement = MailInterface.config.get("composer_options")
                                                        .getElement("/options");
        XmlElement htmlElement = optionsElement.getElement("html");

        if (htmlElement == null) {
            htmlElement = optionsElement.addSubElement("html");
        }

        String enableHtml = htmlElement.getAttribute("enable", "false");
        model.setHtml(Boolean.valueOf(enableHtml).booleanValue());

        try {
            ComposerController controller = (ComposerController)
FrameModel.getInstance().openView("Composer");
            controller.setComposerModel(model);
        } catch (PluginLoadingFailedException plfe) {} //should not occur
    }

    public void contact(String address) {
        //TODO (@author fdietz): remove dependency to addressbook here
        SelectAddressbookFolderDialog dialog = 
        	AddressbookTreeModel.getInstance().getSelectAddressbookFolderDialog();

        org.columba.addressbook.folder.AbstractFolder selectedFolder = dialog.getSelectedFolder();

        if (selectedFolder == null) {
            return;
        }

        try {
            Contact card = new Contact();
            card.set(VCARD.DISPLAYNAME, address);
            card.set(VCARD.EMAIL, address);
            
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
