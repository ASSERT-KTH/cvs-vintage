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

import java.io.IOException;

import java.util.logging.*;

import org.columba.core.config.Config;
import org.columba.core.main.MainInterface;

import org.columba.ristretto.log.RistrettoLogger;

/**
 * Depending on the debug flag (--debug command line
 * option reflected in MainInterface.DEBUG) the logger will either
 * show all debug messages or just severe errors.
 */
public class ColumbaLogger {
    public static Logger log;

    static {
        log = Logger.getLogger("org.columba");
        log.setUseParentHandlers(false);
        try {
            FileHandler handler = new FileHandler("log/columba.log", false);
            handler.setFormatter(new SimpleFormatter());
            log.addHandler(handler);
        } catch(IOException ioe) {
            log.severe(ioe.getMessage());
        }
        if (MainInterface.DEBUG) {
            log.setLevel(Level.ALL);
        } else {
            log.setLevel(Level.SEVERE);
        }

        RistrettoLogger.setDebugEnabled(MainInterface.DEBUG);
        RistrettoLogger.setLogger(new ColumbaRistrettoLogger(log));
    }
}
