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

package org.columba.mail.folder.event;

import java.util.EventListener;

/**
 * Clients can attach implementations of this interface to a folder
 * in order to get notified on folder changes.
 */
public interface FolderListener extends EventListener {
    
    /**
     * Called when a message has been added to a folder.
     */
    public void messageAdded(FolderEvent e);
    
    /**
     * Called when a message has been removed from a folder.
     */
    public void messageRemoved(FolderEvent e);
    
    /**
     * Called when a message flag changed.
     * @param e
     */
    public void messageFlagChanged(FolderEvent e);
    
    /**
     * Called when a folder has been renamed.
     */
    public void folderPropertyChanged(FolderEvent e);
    
    /**
     * Called when a subfolder has been added to a folder.
     */
    public void folderAdded(FolderEvent e);
    
    /**
     * Called when a folder has been removed from its parent folder.
     */
    public void folderRemoved(FolderEvent e);
}
