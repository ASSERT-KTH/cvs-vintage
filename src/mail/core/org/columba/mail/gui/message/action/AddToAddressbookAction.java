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
import java.net.URL;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JFrame;

import org.columba.addressbook.gui.tree.util.SelectAddressbookFolderDialog;
import org.columba.addressbook.main.AddressbookInterface;
import org.columba.addressbook.model.Contact;
import org.columba.core.action.AbstractColumbaAction;
import org.columba.core.gui.frame.FrameMediator;
import org.columba.core.gui.util.ImageLoader;
import org.columba.core.main.MainInterface;
import org.columba.mail.gui.frame.MessageViewOwner;
import org.columba.mail.gui.message.URLObservable;
import org.columba.mail.gui.message.util.ColumbaURL;
import org.columba.mail.util.MailResourceLoader;


/**
 * Add address to addressbook.
 *
 * @author fdietz
 */
public class AddToAddressbookAction extends AbstractColumbaAction
    implements Observer {
		ColumbaURL url = null;
		
    /**
 *
 */
    public AddToAddressbookAction(FrameMediator controller) {
        super(controller,
            MailResourceLoader.getString("menu", "mainframe",
                "viewer_addressbook"));

        setEnabled(false);

        putValue(SMALL_ICON, ImageLoader.getSmallImageIcon("contact_small.png"));

        //		listen for URL changes
        ((MessageViewOwner) controller).getMessageController().getUrlObservable()
                                                                    .addObserver(this);
    }

    /* (non-Javadoc)
 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
 */
    public void actionPerformed(ActionEvent evt) {
        SelectAddressbookFolderDialog dialog = AddressbookInterface.addressbookTreeModel.getSelectAddressbookFolderDialog();

        org.columba.addressbook.folder.AbstractFolder selectedFolder = dialog.getSelectedFolder();

        if (selectedFolder == null) {
            return;
        }

        try {
            Contact card = new Contact();
            card.set("displayname", url.getSender());
            card.set("email", "internet", url.getEmailAddress());

            selectedFolder.add(card);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /* (non-Javadoc)
 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
 */
    public void update(Observable arg0, Object arg1)
    {

      url = ((URLObservable) arg0).getUrl();

      // only enable this action, if this is a mailto: URL
      setEnabled(url.isMailTo());
      
    }
}
