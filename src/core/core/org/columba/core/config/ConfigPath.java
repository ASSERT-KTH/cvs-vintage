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

package org.columba.core.config;

import java.io.File;
import java.io.IOException;

import org.columba.core.io.DiskIO;

/**
 * @author frd
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ConfigPath {

	public static String lineSeparator;
	public static String fileSeparator;
	public static String osName;
	public static String userName;
	public static String userHome;

	public static File configDirectory;

	public ConfigPath() {
		getSystemProperties();
		
		configDirectory.mkdir();
	}

	public ConfigPath(String configPath) {
		
		this.configDirectory = new File(configPath);
		
		configDirectory.mkdir();
	}

	protected boolean isWindowsPlatform() {
		if (osName.equals("Windows 95"))
			return true;
		if (osName.equals("Windows 98"))
			return true;

		return false;
	}

	public static File getConfigDirectory() {
		return configDirectory;
	}

	protected void getSystemProperties() {
		String hstr;

		lineSeparator = new String(System.getProperty("line.separator"));
		fileSeparator = new String(System.getProperty("file.separator"));
		osName = new String(System.getProperty("os.name"));

		//System.out.println("os.arch: "+System.getProperty("os.arch") );
		//System.out.println("os.version: "+System.getProperty("os.version") );

		userName = new String(System.getProperty("user.name"));
		userHome = new String(System.getProperty("user.home"));
		//userDir = new String(System.getProperty("user.dir"));
		//System.out.println("User home: "+userHome);

		if (isWindowsPlatform() == true) {
			// this os has no home directory
			// for example windows9x

			if (configDirectory == null)
				configDirectory = new File("config");

		} else {
			if (configDirectory == null)
				configDirectory = new File(userHome, ".columba");
		}

		try {
			hstr = configDirectory.getCanonicalPath();
		} catch (IOException e) {
			hstr = userHome;
		}

		configDirectory = new File(hstr);
		//DiskIO.ensureDirectory(hstr);
		
	

		//createDefaultConfigFiles();

	}

}
