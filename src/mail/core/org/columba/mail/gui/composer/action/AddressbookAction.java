/*
 * Created on 25.03.2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.gui.composer.action;

import java.awt.event.ActionEvent;

import org.columba.addressbook.gui.SelectAddressDialog;
import org.columba.core.action.FrameAction;
import org.columba.core.gui.util.ImageLoader;
import org.columba.core.main.MainInterface;
import org.columba.mail.gui.composer.ComposerController;
import org.columba.mail.util.MailResourceLoader;

/**
 * @author frd
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class AddressbookAction extends FrameAction {

	/**
	 * @param composerController
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
	public AddressbookAction(ComposerController composerController) {
		super(
			composerController,
			MailResourceLoader.getString(
				"menu",
				"composer",
				"menu_message_addressbook"),
			MailResourceLoader.getString(
				"menu",
				"composer",
				"menu_message_addressbook"),
			MailResourceLoader.getString(
				"menu",
				"composer",
				"menu_message_addressbook"),
			"ADDRESSBOOK",
			ImageLoader.getSmallImageIcon("contact_small.png"),
			ImageLoader.getImageIcon("contact.png"),
			MailResourceLoader.getMnemonic(
				"menu",
				"composer",
				"menu_message_addressbook"),
			null,
			false);

	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {
		ComposerController composerController = ((ComposerController)getFrameController());
		
		composerController.getHeaderController().cleanupHeaderItemList();

		SelectAddressDialog dialog =
			new SelectAddressDialog(
				MainInterface.addressbookInterface,
				composerController.getView(),
		composerController.getHeaderController().getHeaderItemLists());

		org.columba.addressbook.folder.Folder folder =
			(org.columba.addressbook.folder.Folder) MainInterface
				.addressbookInterface
				.treeModel
				.getFolder(101);
		dialog.setHeaderList(folder.getHeaderItemList());

		dialog.setVisible(true);

		composerController.getHeaderController().setHeaderItemLists(
			dialog.getHeaderItemLists());
	}

}
