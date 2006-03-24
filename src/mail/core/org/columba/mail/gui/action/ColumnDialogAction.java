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

package org.columba.mail.gui.action;

import java.awt.event.ActionEvent;

import org.columba.api.gui.frame.IFrameMediator;
import org.columba.core.gui.action.AbstractColumbaAction;
import org.columba.core.xml.XmlElement;
import org.columba.mail.command.IMailFolderCommandReference;
import org.columba.mail.folder.IMailFolder;
import org.columba.mail.folder.IMailbox;
import org.columba.mail.gui.config.columns.ColumnConfigDialog;
import org.columba.mail.gui.frame.MailFrameMediator;
import org.columba.mail.util.MailResourceLoader;

/**
 * Open column config dialog.
 *
 * @author fdietz
 */

public class ColumnDialogAction extends AbstractColumbaAction {
    public ColumnDialogAction(IFrameMediator frameMediator) {
        super(frameMediator, MailResourceLoader.getString(
            "dialog", "columns", "title") + "...");
    }

    public void actionPerformed(ActionEvent arg0) {
        IMailFolderCommandReference r = ((MailFrameMediator) getFrameMediator()).getTreeSelection();

        if (r != null) {
            IMailFolder folder = (IMailFolder) r.getSourceFolder();

            if (folder == null) {
                return;
            }

            // check if we should use the folder-based configuration
            // or the global table configuration
            XmlElement columns = ((MailFrameMediator) getFrameMediator()).getFolderOptionsController()
                                  .getConfigNode((IMailbox) folder,
                    "ColumnOptions");

            new ColumnConfigDialog((MailFrameMediator) getFrameMediator(),
                columns);
        }
    }
}
