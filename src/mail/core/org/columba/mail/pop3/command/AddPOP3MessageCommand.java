/*
 * Created on 18.07.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.pop3.command;

import org.columba.core.command.CompoundCommand;
import org.columba.core.command.DefaultCommandReference;
import org.columba.core.command.Worker;
import org.columba.core.gui.frame.FrameMediator;
import org.columba.core.main.MainInterface;
import org.columba.mail.command.FolderCommand;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.filter.Filter;
import org.columba.mail.filter.FilterList;
import org.columba.mail.folder.Folder;
import org.columba.mail.gui.frame.TableUpdater;
import org.columba.mail.gui.table.model.TableModelChangedEvent;
import org.columba.mail.main.MailInterface;
import org.columba.mail.message.ColumbaMessage;
import org.columba.ristretto.message.Flags;
import org.columba.ristretto.message.io.SourceInputStream;


/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class AddPOP3MessageCommand extends FolderCommand {
    Folder inboxFolder;

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
    public void execute(Worker worker) throws Exception {
        FolderCommandReference[] r = (FolderCommandReference[]) getReferences();

        inboxFolder = (Folder) r[0].getFolder();

        ColumbaMessage message = (ColumbaMessage) r[0].getMessage();

        // add message to folder
        Object uid = inboxFolder.addMessage(new SourceInputStream(message.getSource()), message.getHeader().getAttributes());
        inboxFolder.getFlags(uid).set(Flags.RECENT);

		inboxFolder.getMessageFolderInfo().incRecent();

        // apply filter on message
        FilterList list = inboxFolder.getFilterList();

        for (int j = 0; j < list.count(); j++) {
            Filter filter = list.get(j);

            Object[] result = inboxFolder.searchMessages(filter, new Object[] { uid });

            if (result.length != 0) {
                CompoundCommand command = filter.getCommand(inboxFolder, result);

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
        TableModelChangedEvent ev = new TableModelChangedEvent(TableModelChangedEvent.UPDATE,
                inboxFolder);

        TableUpdater.tableChanged(ev);

        // update tree viewer
        MailInterface.treeModel.nodeChanged(inboxFolder);
    }
}
