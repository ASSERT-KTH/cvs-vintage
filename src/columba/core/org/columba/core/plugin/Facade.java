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
public class Facade {

	/**
	 * Constructor for Decorator.
	 */
	public Facade() {
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
