//The contents of this file are subject to the Mozilla Public License Version 1.1
//(the "License"); you may not use this file except in compliance with the 
//License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License 
//for the specific language governing rights and
//limitations under the License.
//
//The Original Code is "The Columba Project"
//
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003. 
//
//All Rights Reserved.

package org.columba.addressbook.main;

import org.columba.addressbook.folder.*;

import java.util.*;

/**
 * @version 	1.0
 * @author
 */
public class AddressbookExitWorker
{
	private AddressbookInterface addressbookInterface;
	
	public AddressbookExitWorker( AddressbookInterface i )
	{
		this.addressbookInterface = i;
	}
	
	public void saveAllAddressbooks()
	{
		//mainInterface.addressbookInterface.
		Folder rootFolder =
			(Folder) addressbookInterface.tree.getRootFolder();

		//timer.start();
		saveAddressbookFolder(rootFolder);
	}
	
	public void saveAddressbookFolder(Folder parentFolder)
	{

		int count = parentFolder.getChildCount();
		Folder child;
		Folder folder;

		for (Enumeration e = parentFolder.children(); e.hasMoreElements();)
		{
			child = (Folder) e.nextElement();
			if ( child != null )
			{
				if ( child instanceof AddressbookFolder )
				{
					try
					{
						child.save( null );
					}
					catch ( Exception ex )
					{
						ex.printStackTrace();
					}
				}
			}
			
			saveAddressbookFolder( child );
		}
	}

}
