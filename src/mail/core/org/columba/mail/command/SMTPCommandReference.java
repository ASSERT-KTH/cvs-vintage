package org.columba.mail.command;

import org.columba.mail.folder.FolderTreeNode;
import org.columba.mail.message.Message;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class SMTPCommandReference extends FolderCommandReference {

	/**
	 * Constructor for SMTPCommandReference.
	 * @param folder
	 */
	public SMTPCommandReference(FolderTreeNode folder) {
		super(folder);
	}

	/**
	 * Constructor for SMTPCommandReference.
	 * @param folder
	 * @param message
	 */
	public SMTPCommandReference(FolderTreeNode folder, Message message) {
		super(folder, message);
	}

	/**
	 * Constructor for SMTPCommandReference.
	 * @param folder
	 * @param uids
	 */
	public SMTPCommandReference(FolderTreeNode folder, Object[] uids) {
		super(folder, uids);
	}

	/**
	 * Constructor for SMTPCommandReference.
	 * @param folder
	 * @param uids
	 * @param address
	 */
	public SMTPCommandReference(
		FolderTreeNode folder,
		Object[] uids,
		Integer[] address) {
		super(folder, uids, address);
	}

}
