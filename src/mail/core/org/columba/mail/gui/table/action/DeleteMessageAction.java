/*
 * Created on 11.03.2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.gui.table.action;

import java.awt.event.ActionEvent;

import org.columba.core.action.FrameAction;
import org.columba.core.gui.frame.AbstractFrameController;
import org.columba.core.gui.selection.SelectionChangedEvent;
import org.columba.core.gui.selection.SelectionListener;
import org.columba.core.gui.util.ImageLoader;
import org.columba.core.main.MainInterface;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.Folder;
import org.columba.mail.folder.command.ExpungeFolderCommand;
import org.columba.mail.folder.command.MarkMessageCommand;
import org.columba.mail.folder.command.MoveMessageCommand;
import org.columba.mail.gui.frame.MailFrameController;
import org.columba.mail.gui.table.selection.TableSelectionChangedEvent;
import org.columba.mail.util.MailResourceLoader;

/**
 * @author frd
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class DeleteMessageAction
	extends FrameAction
	implements SelectionListener {

	/**
	 * @param frameController
	 * @param name
	 * @param longDescription
	 * @param tooltip
	 * @param actionCommand
	 * @param small_icon
	 * @param big_icon
	 * @param mnemonic
	 * @param keyStroke
	 * @param showToolbarText
	 */
	public DeleteMessageAction(AbstractFrameController frameController) {
		super(
			frameController,
			MailResourceLoader.getString(
				"menu",
				"mainframe",
				"menu_message_delete"),
			MailResourceLoader.getString(
				"menu",
				"mainframe",
				"menu_message_delete_toolbar"),
			MailResourceLoader.getString(
				"menu",
				"mainframe",
				"menu_message_delete_tooltip"),
			"DELETE_MESSAGE",
			ImageLoader.getSmallImageIcon("stock_delete-16.png"),
			ImageLoader.getImageIcon("stock_delete.png"),
			'0',
			null,
			false);
		setEnabled(false);
		((MailFrameController) frameController).registerTableSelectionListener(
			this);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {
		FolderCommandReference[] r =
			((MailFrameController) getFrameController()).getTableSelection();
		r[0].setMarkVariant(MarkMessageCommand.MARK_AS_EXPUNGED);

		Folder folder = (Folder) r[0].getFolder();
		String uid = folder.getFolderItem().get("uid");
		Folder trash = (Folder) MainInterface.treeModel.getTrashFolder();

		// trash folder has uid==105
		if (uid.equals("105")) {
			// trash folder is selected
			//  -> delete message

			MainInterface.processor.addOp(new MarkMessageCommand(r));

			MainInterface.processor.addOp(new ExpungeFolderCommand(r));
		} else {
			// -> move messages to trash
			Folder destFolder = trash;

			FolderCommandReference[] result = new FolderCommandReference[2];
			FolderCommandReference[] r1 =
				((MailFrameController) getFrameController())
					.getTableSelection();
			FolderCommandReference r2 = new FolderCommandReference(destFolder);

			result[0] = r1[0];
			result[1] = r2;
			
			MoveMessageCommand c = new MoveMessageCommand(result);

			MainInterface.processor.addOp(c);
		}

	}
	/* (non-Javadoc)
			 * @see org.columba.core.gui.util.SelectionListener#selectionChanged(org.columba.core.gui.util.SelectionChangedEvent)
			 */
	public void selectionChanged(SelectionChangedEvent e) {

		if (((TableSelectionChangedEvent) e).getUids().length > 0)
			setEnabled(true);
		else
			setEnabled(false);

	}
}
