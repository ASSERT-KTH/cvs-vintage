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

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.columba.core.main.MainInterface;
import org.columba.ristretto.log.RistrettoLogger;

/**
 * Depending on the debug flag (--debug command line
 * option reflected in MainInterface.DEBUG) the logger will either
 * show all debug messages or just severe errors.
 * <p>
 * Changed this from a completely static class, to something
 * which has to be instanciated explicitly from org.columba.core.main.Main.
 * This is necessary to have a proper initialization of MainInterface.DEBUG.
 * <p>
 * Switched to ConsoleHandler, instead of FileHandler. Nevertheless, would be
 * useful to add a commandline switch to optionally enable a file handler.
 * 
 */
public class ColumbaLogger {
    public static Logger log;

    public ColumbaLogger() {
        log = Logger.getLogger("org.columba");
        log.setUseParentHandlers(false);

        /*
        // create logging file in users config-folder
        File loggingFile = new File(ConfigPath.getConfigDirectory(), "columba.log");
        FileHandler handler = new FileHandler(loggingFile.getPath(), false);
        */

        // init console handler
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new SimpleFormatter());
        log.addHandler(handler);

        if (MainInterface.DEBUG) {
            log.setLevel(Level.ALL);

            // init java.net.ssl debugging
            System.setProperty(
                "javax.net.debug",
                "ssl,handshake,data,trustmanager");
        } else {
            log.setLevel(Level.SEVERE);
        }

        RistrettoLogger.setParentLogger(log);

    }
}
