/*
 * Created on 18.07.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.pop3.command;

import org.columba.core.command.CompoundCommand;
import org.columba.core.command.DefaultCommandReference;
import org.columba.core.command.WorkerStatusController;
import org.columba.core.gui.frame.FrameMediator;
import org.columba.core.main.MainInterface;
import org.columba.mail.command.FolderCommand;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.config.AccountItem;
import org.columba.mail.filter.Filter;
import org.columba.mail.filter.FilterList;
import org.columba.mail.folder.MessageFolder;
import org.columba.mail.folder.AbstractFolder;
import org.columba.mail.folder.RootFolder;
import org.columba.mail.folder.command.MoveMessageCommand;
import org.columba.mail.gui.frame.TableUpdater;
import org.columba.mail.gui.table.model.TableModelChangedEvent;
import org.columba.mail.main.MailInterface;
import org.columba.mail.message.ColumbaMessage;
import org.columba.mail.spam.command.CommandHelper;
import org.columba.mail.spam.command.ScoreMessageCommand;
import org.columba.ristretto.message.Flags;
import org.columba.ristretto.message.io.SourceInputStream;

/**
 * After downloading the message from the POP3 server, its added to the Inbox
 * folder.
 * <p>
 * The spam filter is executed on this message.
 * <p>
 * The Inbox filters are applied to the message.
 * 
 * @author fdietz
 */
public class AddPOP3MessageCommand extends FolderCommand {

    MessageFolder inboxFolder;

    /**
     * @param references
     */
    public AddPOP3MessageCommand(DefaultCommandReference[] references) {
        super(references);
    }

    /**
     * @param frame
     * @param references
     */
    public AddPOP3MessageCommand(FrameMediator frame,
            DefaultCommandReference[] references) {
        super(frame, references);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.columba.core.command.Command#execute(org.columba.core.command.Worker)
     */
    public void execute(WorkerStatusController worker) throws Exception {
        FolderCommandReference[] r = (FolderCommandReference[]) getReferences();

        inboxFolder = (MessageFolder) r[0].getFolder();

        ColumbaMessage message = (ColumbaMessage) r[0].getMessage();

        // add message to folder
        Object uid = inboxFolder.addMessage(new SourceInputStream(message
                .getSource()), message.getHeader().getAttributes());
        inboxFolder.getFlags(uid).set(Flags.RECENT);

        inboxFolder.getMessageFolderInfo().incRecent();

        // apply spam filter
        applySpamFilter(uid, worker);

        // apply filter on message
        applyFilters(uid);
    }

    /**
     * Apply spam filter engine on message.
     * <p>
     * Message is marked as ham or spam.
     * 
     * @param uid
     *            message uid.
     * @throws Exception
     */
    private void applySpamFilter(Object uid, WorkerStatusController worker)
            throws Exception {
        // message belongs to which account?
        AccountItem item = CommandHelper.retrieveAccountItem(inboxFolder, uid);

        // if spam filter is not enabled -> return
        if (item.getSpamItem().isEnabled() == false) return;

        // create reference
        FolderCommandReference[] r = new FolderCommandReference[1];
        r[0] = new FolderCommandReference(inboxFolder, new Object[] { uid});

        // pass command to command scheduler
        new ScoreMessageCommand(r).execute(worker);

        if (item.getSpamItem().isMoveIncomingJunkMessagesEnabled()) {
            if (item.getSpamItem().isIncomingTrashSelected()) {
                // move message to trash
                MessageFolder trash = (MessageFolder) ((RootFolder) inboxFolder
                        .getRootFolder()).getTrashFolder();

                // create reference
                FolderCommandReference[] ref2 = new FolderCommandReference[2];
                ref2[0] = new FolderCommandReference(inboxFolder,
                        new Object[] { uid});
                ref2[1] = new FolderCommandReference(trash);

                MainInterface.processor.addOp(new MoveMessageCommand(ref2));
            } else {
                // move message to user-configured folder (generally "Junk"
                // folder)
                AbstractFolder destFolder = MailInterface.treeModel
                        .getFolder(item.getSpamItem().getMoveCustomFolder());

                // create reference
                FolderCommandReference[] ref2 = new FolderCommandReference[2];
                ref2[0] = new FolderCommandReference(inboxFolder,
                        new Object[] { uid});
                ref2[1] = new FolderCommandReference(destFolder);
                MainInterface.processor.addOp(new MoveMessageCommand(ref2));
            }
        }
    }

    /**
     * Apply filters on new message.
     * 
     * @param uid
     *            message uid
     */
    private void applyFilters(Object uid) throws Exception {
        FilterList list = inboxFolder.getFilterList();

        for (int j = 0; j < list.count(); j++) {
            Filter filter = list.get(j);

            Object[] result = inboxFolder.searchMessages(filter,
                    new Object[] { uid});

            if (result.length != 0) {
                CompoundCommand command = filter
                        .getCommand(inboxFolder, result);

                MainInterface.processor.addOp(command);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.columba.core.command.Command#updateGUI()
     */
    public void updateGUI() throws Exception {
        // update table viewer
        TableModelChangedEvent ev = new TableModelChangedEvent(
                TableModelChangedEvent.UPDATE, inboxFolder);

        TableUpdater.tableChanged(ev);

        // update tree viewer
        MailInterface.treeModel.nodeChanged(inboxFolder);
    }
}
