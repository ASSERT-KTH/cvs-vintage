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
package org.columba.mail.message;

import org.columba.mail.folder.MailboxInterface;
import org.columba.mail.folder.headercache.CachedHeaderfields;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;


/**
 * Wrapper around a Hashtable to allow typesafe
 * mapping of {@link ColumbaHeader} objects.
 * <p>
 * Every {@link MailboxInterface} uses this headerlist
 * internally to store headerfields.
 * <p>
 *
 * @see CachedHeaderfields
 *
 * @author fdietz
 */
public class HeaderList {
    protected Map map;

    public HeaderList() {
        map = new Hashtable();
    }

    public HeaderList(int initialCapacity) {
        map = new Hashtable(initialCapacity);
    }

    public void add(ColumbaHeader header, Object uid) {
        map.put(uid, header);
    }

    public int count() {
        return map.size();
    }

    public boolean containsKey(Object uid) {
        return map.containsKey(uid);
    }

    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    public ColumbaHeader get(Object uid) {
        return (ColumbaHeader) map.get(uid);
    }

    public Object remove(Object uid) {
        return map.remove(uid);
    }

    public Enumeration keys() {
        return ((Hashtable) map).keys();
    }

    public void clear() {
        map.clear();
    }
}
