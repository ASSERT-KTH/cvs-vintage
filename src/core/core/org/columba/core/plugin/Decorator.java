package org.columba.core.plugin;

import java.io.File;

import org.columba.core.config.Config;
import org.columba.core.gui.util.ExceptionDialog;
import org.columba.core.logging.ColumbaLogger;
import org.columba.core.util.TempFileStore;
import org.columba.core.xml.XmlElement;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class Decorator {

	/**
	 * Constructor for Decorator.
	 */
	public Decorator() {
		super();
	}

	public static XmlElement getConfigElement(String configName) {
		XmlElement root = Config.get(configName);

		return root;
	}

	public static void showExceptionDialog(Exception ex) {
		ExceptionDialog dialog = new ExceptionDialog();
		dialog.showDialog(ex);
	}

	public static File createTempFile() {
		return TempFileStore.createTempFile();
	}

	public static void logInfo(String infoMessage) {
		ColumbaLogger.log.info(infoMessage);
	}

	public static void logDebug(String debugMessage) {
		ColumbaLogger.log.debug(debugMessage);
	}

	public static void logError(String errorMessage) {
		ColumbaLogger.log.error(errorMessage);
	}
}
