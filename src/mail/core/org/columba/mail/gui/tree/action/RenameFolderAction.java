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
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.config.FolderItem;
import org.columba.mail.folder.Folder;
import org.columba.mail.folder.FolderTreeNode;
import org.columba.mail.gui.config.folder.FolderOptionsDialog;
import org.columba.mail.gui.frame.AbstractMailFrameController;
import org.columba.mail.gui.tree.selection.TreeSelectionChangedEvent;
import org.columba.mail.util.MailResourceLoader;

/**
 * @author frd
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class RenameFolderAction
	extends FrameAction
	implements SelectionListener {

	public RenameFolderAction(AbstractFrameController frameController) {
		super(
				frameController,
				MailResourceLoader.getString(
					"menu", "mainframe", "menu_folder_renamefolder"));
		
		// tooltip text
		setTooltipText(
				MailResourceLoader.getString(
					"menu", "mainframe", "menu_folder_renamefolder"));
		
		// action command
		setActionCommand("RENAME_FOLDER");

		// shortcut key
		setAcceleratorKey(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
		
		setEnabled(false);
		(
			(
				AbstractMailFrameController) frameController)
					.registerTreeSelectionListener(
			this);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {
		FolderCommandReference[] r =
			(FolderCommandReference[])
				((AbstractMailFrameController) frameController)
				.getTreeSelection();

		new FolderOptionsDialog((Folder) r[0].getFolder(), true);

		/*
		EditFolderDialog dialog = new EditFolderDialog("New Folder");
		dialog.showDialog();
		
		String name;
		
		if (dialog.success()) {
			// ok pressed
			name = dialog.getName();
		} else {
			// cancel pressed
			return;
		}
		
		FolderCommandReference[] r =
			(FolderCommandReference[]) frameController
				.getSelectionManager()
				.getSelection(
				"mail.tree");
		r[0].setFolderName(name);
		
		MainInterface.processor.addOp(new RenameFolderCommand(r));
		*/
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
