package org.columba.mail.command;

import org.columba.mail.folder.FolderTreeNode;
import org.columba.mail.gui.composer.ComposerController;
import org.columba.mail.message.Message;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ComposerCommandReference extends FolderCommandReference {
	
	protected ComposerController composerController;


	/**
	 * Constructor for ComposerCommandReference.
	 * @param folder
	 */
	public ComposerCommandReference(ComposerController composerController, FolderTreeNode folder) {
		super(folder);
		this.composerController = composerController;
	}

	/**
	 * Constructor for ComposerCommandReference.
	 * @param folder
	 * @param message
	 */
	public ComposerCommandReference(FolderTreeNode folder, Message message) {
		super(folder, message);
	}

	/**
	 * Constructor for ComposerCommandReference.
	 * @param folder
	 * @param uids
	 */
	public ComposerCommandReference(FolderTreeNode folder, Object[] uids) {
		super(folder, uids);
	}

	/**
	 * Constructor for ComposerCommandReference.
	 * @param folder
	 * @param uids
	 * @param address
	 */
	public ComposerCommandReference(
		FolderTreeNode folder,
		Object[] uids,
		Integer[] address) {
		super(folder, uids, address);
	}

	/**
	 * Returns the composerController.
	 * @return ComposerController
	 */
	public ComposerController getComposerController() {
		return composerController;
	}

}
