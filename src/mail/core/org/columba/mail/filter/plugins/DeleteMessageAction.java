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
package org.columba.mail.filter.plugins;

import org.columba.core.command.Command;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.filter.FilterAction;
import org.columba.mail.folder.Folder;
import org.columba.mail.folder.command.ExpungeFolderCommand;
import org.columba.mail.folder.command.MarkMessageCommand;
import org.columba.core.main.MainInterface;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class DeleteMessageAction extends AbstractFilterAction {

	

	/**
	 * @see org.columba.modules.mail.filter.action.AbstractFilterAction#execute()
	 */
	public Command getCommand( FilterAction filterAction, Folder srcFolder, Object[] uids) throws Exception {
		FolderCommandReference[] r = new FolderCommandReference[1];
		r[0] = new FolderCommandReference(srcFolder, uids);
		r[0].setMarkVariant(MarkMessageCommand.MARK_AS_EXPUNGED);

		MarkMessageCommand c = new MarkMessageCommand( r);

		MainInterface.processor.addOp(c);

		r = new FolderCommandReference[1];
		r[0] = new FolderCommandReference(srcFolder);

		return new ExpungeFolderCommand(r);
	}

}
