package org.columba.mail.folder.mh;

import org.columba.core.config.AdapterNode;
import org.columba.mail.config.FolderItem;
import org.columba.mail.folder.DataStorageInterface;
import org.columba.mail.folder.LocalFolder;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public abstract class MHFolder extends LocalFolder {

	public MHFolder( AdapterNode node, FolderItem item )
	{
		super( node, item );
	}
	
	public DataStorageInterface getDataStorageInstance()
	{
		if (dataStorage == null)
			dataStorage = new MHDataStorage(this);

		return dataStorage;
	}
	
}
