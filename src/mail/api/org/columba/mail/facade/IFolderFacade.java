// The contents of this file are subject to the Mozilla Public License Version
// 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.mail.facade;

import java.util.Enumeration;

import org.columba.mail.folder.IMailFolder;
import org.columba.mail.folder.IMailbox;

/**
 * @author fdietz
 *
 */
public interface IFolderFacade {
	/**
	 * Get folder with uid.
	 * 
	 * @param uid
	 *            folder uid
	 * @return folder
	 */
	IMailFolder getFolder(int uid);

	/**
	 * Get folder with name.
	 * 
	 * @param name
	 *            folder name
	 * @return folder
	 */
	IMailFolder getFolder(String name);

	/**
	 * Get breadth-first enumeration of all mailbox folders.
	 * 
	 * @return	breadth-first enumeration of folders
	 */
	Enumeration getBreadthFirstEnumeration();

	/**
	 * Get depth-first enumeration of all mailbox folders.
	 * 
	 * @return	depth-first enumeration of folders
	 */
	Enumeration getDepthFirstEnumeration();

	/**
	 * Get local Inbox folder.
	 * 
	 * @return		inbox folder
	 */
	IMailbox getLocalInboxFolder();

	/**
	 * Get local Trash folder.
	 * 
	 * @return		trash folder
	 */
	IMailbox getLocalTrashFolder();

	/**
	 * Get local Drafts folder.
	 * 
	 * @return	drafts folder
	 */
	IMailbox getLocalDraftsFolder();

	/**
	 * Get local Templates folder.
	 * 
	 * @return		templates folder
	 */
	IMailbox getLocalTemplatesFolder();

	/**
	 * Get local outbox folder.
	 * 
	 * @return		outbox folder
	 */
	IMailbox getLocalOutboxFolder();

	/**
	 * Get local sent folder.
	 * 
	 * @return		sent folder
	 */
	IMailbox getLocalSentFolder();
}