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
package org.columba.core.gui.externaltools;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.columba.core.base.OSInfo;


/**
 * Plugin for the aspell spell-checking package.
 *
 * @author fdietz
 */
public class GPGPlugin extends AbstractExternalToolsPlugin {
    protected static File defaultLinux = new File("/usr/bin/gpg");
    protected static File defaultLocalLinux = new File("/usr/local/bin/gpg");

    /* GPG for windows is an executable-only download, fortunately there is
 * a windows registry file included in the download and has this as the
 * default installation path in it. While users will probably install GPG
 * into many other places, this is atleast a best-guess start.
 */
    protected static File defaultWin = new File("C:\\GnuPG\\gpg.exe");
    protected static URL websiteURL;

    static {
        try {
            websiteURL = new URL("http://www.gnupg.org/");
        } catch (MalformedURLException mue) {
        }

        //does not happen
    }

    /**
 * Construct the default GPG plugin.
 */
    public GPGPlugin() {
        super();
    }

    public String getDescription() {
        return "<html><body><p>GnuPG is a complete and free replacement for PGP.</p><p>Because it does not use the patented IDEA algorithm, it can be used without any restrictions. GnuPG is a RFC2440 (OpenPGP) compliant application.</p><p>GnuPG itself is a commandline tool without any graphical stuff. It is the real crypto engine which can be used directly from a command prompt, from shell scripts or by other programs. Therefore it can be considered as a backend for other applications.</p></body></html>";
    }

    public URL getWebsite() {
        return websiteURL;
    }

    public File locate() {
        /* If this is a unix-based system, check the 2 best-known areas for the
 * gpg binary.
 */
        if (OSInfo.isLinux() || OSInfo.isSolaris()) {
            if (defaultLinux.exists()) {
                return defaultLinux;
            } else if (defaultLocalLinux.exists()) {
                return defaultLocalLinux;
            }
        }

        /* RIYAD: The Prefs API cannot be used to read the Window's registry,
 * it is coded to use the registry (if available) as a backing store
 * on in the SOFTWARE/JavaSoft/Prefs registry keys for HKEY_CURRENT_USER
 * and HKEY_LOCAL_MACHINE paths. I have seen a few java apps that use
 * the Windows registry and they all required a native lib to do it.
 */
        /* If this is windows, check the default installation location for the
 * gpg.exe binary.
 */
        if (OSInfo.isWin32Platform() && defaultWin.exists()) {
            return defaultWin;
        }

        /* Couldn't find anything, so return null and let the wizard ask the
 * user.
 */
        return null;
    }
}
