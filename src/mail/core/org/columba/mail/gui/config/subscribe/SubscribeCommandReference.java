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
package org.columba.mail.gui.config.subscribe;

import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.AbstractFolder;


public class SubscribeCommandReference extends FolderCommandReference {
    private SubscribeDialog dialog;
    private String mailbox;

    /**
 * @param folder
 */
    public SubscribeCommandReference(AbstractFolder folder,
        SubscribeDialog dialog) {
        super(folder);

        this.dialog = dialog;
    }

    /**
 * @param folder
 */
    public SubscribeCommandReference(AbstractFolder folder,
        SubscribeDialog dialog, String mailbox) {
        super(folder);

        this.dialog = dialog;
        this.mailbox = mailbox;
    }

    /**
 * @return Returns the dialog.
 */
    public SubscribeDialog getDialog() {
        return dialog;
    }

    /**
 * @param dialog The dialog to set.
 */
    public void setDialog(SubscribeDialog dialog) {
        this.dialog = dialog;
    }

    /**
 * @return Returns the mailbox.
 */
    public String getMailbox() {
        return mailbox;
    }

    /**
 * @param mailbox The mailbox to set.
 */
    public void setMailbox(String mailbox) {
        this.mailbox = mailbox;
    }
}
