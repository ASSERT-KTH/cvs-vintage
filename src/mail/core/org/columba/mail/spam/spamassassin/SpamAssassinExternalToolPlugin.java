package org.columba.mail.spam.spamassassin;
import org.columba.core.externaltools.AbstractExternalToolsPlugin;
import org.columba.core.util.OSInfo;

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
import java.io.File;

import java.net.MalformedURLException;
import java.net.URL;


/**
 * @author fdietz
 */
public class SpamAssassinExternalToolPlugin extends AbstractExternalToolsPlugin {
    protected static URL websiteURL;

    static {
        try {
            websiteURL = new URL("http://www.spamassassin.org/");
        } catch (MalformedURLException mue) {
        }
         //does not happen
    }

    File defaultLinux = new File("/usr/bin/spamassassin");
    File defaultLocalLinux = new File("/usr/local/bin/spamassassin");

    public SpamAssassinExternalToolPlugin() {
        super();
    }

    public String getDescription() {
        return "<html><body><p>spamassassin - mail filter to identify spam using text analysis</p></body></html>";
    }

    public URL getWebsite() {
        return websiteURL;
    }

    public File locate() {
        /*
         * If this is a unix-based system, check the 2 best-known areas for the
         * aspell binary.
         */
        if (OSInfo.isLinux() || OSInfo.isSolaris()) {
            if (defaultLinux.exists()) {
                return defaultLinux;
            } else if (defaultLocalLinux.exists()) {
                return defaultLocalLinux;
            }
        }

        return null;
    }
}
