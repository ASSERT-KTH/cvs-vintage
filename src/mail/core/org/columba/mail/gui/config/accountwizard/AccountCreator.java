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

package org.columba.mail.gui.config.accountwizard;

import net.javaprog.ui.wizard.DataModel;
import net.javaprog.ui.wizard.WizardModelListener;
import net.javaprog.ui.wizard.WizardModelEvent;

import org.columba.core.main.MainInterface;

import org.columba.mail.config.*;
import org.columba.mail.folder.imap.IMAPRootFolder;

class AccountCreator implements WizardModelListener {
    protected DataModel data;
    
    public AccountCreator(DataModel data) {
        this.data = data;
    }
    
    public void wizardFinished(WizardModelEvent e) {
        String type = (String)data.getData("IncomingServer.type");
        AccountItem account = MailConfig.getAccountList().addEmptyAccount(type.toLowerCase());
        if (account == null) {
            //this should not happen, the templates seem to be missing
            throw new RuntimeException("Account templates missing!");
        }
        account.setName((String)data.getData("Identity.accountName"));
        IdentityItem identity = account.getIdentityItem();
        identity.set("name", (String)data.getData("Identity.name"));
        identity.set("address", (String)data.getData("Identity.address"));
        if (type.equals("POP3")) {
            PopItem pop = account.getPopItem();
            pop.set("host", (String)data.getData("IncomingServer.host"));
            pop.set("user", (String)data.getData("IncomingServer.login"));
	    MainInterface.popServerCollection.add(account);
        } else {
            ImapItem imap = account.getImapItem();
            imap.set("host", (String)data.getData("IncomingServer.host"));
            imap.set("user", (String)data.getData("IncomingServer.login"));
            IMAPRootFolder parentFolder = new IMAPRootFolder(account);
            MainInterface.treeModel.nodeStructureChanged(parentFolder.getParent());
            try {
                    parentFolder.addFolder("INBOX");
            } catch (Exception ex) {
                    ex.printStackTrace();
            }
        }
        account.getSmtpItem().set("host", (String)data.getData("OutgoingServer.host"));
    }
    
    public void stepShown(WizardModelEvent e) {}
    public void wizardCanceled(WizardModelEvent e) {}
}
