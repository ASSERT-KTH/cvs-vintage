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

import java.util.EventObject;

import org.columba.mail.folder.IMailFolder;
import org.columba.ristretto.message.Flags;

/**
 * Passed to listeners to notify them of changes.
 */

public class FolderEvent extends EventObject implements IFolderEvent {
    protected Object changes;
    
    protected int parameter;
    protected Flags oldFlags;
    
	/**
	 * @param source
	 * @param changes
	 * @param parameter
	 */
	public FolderEvent(Object source, Object changes, Flags oldFlags, int parameter) {
		super(source);
		this.changes = changes;
		this.parameter = parameter;
		this.oldFlags = oldFlags;
	}
    /**
     * Creates a new event for the given folder.
     */
    public FolderEvent(IMailFolder source, Object changes) {
        super(source);
        this.changes = changes;
    }
    
    /**
	 * @param folder
	 */
	public FolderEvent(IMailFolder folder) {
		super(folder);
	}

	/**
     * Encapsulates the changes that have occured.
     */
    public Object getChanges() {
        return changes;
    }
	/**
	 * @return Returns the parameter.
	 */
	public int getParameter() {
		return parameter;
	}
	/**
	 * @return Returns the oldFlags.
	 */
	public Flags getOldFlags() {
		return oldFlags;
	}
}
