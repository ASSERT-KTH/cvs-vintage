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
package org.columba.core.facade;

import java.util.logging.Logger;

/**
 * A facade for logging in columba.
 * @author frd
 */
public final class LoggingFacade {

    private static final Logger LOG = Logger.getLogger("org.columba.core.facade");

    /**
     * Utility classes should have a private constructor.
     */
    private LoggingFacade() {
    }

    /**
     * Log a message that are reasonably significant and informative.
     * The message is intended for end users.
     * @param infoMessage the message.
     */
    public static void logInfo(String infoMessage) {
        LOG.info(infoMessage);
    }

    /**
     * Log a message that are intended for tracing.
     * The message is intended for developers.
     * @param debugMessage the message.
     */
    public static void logDebug(String debugMessage) {
        LOG.fine(debugMessage);
    }

    /**
     * Log a message when there is a chance Columba may not function properly.
     * The message is intended for end users or system administrators.
     * @param errorMessage the message.
     */
    public static void logError(String errorMessage) {
        LOG.severe(errorMessage);
    }

    /**
     * Log a message when a potential problem has occurred.
     * The message is intended for end users or system administrators.
     * @param warningMessage the message.
     */
    public static void logWarning(String warningMessage) {
        LOG.severe(warningMessage);
    }
}
