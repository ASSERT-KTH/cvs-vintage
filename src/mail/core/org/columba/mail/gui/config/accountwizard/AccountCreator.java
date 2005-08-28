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
import net.javaprog.ui.wizard.WizardModelEvent;
import net.javaprog.ui.wizard.WizardModelListener;

import org.columba.mail.config.AccountItem;
import org.columba.mail.config.ImapItem;
import org.columba.mail.config.MailConfig;
import org.columba.mail.config.PopItem;
import org.columba.mail.folder.IMailFolder;
import org.columba.mail.folder.imap.IMAPFolder;
import org.columba.mail.folder.imap.IMAPRootFolder;
import org.columba.mail.gui.tree.FolderTreeModel;
import org.columba.mail.mailchecking.MailCheckingManager;
import org.columba.mail.pop3.POP3ServerCollection;
import org.columba.ristretto.message.Address;

class AccountCreator implements WizardModelListener {
    protected DataModel data;

    public AccountCreator(DataModel data) {
        this.data = data;
    }

    public void wizardFinished(WizardModelEvent e) {
        String type = (String) data.getData("IncomingServer.type");
        AccountItem account = MailConfig.getInstance().getAccountList()
                                                  .addEmptyAccount(type.toLowerCase());

        if (account == null) {
            //this should not happen, the templates seem to be missing
            throw new RuntimeException("Account templates missing!");
        }

        account.setName((String) data.getData("Identity.accountName"));
        account.getIdentity().setAddress((Address)data.getData("Identity.address"));

        if (type.equals("POP3")) {
            PopItem pop = account.getPopItem();
            pop.setString("host", (String) data.getData("IncomingServer.host"));
            pop.setString("user", (String) data.getData("IncomingServer.login"));
            POP3ServerCollection.getInstance().add(account);
        } else {
            ImapItem imap = account.getImapItem();
            imap.setString("host", (String) data.getData("IncomingServer.host"));
            imap.setString("user", (String) data.getData("IncomingServer.login"));

            // TODO (@author fdietz): All this code for creating a new IMAPRootFolder should
            //       be moved to a FolderFactory
            //       -> this way "path" would be handled in the factory, too
            // parent directory for mail folders
            // for example: ".columba/mail/"
            String path = MailConfig.getInstance().getConfigDirectory().getPath();

            IMAPRootFolder parentFolder = new IMAPRootFolder(account, path);
            ((IMailFolder) FolderTreeModel.getInstance().getRoot()).add(parentFolder);
            ((IMailFolder) FolderTreeModel.getInstance().getRoot())
                    .getConfiguration().getRoot().addElement(
                            parentFolder.getConfiguration().getRoot());

            FolderTreeModel.getInstance().nodeStructureChanged(parentFolder.getParent());

            try {
            	IMailFolder inbox = new IMAPFolder("INBOX", "IMAPFolder",
                        path);
                parentFolder.add(inbox);
                parentFolder.getConfiguration().getRoot().addElement(
                        inbox.getConfiguration().getRoot());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        // add account to mail-checking manager
        MailCheckingManager.getInstance().add(account);

        // notify all observers
        MailCheckingManager.getInstance().update();

        account.getSmtpItem().setString("host",
            (String) data.getData("OutgoingServer.host"));

        // generally we can just use the same login for both servers
        account.getSmtpItem().setString("user",
            (String) data.getData("IncomingServer.login"));
    }

    public void stepShown(WizardModelEvent e) {
    }

    public void wizardCanceled(WizardModelEvent e) {
    }

    public void wizardModelChanged(WizardModelEvent e) {
    }
}
