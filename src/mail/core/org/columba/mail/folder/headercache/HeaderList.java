package org.columba.mail.folder.headercache;

import java.util.Enumeration;
import java.util.Hashtable;

import org.columba.mail.message.IColumbaHeader;
import org.columba.mail.message.IHeaderList;
import org.columba.ristretto.message.Attributes;
import org.columba.ristretto.message.Flags;
import org.columba.ristretto.message.Header;

public class HeaderList implements IHeaderList {
    protected Hashtable map;

    public HeaderList() {
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

    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    public Enumeration keys() {
        return map.keys();
    }
    
    public Enumeration elements() {
    	return map.elements();
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

	public void setAttribute(Object uid, String key, Object value) {
		IColumbaHeader header = get(uid);
		header.getAttributes().put(key, value);
		
	}

	public Object getAttribute(Object uid, String key) {
		IColumbaHeader header = get(uid);
		return header.getAttributes().get(key);
	}

	public Flags getFlags(Object uid) {
		IColumbaHeader header = get(uid);
		return header.getFlags();
	}

	public Attributes getAttributes(Object uid) {
		IColumbaHeader header = get(uid);
		return header.getAttributes();
	}

	public Header getHeaderFields(Object uid, String[] keys) {
		IColumbaHeader header = get(uid);
		// copy fields
		Header result = new Header();

		for (int i = 0; i < keys.length; i++) {
			if (header.get(keys[i]) != null) {
				// headerfield found
				result.set(keys[i], header.get(keys[i]));
			}
		}

		return result;		
	}
	
	public IColumbaHeader remove(Object uid) {
		return (IColumbaHeader)map.remove(uid);
	}
}
