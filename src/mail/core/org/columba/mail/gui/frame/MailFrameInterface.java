/*
 * Created on Jun 10, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.gui.frame;

import org.columba.mail.command.FolderCommandReference;

/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public interface MailFrameInterface {

	abstract public FolderCommandReference[] getTableSelection();
	abstract public FolderCommandReference[] getTreeSelection();
	abstract public void setTreeSelection( FolderCommandReference[] r);
	abstract public void setTableSelection( FolderCommandReference[] r);
	
}
