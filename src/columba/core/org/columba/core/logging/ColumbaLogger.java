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

import java.io.File;
import java.io.IOException;

import java.util.logging.*;

import org.columba.core.main.MainInterface;
import org.columba.core.main.MainInterface;
import org.columba.ristretto.log.RistrettoLogger;

/**
 * Depending on the debug flag (--debug command line option reflected
 * in MainInterface.DEBUG) the logger will either show all debug messages
 * or just severe errors. Logging information is passed to a log file and
 * to the console.
 * <p>
 * Note, that ColumbaLogger must not be called before MainInterface.DEBUG,
 * was set. Otherwise, the logger won't show the correct debug level.
 * 
 * @see org.columba.core.main.Main 
 */
public class ColumbaLogger {
    
    /**
     * A static reference to the actual logger. This reference is final so
     * that it cannot be reassigned. However, you are free to add further
     * handlers to the logger.
     */
    public static final Logger log;

    static {
        log = Logger.getLogger("org.columba");
        log.setUseParentHandlers(false);

        // create logging file in users config-folder
        File loggingFile = new File(MainInterface.config.getConfigDirectory(), "columba.log");
        Handler handler;
        try {
            handler = new FileHandler(loggingFile.getPath(), false);
            
            // don't use standard XML formatting
            handler.setFormatter(new SimpleFormatter());
            log.addHandler(handler);
        } catch (IOException ioe) {
            // TODO: how to handle this?
        }

        // TODO: only add console handler if command line option is given
        // init console handler
        handler = new ConsoleHandler();
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

        /*
         Ristretto is a singleton library and doesn't know about Columba.
         We need to connect ristretto's logger with Columba's logger therefore.
         */
        RistrettoLogger.setParentLogger(log);
        if( MainInterface.DEBUG ) {
            RistrettoLogger.setLogStream(System.out);
        }
    }
    
    /**
     * Don't instanciate this class.
     */
    private ColumbaLogger() {}
}
