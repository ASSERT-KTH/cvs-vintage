package org.columba.core.gui.util;

import java.util.Vector;

import org.columba.core.command.DefaultCommandReference;
import org.columba.mail.folder.Folder;
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
public abstract class SelectionManager {

	public abstract DefaultCommandReference[] getSelection();

}
