// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Library General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

package org.columba.mail.util;

import java.util.Hashtable;

import org.columba.addressbook.folder.HeaderItem;

public class AddressCollector {

    static private Hashtable _adds = new Hashtable();

    static public void addAddress(String add, HeaderItem item) {
    	if ( add != null ) 
	_adds.put(add, item);
    }

    static public Object[] getAddresses() {
	return _adds.keySet().toArray();
    }
    
    static public HeaderItem getHeaderItem( String add )
    {
    	return (HeaderItem) _adds.get(add);
    }
    
    static public void clear()
    {
    	_adds.clear();
    }

}
