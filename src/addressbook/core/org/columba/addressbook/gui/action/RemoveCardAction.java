/*
 * Created on 26.03.2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.addressbook.gui.action;

import java.awt.event.ActionEvent;

import org.columba.addressbook.folder.AddressbookFolder;
import org.columba.addressbook.gui.frame.AddressbookFrameController;
import org.columba.addressbook.util.AddressbookResourceLoader;
import org.columba.core.action.FrameAction;
import org.columba.core.gui.frame.AbstractFrameController;
import org.columba.core.gui.util.ImageLoader;

/**
 * @author frd
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class RemoveCardAction extends FrameAction {

	/**
	 * @param frameController
	 * @param name
	 * @param longDescription
	 * @param actionCommand
	 * @param small_icon
	 * @param big_icon
	 * @param mnemonic
	 * @param keyStroke
	 */
	public RemoveCardAction(AbstractFrameController frameController) {
		super(
			frameController,
			AddressbookResourceLoader.getString(
				"menu",
				"mainframe",
				"menu_file_remove"),
			AddressbookResourceLoader.getString(
				"menu",
				"mainframe",
				"menu_file_remove"),
			"REMOVE",
			ImageLoader.getSmallImageIcon("stock_delete-16.png"),
			ImageLoader.getImageIcon("stock_delete.png"),
			'0',
			null);

	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {
		AddressbookFrameController addressbookFrameController =
			(AddressbookFrameController) frameController;

		Object[] uids =
			addressbookFrameController.getTable().getView().getSelectedUids();
		AddressbookFolder folder =
			(AddressbookFolder) addressbookFrameController
				.getTree()
				.getView()
				.getSelectedFolder();

		for (int i = 0; i < uids.length; i++) {
			folder.remove(uids[i]);
		}
		addressbookFrameController.getTable().getView().setFolder(folder);
	}

}
