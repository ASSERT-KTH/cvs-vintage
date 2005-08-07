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
public class ASpellPlugin extends AbstractExternalToolsPlugin {
    protected static File defaultLinux = new File("/usr/bin/aspell");
    protected static File defaultLocalLinux = new File("/usr/local/bin/aspell");
    protected static File defaultWin = new File(
            "C:\\Program Files\\Aspell\\bin\\aspell.exe");
    protected static URL websiteURL;

    static {
        try {
            websiteURL = new URL("http://aspell.sourceforge.net/");
        } catch (MalformedURLException mue) {
        }

        //does not happen
    }

    /**
 * Construct the default ASpell plugin.
 */
    public ASpellPlugin() {
        super();
    }

    public String getDescription() {
        // TODO (@author fdietz): i18n
        return "<html><body><p>GNU Aspell is a Free and Open Source spell checker designed to eventually replace Ispell.</p><p>It can either be used as a library or as an independent spell checker. Its main feature is that it does a much better job of coming up with possible suggestions than just about any other spell checker out there for the English language, including Ispell and Microsoft Word.</p></p>It also has many other technical enhancements over Ispell such as using shared memory for dictionaries and intelligently handling personal dictionaries when more than one Aspell process is open at once.</p></body></html>";
    }

    public URL getWebsite() {
        return websiteURL;
    }

    public File locate() {
        /* If this is a unix-based system, check the 2 best-known areas for the
 * aspell binary.
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
 * aspell.exe binary.
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
