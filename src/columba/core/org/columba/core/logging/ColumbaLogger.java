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

package org.columba.core.logging;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.columba.core.config.Config;
import org.columba.core.main.MainInterface;

/**
 * A wrapper class for log4j. This class initialized and configures
 * a log4j logger. Depending on the debug flag (--debug command line
 * option reflected in MainInterface.DEBUG) the logger will either
 * show all debug messages (debug, info, warn, error) or nothing.
 */
public class ColumbaLogger {
	public static Logger log;

	static {
		log = Logger.getLogger("org.columba");
		PropertyConfigurator.configure(Config.getLoggingPropertyFile().toString());
		if (MainInterface.DEBUG) {
			log.setLevel(Level.DEBUG);
		} else {
			log.setLevel(Level.OFF);
		}
	}
}
