/*
 * Created on 11.03.2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.gui.tree.action;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import org.columba.core.action.FrameAction;
import org.columba.core.gui.frame.AbstractFrameController;
import org.columba.core.gui.selection.SelectionChangedEvent;
import org.columba.core.gui.selection.SelectionListener;
import org.columba.core.main.MainInterface;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.config.FolderItem;
import org.columba.mail.folder.FolderTreeNode;
import org.columba.mail.folder.command.ExpungeFolderCommand;
import org.columba.mail.gui.frame.AbstractMailFrameController;
import org.columba.mail.gui.tree.selection.TreeSelectionChangedEvent;
import org.columba.mail.util.MailResourceLoader;

/**
 * @author frd
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ExpungeFolderAction
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
	 */
	public ExpungeFolderAction(AbstractFrameController frameController) {
		super(
			frameController,
			MailResourceLoader.getString(
				"menu",
				"mainframe",
				"menu_folder_expungefolder"),
			MailResourceLoader.getString(
				"menu",
				"mainframe",
				"menu_folder_expungefolder"),
			MailResourceLoader.getString(
				"menu",
				"mainframe",
				"menu_folder_expungefolder"),
			"EXPUNGE_FOLDER",
			null,
			null,
			'P',
			KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.ALT_MASK));
		setEnabled(false);
		((AbstractMailFrameController) frameController).registerTreeSelectionListener(
			this);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {
		FolderCommandReference[] r =
			(FolderCommandReference[]) frameController
				.getSelectionManager()
				.getSelection(
				"mail.tree");
		ExpungeFolderCommand c = new ExpungeFolderCommand(r);

		MainInterface.processor.addOp(c);
	}
	/* (non-Javadoc)
					 * @see org.columba.core.gui.util.SelectionListener#selectionChanged(org.columba.core.gui.util.SelectionChangedEvent)
					 */
	public void selectionChanged(SelectionChangedEvent e) {

		if (((TreeSelectionChangedEvent) e).getSelected().length > 0) {
			FolderTreeNode folder = ((TreeSelectionChangedEvent) e).getSelected()[0];

			if (folder != null) {

				FolderItem item = folder.getFolderItem();
				if (item.get("property", "accessrights").equals("user"))
					setEnabled(true);
				else
					setEnabled(false);
			}
		} else
			setEnabled(false);

	}
}
