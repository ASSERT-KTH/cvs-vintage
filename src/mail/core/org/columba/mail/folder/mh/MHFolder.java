package org.columba.mail.folder.mh;

import org.columba.core.xml.XmlElement;
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

	public MHFolder( FolderItem item )
	{
		super( item );
	}

	public DataStorageInterface getDataStorageInstance()
	{
		if (dataStorage == null)
			dataStorage = new MHDataStorage(this);

		return dataStorage;
	}
	
	/**
	 * @see org.columba.mail.folder.FolderTreeNode#getDefaultProperties()
	 */
	public static XmlElement getDefaultProperties() {
		XmlElement props = new XmlElement("property");
		props.addAttribute("accessrights","user");
		props.addAttribute("subfolder","true");
				
		return props;
	}

}
