/*
 * Created on Jun 12, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.gui.frame;

import org.columba.mail.folder.Folder;
import org.columba.mail.gui.table.TableController;
import org.columba.mail.message.HeaderList;

/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public interface TableOwnerInterface {
	
	public abstract void showHeaderList( Folder folder, HeaderList headerList) throws Exception;
	public abstract TableController getTableController();

}
