package org.columba.mail.spam.spamassassin;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.columba.core.base.OSInfo;
import org.columba.core.gui.externaltools.AbstractExternalToolsPlugin;


/**
 * @author fdietz
 */
public class SpamcExternalToolPlugin extends AbstractExternalToolsPlugin {
    protected static URL websiteURL;

    static {
        try {
            websiteURL = new URL("http://www.spamassassin.org/");
        } catch (MalformedURLException mue) {
        }
         //does not happen
    }

    File defaultLinux = new File("/usr/bin/spamc");
    File defaultLocalLinux = new File("/usr/local/bin/spamc");

    public SpamcExternalToolPlugin() {
        super();
    }

    public String getDescription() {
        return "<html><body><p>spamc - client for spamd</p></body></html>";
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
