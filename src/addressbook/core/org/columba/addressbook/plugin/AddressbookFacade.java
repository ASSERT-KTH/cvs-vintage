/*
 * Created on 16.04.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.addressbook.plugin;

import org.columba.addressbook.config.AddressbookConfig;
import org.columba.addressbook.folder.Folder;
import org.columba.core.main.MainInterface;
import org.columba.core.xml.XmlElement;

/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class AddressbookFacade {

	/**
	 * 
	 */
	public AddressbookFacade() {
		super();
		
	}
	
	public static XmlElement getConfigElement(String configName) {
			XmlElement root = AddressbookConfig.get(configName);

			return root;
		}
		
	public static Folder getAddressbook( int uid )
	{
		return (Folder) MainInterface.addressbookTreeModel.getFolder(uid);
	}
	
	public static Folder getCollectedAddresses()
		{
			return (Folder) MainInterface.addressbookTreeModel.getFolder(102);
		}
	

}
