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

import org.apache.log4j.Logger;

import org.columba.ristretto.log.RistrettoLoggerInterface;


/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ColumbaRistrettoLogger implements RistrettoLoggerInterface {
    Logger log;

    public ColumbaRistrettoLogger(Logger log) {
        this.log = log;
    }

    /* (non-Javadoc)
             * @see org.columba.ristretto.log.RistrettoLoggerInterface#debug(java.lang.String)
             */
    public void debug(String message) {
        log.debug(message);
    }

    /* (non-Javadoc)
     * @see org.columba.ristretto.log.RistrettoLoggerInterface#error(java.lang.String)
     */
    public void error(String message) {
        log.error(message);
    }

    /* (non-Javadoc)
     * @see org.columba.ristretto.log.RistrettoLoggerInterface#info(java.lang.String)
     */
    public void info(String message) {
        log.info(message);
    }

    /* (non-Javadoc)
     * @see org.columba.ristretto.log.RistrettoLoggerInterface#warn(java.lang.String)
     */
    public void warn(String message) {
        log.warn(message);
    }
}
