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
package org.columba.mail.folder;


/**
 * Folders throw this exception if they find themselves in
 * a non-consistent state.
 * <p>
 * Goal is to make it possible to start an automatic recreation
 * of the header cache, etc. to fix the folder consistency.
 *
 * @author fdietz
 */

public class FolderInconsistentException extends Exception {
    /**
 * Constructs the FolderInconsistentException.java.
 * 
 * 
 */
    public FolderInconsistentException() {
        super();
    }

    /**
 * Constructs the FolderInconsistentException.java.
 * 
 * @param arg0
 */
    public FolderInconsistentException(String arg0) {
        super(arg0);
    }

    /**
 * Constructs the FolderInconsistentException.java.
 * 
 * @param arg0
 * @param arg1
 */
    public FolderInconsistentException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    /**
 * Constructs the FolderInconsistentException.java.
 * 
 * @param arg0
 */
    public FolderInconsistentException(Throwable arg0) {
        super(arg0);
    }
}
