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
//$Log: SaveAllAddressbooksPlugin.java,v $
//Revision 1.6  2003/03/29 10:53:14  fdietz
//[bug]fixed loading/saveing of views, size is saved/loaded correctly now
//
//Revision 1.5  2003/03/28 13:08:33  fdietz
//[intern]more mail/addressbook splitting, code cleanups
//
//Revision 1.4  2003/03/09 13:08:34  fdietz
//[intern]import cleanups
//
//Revision 1.3  2003/02/04 17:13:10  fdietz
//[bug]fixed saving of header-cache, changed license header
//
package org.columba.addressbook.shutdown;

import org.columba.addressbook.folder.AddressbookFolder;
import org.columba.addressbook.gui.tree.AddressbookTreeNode;
import org.columba.core.main.MainInterface;
import org.columba.core.shutdown.ShutdownPluginInterface;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class SaveAllAddressbooksPlugin implements ShutdownPluginInterface {

	/**
	 * Constructor for SaveAllFoldersPlugin.
	 */
	public SaveAllAddressbooksPlugin() {
		super();
	}

	/**
	 * @see org.columba.core.shutdown.ShutdownPluginInterface#run()
	 */
	public void shutdown() {	
		saveFolders(
			(AddressbookTreeNode) MainInterface				
				.addressbookTreeModel
				.getRoot());
		
	}

	public void saveFolders(AddressbookTreeNode folder) {
		for (int i = 0; i < folder.getChildCount(); i++) {
			AddressbookTreeNode child =
				(AddressbookTreeNode) folder.getChildAt(i);

			
			if (child instanceof AddressbookFolder) {
				try {
					
					((AddressbookFolder) child).save(null);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			
			saveFolders(child);

		}
	}

}
