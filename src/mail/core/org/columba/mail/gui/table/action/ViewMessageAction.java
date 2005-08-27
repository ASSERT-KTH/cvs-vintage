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
package org.columba.mail.gui.table.action;

import java.awt.event.ActionEvent;
import java.util.logging.Logger;

import org.columba.api.gui.frame.IFrameMediator;
import org.columba.core.command.CommandProcessor;
import org.columba.core.gui.action.AbstractColumbaAction;
import org.columba.mail.command.MailFolderCommandReference;
import org.columba.mail.gui.frame.MessageViewOwner;
import org.columba.mail.gui.message.IMessageController;
import org.columba.mail.gui.message.command.ViewMessageCommand;


public class ViewMessageAction extends AbstractColumbaAction {

    /** JDK 1.4+ logging framework logger, used for logging. */
    private static final Logger LOG = Logger.getLogger("org.columba.mail.gui.table.action");

    protected static Object oldUid;

    /**
     * @param controller
     */
    public ViewMessageAction(IFrameMediator controller) {
        super(controller, "ViewMessageAction");
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent evt) {
        MailFolderCommandReference references = (MailFolderCommandReference) getFrameMediator()
                                                                             .getSelectionManager()
                                                                             .getSelection("mail.table");
        Object[] uids = references.getUids();

        if (uids.length == 1) {
            // show selected message
            CommandProcessor.getInstance().addOp(new ViewMessageCommand(
                    getFrameMediator(), references));
        }
    }
}
