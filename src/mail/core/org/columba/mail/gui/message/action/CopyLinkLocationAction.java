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

import org.columba.core.action.AbstractColumbaAction;
import org.columba.core.gui.frame.FrameMediator;

import org.columba.mail.gui.frame.MessageViewOwner;
import org.columba.mail.gui.message.URLObservable;
import org.columba.mail.gui.message.util.ColumbaURL;
import org.columba.mail.util.MailResourceLoader;

import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;

import java.net.URL;

import java.util.Observable;
import java.util.Observer;


/**
 * Copy url to clipboard.
 *
 * @author fdietz
 */
public class CopyLinkLocationAction extends AbstractColumbaAction
    implements Observer {
    ColumbaURL url = null;

    /**
 *
 */
    public CopyLinkLocationAction(FrameMediator controller) {
        super(controller,
            MailResourceLoader.getString("menu", "mainframe", "viewer_copylink"));

        setEnabled(false);

        // listen for URL changes
        ((MessageViewOwner) controller).getMessageController().getUrlObservable()
                                                                    .addObserver(this);
    }

    /* (non-Javadoc)
 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
 */
    public void actionPerformed(ActionEvent evt) {
        // copy selected URL to clipboard as string
        ((MessageViewOwner) frameMediator).getMessageController().getView()
                                                                       .getToolkit()
                                                                       .getSystemClipboard()
                                                                       .setContents(new StringSelection(
                url.getRealURL().toString()), null);
    }

    /* (non-Javadoc)
 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
 */
    public void update(Observable arg0, Object arg1) {
        URLObservable o = (URLObservable) arg0;

        url = o.getUrl();

        if (url == null) {
            setEnabled(false);
        } else {
            setEnabled(true);
        }
    }
}
