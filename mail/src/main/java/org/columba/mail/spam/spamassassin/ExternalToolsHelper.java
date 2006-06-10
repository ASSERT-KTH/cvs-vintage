package org.columba.mail.spam.spamassassin;

import java.io.File;

import org.columba.core.gui.externaltools.ExternalToolsManager;

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

		try {
			File file = ExternalToolsManager.getInstance()
					.getLocationOfExternalTool(name);
			if ( file != null )
				return file.getPath();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
