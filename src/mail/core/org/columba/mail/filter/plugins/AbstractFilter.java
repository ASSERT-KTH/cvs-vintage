package org.columba.mail.filter.plugins;

import org.columba.core.command.WorkerStatusController;
import org.columba.core.plugin.PluginInterface;
import org.columba.mail.folder.Folder;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public abstract class AbstractFilter implements PluginInterface{

	

	public abstract Object[] getAttributes();

	public abstract boolean process(
		Object[] args,
		Folder folder,
		Object uid,
		WorkerStatusController worker)
		throws Exception;

}
