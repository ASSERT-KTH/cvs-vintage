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

package org.columba.core.main;

import javax.swing.event.ChangeListener;

/**
 * Encapsulates the system's connection state. Enables clients to register
 * listeners in order to get notified on state changes.
 */
public interface ConnectionState {
    /**
     * Registers a listener to get notified on changes.
     */
    public void addChangeListener(ChangeListener l);
    
    /**
     * Removes a previously registered listener.
     */
    public void removeChangeListener(ChangeListener l);
    
    /**
     * Returns whether the system is currently online.
     */
    public boolean isOnline();
    
    /**
     * Sets the system's online state and notifies registered listeners.
     */
    public void setOnline(boolean b);
}
