// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Library General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

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
