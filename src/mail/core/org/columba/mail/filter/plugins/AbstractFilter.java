package org.columba.mail.filter.plugins;

import org.columba.core.command.WorkerStatusController;
import org.columba.core.plugin.PluginInterface;
import org.columba.mail.folder.Folder;

/**
 * @author freddy
 *
 * Every FilterPlugin needs to subclass this class
 * 
 * 
 */
public abstract class AbstractFilter implements PluginInterface{

	
	/**
	 * 
	 * specify the properties the plugin needs
	 * 
	 * @return Object[] array of String
	 */
	public abstract Object[] getAttributes();

 	/**
 	 * 
 	 * execute the plugin
 	 * 
 	 * 
 	 * @param args 			attributes, requested by getAttributes()-methos
 	 * @param folder		Folder on which the filter gets applied
 	 * @param uid			uid of Message object
 	 * @param worker		WorkerStatusController is used to print information
 	 * 						on the Statusbar
 	 * @return boolean      true if match, otherwise false
 	 * 
 	 * @throws Exception    pass exception one level higher to handle it in the
 	 *                      correct  place
 	 */
	public abstract boolean process(
		Object[] args,
		Folder folder,
		Object uid,
		WorkerStatusController worker)
		throws Exception;

}
