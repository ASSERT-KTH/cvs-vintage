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
package org.columba.mail.pop3.command;

import org.columba.core.command.Command;
import org.columba.core.command.CommandCancelledException;
import org.columba.core.command.DefaultCommandReference;
import org.columba.core.command.StatusObservableImpl;
import org.columba.core.command.Worker;
import org.columba.core.logging.ColumbaLogger;
import org.columba.core.main.MainInterface;

import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.command.POP3CommandReference;
import org.columba.mail.folder.Folder;
import org.columba.mail.message.ColumbaMessage;
import org.columba.mail.pop3.POP3Server;
import org.columba.mail.util.MailResourceLoader;

import java.io.IOException;

import java.text.MessageFormat;

import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;


/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class FetchNewMessagesCommand extends Command {
    POP3Server server;
    int totalMessageCount;
    int newMessageCount;

    /**
     * Constructor for FetchNewMessages.
     * @param frameMediator
     * @param references
     */
    public FetchNewMessagesCommand(DefaultCommandReference[] references) {
        super(references);

        POP3CommandReference[] r = (POP3CommandReference[]) getReferences(FIRST_EXECUTION);

        server = r[0].getServer();
    }

    /**
     * @see org.columba.core.command.Command#execute(Worker)
     */
    public void execute(Worker worker) throws Exception {
        POP3CommandReference[] r = (POP3CommandReference[]) getReferences(FIRST_EXECUTION);

        server = r[0].getServer();

        // register interest on status bar information
        ((StatusObservableImpl) server.getObservable()).setWorker(worker);

        log(MailResourceLoader.getString("statusbar", "message",
                "authenticating"));

        try {
            // login and get # of messages on server
            totalMessageCount = server.getMessageCount();

            // synchronize local UID list with server UID list
            List newMessagesUidList = synchronize();

            // only download new messages
            downloadNewMessages(newMessagesUidList, worker);

            // logout cleanly
            logout();

            // display downloaded message count in statusbar
            if (newMessageCount == 0) {
                log(MailResourceLoader.getString("statusbar", "message",
                        "no_new_messages"));
            } else {
                log(MessageFormat.format(MailResourceLoader.getString(
                            "statusbar", "message", "fetched_count"),
                        new Object[] { new Integer(newMessageCount) }));
            }
        } catch (CommandCancelledException e) {
            server.forceLogout();

            // clear statusbar message
            server.getObservable().clearMessage();
        } catch (IOException e) {
            String name = e.getClass().getName();
            JOptionPane.showMessageDialog(null, e.getLocalizedMessage(),
                name.substring(name.lastIndexOf(".")), JOptionPane.ERROR_MESSAGE);

            // clear statusbar message
            server.getObservable().clearMessage();
        } finally {
            // always enable the menuitem again 
            r[0].getPOP3ServerController().enableActions(true);
        }
    }

    protected void log(String message) {
        server.getObservable().setMessage(server.getFolderName() + ": " +
            message);
    }

    public void downloadMessage(Object serverUID, Worker worker)
        throws Exception {
        // server message numbers start with 1
        // whereas List numbers start with 0
        //  -> always increase fetch number
        ColumbaMessage message = server.getMessage(serverUID);

        if (message == null) {
            ColumbaLogger.log.severe("Message with UID=" + serverUID +
                " isn't on the server.");

            return;
        }

        message.getHeader().getFlags().setSeen(false);

        //get inbox-folder from pop3-server preferences
        Folder inboxFolder = server.getFolder();

        // start command which adds message to folder
        // and calls apply-filter on this specific message
        FolderCommandReference[] r = new FolderCommandReference[1];
        r[0] = new FolderCommandReference(inboxFolder, message);

        MainInterface.processor.addOp(new AddPOP3MessageCommand(r));
    }

    protected int calculateTotalSize(List uidList) throws Exception {
        int totalSize = 0;

        Iterator it = uidList.iterator();

        while (it.hasNext()) {
            totalSize += server.getMessageSize(it.next());
        }

        // return kB
        return totalSize / 1024;
    }

    public void downloadNewMessages(List newMessagesUIDList, Worker worker)
        throws Exception {
        ColumbaLogger.log.fine("need to fetch " + newMessagesUIDList.size() +
            " messages.");

        int totalSize = calculateTotalSize(newMessagesUIDList);

        worker.setProgressBarMaximum(totalSize);
        worker.setProgressBarValue(0);

        newMessageCount = newMessagesUIDList.size();

        for (int i = 0; i < newMessageCount; i++) {
            // which UID should be downloaded next
            Object serverUID = newMessagesUIDList.get(i);

            ColumbaLogger.log.fine("fetch message with UID=" + serverUID);

            log(MessageFormat.format(MailResourceLoader.getString("statusbar",
                        "message", "fetch_messages"),
                    new Object[] {
                        new Integer(i + 1), new Integer(newMessageCount)
                    }));

            int size = server.getMessageSize(serverUID);

            if (server.getAccountItem().getPopItem().getBoolean("enable_limit")) {
                // check if message isn't too big to download
                int maxSize = server.getAccountItem().getPopItem().getInteger("limit");

                // if message-size is bigger skip download of this message
                if (size > maxSize) {
                    ColumbaLogger.log.fine(
                        "skipping download of message, too big");

                    continue;
                }
            }

            // now download the message
            downloadMessage(serverUID, worker);

            if (!server.getAccountItem().getPopItem().getBoolean("leave_messages_on_server")) {
                // delete message from server

                /*
                // remove UID from server list
                boolean remove = newUIDList.remove(serverUID);
                */

                // server message numbers start with 1
                // whereas List numbers start with 0
                //  -> always increase delete number
                // delete message with <index>==index from server
                server.deleteMessage(serverUID);

                ColumbaLogger.log.fine("deleted message with uid=" + serverUID);
            }
        }
    }

    public List synchronize() throws Exception {
        log(MailResourceLoader.getString("statusbar", "message",
                "fetch_uid_list"));

        ColumbaLogger.log.fine(
            "synchronize local UID-list with remote UID-list");

        // synchronize local UID-list with server 		
        List newMessagesUIDList = server.synchronize();

        return newMessagesUIDList;
    }

    public void logout() throws Exception {
        server.logout();

        ColumbaLogger.log.fine("logout");

        log(MailResourceLoader.getString("statusbar", "message", "logout"));

        if (newMessageCount == 0) {
            log(MailResourceLoader.getString("statusbar", "message",
                    "no_new_messages"));
        }
    }
}
