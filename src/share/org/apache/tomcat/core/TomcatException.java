/*   
 *  Copyright 1999-2004 The Apache Sofware Foundation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.tomcat.core;


/**
 * General Tomcat exception
 *
 * @author Conor MacNeill [conor@m64.com]
 */
public class TomcatException extends Exception {

    private Throwable rootCause;

    public TomcatException() {
        super();
    }

    public TomcatException(String msg) {
        super(msg);
    }

    /**
     * Create a connector exception to encapsulate some underlying eception.
     */
    public TomcatException(Throwable rootCause) {
        super("Root cause - " + rootCause.getLocalizedMessage());
        this.rootCause = rootCause;
    }

    /**
     * Create a connector exception to encapsulate some underlying eception.
     */
    public TomcatException(String msg, Throwable rootCause) {
        super(msg);
        this.rootCause = rootCause;
    }

    /**
     * Get the underlying exception that caused this ConnectorException.
     *
     * @return the <code>Throwable</code>
     *         that caused this connector exception
     */
    public Throwable getRootCause() {
	// Does any other Exception have a "getRootCause" ?
        return rootCause;
    }

    /**
     * Get the underlying exception that caused this ConnectorException.
     * Follow the pattern from SQLException, etc.
     *
     * @return the <code>Throwable</code>
     *         that caused this connector exception
     */
    public Throwable getNextException() {
        return rootCause;
    }
}
