package org.columba.core.util;

import org.columba.mail.folder.Folder;

/**
 * @author timo
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class Lock {
	private boolean locked;
	private Object owner;
	
	public Lock(Object owner) {
		locked = false;
		this.owner = owner;
	}	

	public synchronized boolean tryToGetLock() {
		if( locked ) return false;
		else {
			locked = true;
			return true;	
		}
	}

	public void release() {
		locked = false;				
	}

	/**
	 * Returns the owner.
	 * @return Folder
	 */
	public Object getOwner() {
		return owner;
	}

}