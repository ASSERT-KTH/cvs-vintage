/*
 * Created on 06.04.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.gui.messageframe;

import org.columba.core.config.ViewItem;
import org.columba.core.gui.frame.AbstractFrameView;
import org.columba.core.logging.ColumbaLogger;
import org.columba.core.main.MainInterface;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.Folder;
import org.columba.mail.gui.frame.MailFrameController;
import org.columba.mail.gui.message.command.ViewMessageCommand;

/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class MessageFrameController extends MailFrameController {

	/**
	 * @param viewItem
	 */
	public MessageFrameController(ViewItem viewItem) {
		super("MessageFrame", viewItem);

		list.add(this);
	}

	public void selectInbox() {
		Folder inboxFolder = (Folder) MainInterface.treeModel.getFolder(101);
		try {

			Object[] uids = inboxFolder.getUids(null);
			if (uids.length > 0) {
				Object uid = uids[0];

				Object[] newUids = new Object[1];
				newUids[0] = uid;

				FolderCommandReference[] r = new FolderCommandReference[1];
				r[0] = new FolderCommandReference(inboxFolder, newUids);

				getSelectionManager().getHandler("mail.table").setSelection(r);

				MainInterface.processor.addOp(new ViewMessageCommand(this, r));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	/* (non-Javadoc)
	 * @see org.columba.core.gui.frame.AbstractFrameController#createView()
	 */
	public AbstractFrameView createView() {
		MessageFrameView view = new MessageFrameView(this);

		view.setFolderInfoPanel(folderInfoPanel);

		view.init(messageController.getView(), statusBar);

		view.pack();

		view.setVisible(true);

		return view;
	}

	

}
