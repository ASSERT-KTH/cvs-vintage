package org.columba.mail.filter.plugins;

import org.columba.core.command.Command;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.filter.FilterAction;
import org.columba.mail.folder.Folder;
import org.columba.mail.folder.command.MarkMessageCommand;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class MarkMessageAsReadAction extends AbstractFilterAction {

	

	/**
	 * @see org.columba.modules.mail.filter.action.AbstractFilterAction#execute()
	 */
		public Command getCommand(FilterAction filterAction, Folder srcFolder, Object[] uids) throws Exception{
		FolderCommandReference[] r = new FolderCommandReference[1];
		r[0] = new FolderCommandReference(srcFolder, uids);
		r[0].setMarkVariant(MarkMessageCommand.MARK_AS_READ);

		MarkMessageCommand c = new MarkMessageCommand( r);

		return c;
	}

	
}
