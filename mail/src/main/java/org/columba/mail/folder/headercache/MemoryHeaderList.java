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

import java.util.Hashtable;
import java.util.Set;

import org.columba.mail.message.ICloseableIterator;
import org.columba.mail.message.IColumbaHeader;
import org.columba.mail.message.IHeaderList;

public class MemoryHeaderList implements IHeaderList {
    protected Hashtable map;

    public MemoryHeaderList() {
    	map = new Hashtable();
    }
    
    public void add(IColumbaHeader header, Object uid) {
        if( header.get("columba.uid") == null) {
        	header.set("columba.uid", uid);
        } 
    	
    	map.put(uid, header);
    }

    public int count() {
        return map.size();
    }

    public boolean exists(Object uid) {
        return map.containsKey(uid);
    }

    public Set keySet() {
    	return map.keySet();
    }
    

    public void clear() {
        map.clear();
    }

	public IColumbaHeader get(Object uid) {
		return (IColumbaHeader) map.get(uid);
	}

	public Object[] getUids() {
		return map.keySet().toArray();
	}

	public IColumbaHeader remove(Object uid) {
		return (IColumbaHeader)map.remove(uid);
	}

	public ICloseableIterator headerIterator() {
		return  new DefaultCloseableIterator( map.values().iterator() );
	}

	public void update(Object uid, IColumbaHeader header) {
		//Update is unnecessary
	}

	public ICloseableIterator keyIterator() {
		return new DefaultCloseableIterator(map.keySet().iterator());
	}
}
