package org.columba.mail.spam.spamassassin;
import java.io.File;

import org.columba.core.plugin.PluginManager;
import org.columba.core.pluginhandler.ExternalToolsExtensionHandler;


/**
 * @author fdietz
 */
public class ExternalToolsHelper {
    public static String getSpamc() {
        return get("spamc");
    }

    public static String getSpamassassin() {
        return get("spamassassin");
    }

    public static String getSALearn() {
        return get("sa-learn");
    }

    public static String get(String name) {
        ExternalToolsExtensionHandler handler = null;

        try {
            handler = (ExternalToolsExtensionHandler) PluginManager.getInstance().getHandler(
                    "org.columba.core.externaltools");
            File file = handler.getLocationOfExternalTool(name);
            
            return file.getPath();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
