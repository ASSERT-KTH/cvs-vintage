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
package org.columba.mail.shutdown;

import java.util.Enumeration;

import org.columba.core.main.MainInterface;
import org.columba.core.shutdown.*;
import org.columba.mail.folder.FolderTreeNode;
import org.columba.mail.folder.imap.IMAPFolder;
import org.columba.mail.folder.mh.CachedMHFolder;
import org.columba.mail.folder.outbox.OutboxFolder;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class SaveAllFoldersPlugin implements ShutdownPluginInterface {

	public void shutdown() {
		saveAllFolders();
	}

	public void saveAllFolders() {
		FolderTreeNode rootFolder =
			(FolderTreeNode) MainInterface.treeModel.getRoot();

		saveFolder(rootFolder);
	}

	public void saveFolder(FolderTreeNode parentFolder) {

		int count = parentFolder.getChildCount();
		FolderTreeNode child;
		FolderTreeNode folder;

		for (Enumeration e = parentFolder.children(); e.hasMoreElements();) {

			child = (FolderTreeNode) e.nextElement();

			if (child instanceof CachedMHFolder) {
				CachedMHFolder mhFolder = (CachedMHFolder) child;
				try {
					mhFolder.save();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			} else if (child instanceof OutboxFolder) {
				OutboxFolder outboxFolder = (OutboxFolder) child;
				try {
					outboxFolder.save();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			} else if (child instanceof IMAPFolder) {
				IMAPFolder imapFolder = (IMAPFolder) child;

				try {
					imapFolder.save();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			saveFolder(child);
		}
	}

}
