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
package org.columba.mail.command;

import org.columba.core.command.DefaultCommandReference;
import org.columba.mail.pop3.POP3Server;


/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class POP3CommandReference extends DefaultCommandReference {
    protected POP3Server server;

    public POP3CommandReference(POP3Server server) {
        this.server = server;
    }

    /**
 * Returns the server.
 * @return POP3Server
 */
    public POP3Server getServer() {
        return server;
    }

    /**
 * @see org.columba.core.command.DefaultCommandReference#releaseLock(java.lang.Object)
 */
    public void releaseLock(Object locker) {
        server.releaseLock(locker);
    }

    /**
 * @see org.columba.core.command.DefaultCommandReference#tryToGetLock(java.lang.Object)
 */
    public boolean tryToGetLock(Object locker) {
        return server.tryToGetLock(locker);
    }
}
