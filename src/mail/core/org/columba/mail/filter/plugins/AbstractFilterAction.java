package org.columba.mail.filter.plugins;

import org.columba.core.command.Command;
import org.columba.core.plugin.DefaultPlugin;
import org.columba.mail.filter.FilterAction;
import org.columba.mail.folder.Folder;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public abstract class AbstractFilterAction extends DefaultPlugin{

	
	public abstract Command getCommand(FilterAction filterAction, Folder srcFolder, Object[] uids) throws Exception;
	
	
}
