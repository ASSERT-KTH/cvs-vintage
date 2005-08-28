// The contents of this file are subject to the Mozilla Public License Version
// 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.mail.folder.headercache;

import java.io.IOException;

import org.columba.mail.folder.IHeaderListStore;
import org.columba.mail.folder.IMailbox;
import org.columba.mail.message.ColumbaHeader;
import org.columba.mail.message.IColumbaHeader;
import org.columba.ristretto.message.Attributes;
import org.columba.ristretto.message.Flags;
import org.columba.ristretto.message.Header;


/**
 * Wrapper around a Hashtable to allow typesafe
 * mapping of {@link ColumbaHeader} objects.
 * <p>
 * Every {@link IMailbox} uses this headerlist
 * internally to store headerfields.
 * <p>
 *
 * @see CachedHeaderfields
 *
 * @author fdietz
 */
public class PersistantHeaderList extends HeaderList {
    protected IHeaderListStore store;
    
    private boolean restored;
    private boolean dirty;
    
    public PersistantHeaderList(IHeaderListStore store) {
    	super();
        this.store = store;
        
        restored = false;
        dirty = false;
    }

	/**
	 * @return Returns the store.
	 */
	public IHeaderListStore getStore() {
		return store;
	}

	/**
	 * @param store The store to set.
	 */
	public void setStore(IHeaderListStore store) {
		this.store = store;
	}

	public void restore() throws IOException {
		if( !restored ) {
			store.restoreHeaderList(this);
			restored = true;
			// This is necessary because dirty will be set to true while loading
			dirty = false;
		}
	}

	public void persist() throws IOException {
		if( dirty ) {
			store.persistHeaderList(this);
			dirty = false;
		}
	}

	/**
	 * @see org.columba.mail.folder.headercache.HeaderList#add(org.columba.mail.message.IColumbaHeader, java.lang.Object)
	 */
	public void add(IColumbaHeader header, Object uid) {
		dirty = true;
		super.add(header, uid);
	}

	/**
	 * @see org.columba.mail.folder.headercache.HeaderList#clear()
	 */
	public void clear() {
		dirty = true;
		super.clear();
	}

	/**
	 * @see org.columba.mail.folder.headercache.HeaderList#remove(java.lang.Object)
	 */
	public IColumbaHeader remove(Object uid) {
		dirty = true;
		return super.remove(uid);
	}

	/**
	 * @return Returns the dirty.
	 */
	public boolean isDirty() {
		return dirty;
	}

	/**
	 * @return Returns the restored.
	 */
	public boolean isRestored() {
		return restored;
	}

	/* (non-Javadoc)
	 * @see org.columba.mail.folder.headercache.HeaderList#getAttributes(java.lang.Object)
	 */
	public Attributes getAttributes(Object uid) {
		dirty = true;
		return super.getAttributes(uid);
	}

	/* (non-Javadoc)
	 * @see org.columba.mail.folder.headercache.HeaderList#getFlags(java.lang.Object)
	 */
	public Flags getFlags(Object uid) {
		dirty = true;
		return super.getFlags(uid);
	}

	/* (non-Javadoc)
	 * @see org.columba.mail.folder.headercache.HeaderList#setAttribute(java.lang.Object, java.lang.String, java.lang.Object)
	 */
	public void setAttribute(Object uid, String key, Object value) {
		dirty = true;
		super.setAttribute(uid, key, value);
	}
	
}
