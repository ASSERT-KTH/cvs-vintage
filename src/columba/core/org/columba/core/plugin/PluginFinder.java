// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Library General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

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
