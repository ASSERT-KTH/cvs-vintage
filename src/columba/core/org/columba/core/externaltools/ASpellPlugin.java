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
package org.columba.core.externaltools;

import java.io.File;

import org.columba.core.util.OSInfo;

/**
 * Plugin for the aspell spell-checking package.
 * 
 * TODO: overwrite locate() and use some platform dependent
 * good-guessing where the tool might be installed.
 * 
 * @author fdietz
 */
public class ASpellPlugin extends AbstractExternalToolsPlugin {

	// default unix location
	File usrLinux = new File("/usr/bin/aspell");
	File localUsrLinux = new File("/usr/local/bin/aspell");
	
	// windows executable
	String windowsExecutable = "aspell.exe";

	/**
	 * 
	 */
	public ASpellPlugin() {
		super();

	}

	/* (non-Javadoc)
	 * @see org.columba.core.externaltools.AbstractExternalToolsPlugin#getDescription()
	 */
	public String getDescription() {
		// TODO: i18n
		return "<html><body><p>GNU Aspell is a Free and Open Source spell checker designed to eventually replace Ispell.</p><p>It can either be used as a library or as an independent spell checker. Its main feature is that it does a much better job of coming up with possible suggestions than just about any other spell checker out there for the English language, including Ispell and Microsoft Word.</p></p>It also has many other technical enhancements over Ispell such as using shared memory for dictionaries and intelligently handling personal dictionaries when more than one Aspell process is open at once.</p></body></html>";
	}

	/* (non-Javadoc)
	 * @see org.columba.core.externaltools.AbstractExternalToolsPlugin#getWebsite()
	 */
	public String getWebsite() {
		return "http://aspell.sourceforge.net";
	}

	/* (non-Javadoc)
	 * @see org.columba.core.externaltools.AbstractExternalToolsPlugin#locate()
	 */
	public File locate() {
		
		// linux/unix version should have aspell in /usr/bin/aspell
		if (OSInfo.isLinux() || OSInfo.isSolaris()) {
			if (usrLinux.exists())
				return usrLinux;
			else if ( localUsrLinux.exists() )
				return localUsrLinux;
		}

		// TODO: add automatic detecting for windows here
		//       we should probably do a registry lookup
		//       we should be easy with java.util.prefs API
		
		return null;
	}

}
