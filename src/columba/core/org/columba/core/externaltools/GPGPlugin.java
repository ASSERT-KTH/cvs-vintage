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
public class GPGPlugin extends AbstractExternalToolsPlugin {

	//	default unix location
	File usrLinux = new File("/usr/bin/gpg");
	File localUsrLinux = new File("/usr/local/bin/gpg");

	// windows executable
	String windowsExecutable = "gpg.exe";

	/**
	 * 
	 */
	public GPGPlugin() {
		super();

	}

	/* (non-Javadoc)
	 * @see org.columba.core.externaltools.AbstractExternalToolsPlugin#getDescription()
	 */
	public String getDescription() {
		return "<html><body><p>GnuPG is a complete and free replacement for PGP.</p><p>Because it does not use the patented IDEA algorithm, it can be used without any restrictions. GnuPG is a RFC2440 (OpenPGP) compliant application.</p><p>GnuPG itself is a commandline tool without any graphical stuff. It is the real crypto engine which can be used directly from a command prompt, from shell scripts or by other programs. Therefore it can be considered as a backend for other applications.</p></body></html>";
	}

	/* (non-Javadoc)
		 * @see org.columba.core.externaltools.AbstractExternalToolsPlugin#getWebsite()
		 */
	public String getWebsite() {
		return "http://www.gnupg.org";
	}

	/* (non-Javadoc)
		 * @see org.columba.core.externaltools.AbstractExternalToolsPlugin#locate()
		 */
	public File locate() {

		// linux/unix version should have GnuPG in /usr/bin/gpg
		if (OSInfo.isLinux() || OSInfo.isSolaris()) {
			if (OSInfo.isLinux() || OSInfo.isSolaris()) {
				if (usrLinux.exists())
					return usrLinux;
				else if (localUsrLinux.exists())
					return localUsrLinux;
			}
		}

		//		TODO: add automatic detecting for windows here
		//       we should probably do a registry lookup
		//       we should be easy with java.util.prefs API

		return null;
	}
}
