package org.columba.core.plugin;

import java.io.File;
import java.util.Vector;

import org.columba.core.logging.ColumbaLogger;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class PluginFinder {

	/**
	 * Constructor for PluginFinder.
	 */
	public PluginFinder() {
		super();
	}
	
	public static File[] searchPlugins()
	{
		Vector v = new Vector();
		
		File programFolder = new File("plugins");
		
		if ( programFolder.exists() )
		{
			File[] list = programFolder.listFiles();
			
			return list;
		}
		else ColumbaLogger.log.info("Folder \"plugins\" doesn't exist.");
			
		
		
		return null;
	}

}
