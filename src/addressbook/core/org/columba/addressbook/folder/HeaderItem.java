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

package org.columba.addressbook.folder;

import java.util.Enumeration;
import java.util.Hashtable;

public class HeaderItem {

	//private DefaultCard item;
	private Hashtable hashtable;
	//private int type;

	public static int CONTACT = 0;
	public static int GROUPLIST = 1;

	private Object uid;

	private Folder folder;

	public HeaderItem() {

		hashtable = new Hashtable();
	}

	public HeaderItem(int type) {

		hashtable = new Hashtable();

		if (type == CONTACT)
			add("type", "contact");
		else
			add("type", "grouplist");
	}

	public boolean matchPattern(String pattern) {
		if (pattern == null)
			return false;

		pattern = pattern.toLowerCase();

		if (isContact()) {
			String displayname = (String) get("displayname");
			String address = (String) get("email;internet");

			if (address != null) {
				address = address.toLowerCase();
				if (address.startsWith(pattern) == true)
					return true;
			}

			if (displayname != null) {
				displayname = displayname.toLowerCase();
				if (displayname.startsWith(pattern) == true)
					return true;
			}

			return false;
		} else {
			String displayname = (String) get("displayname");

			if (displayname != null) {
				displayname = displayname.toLowerCase();
				if (displayname.startsWith(pattern) == true)
					return true;
			}

			return false;
		}
	}

	public Folder getFolder() {
		return folder;
	}

	public void setFolder(Folder f) {
		folder = f;
	}

	public boolean contains(String key) {
		if (hashtable.containsKey(key))
			return true;

		return false;
	}

	public boolean isContact() {
		String type = (String) get("type");
		if (type.equals("contact"))
			return true;
		else
			return false;
	}

	public void add(Object key, Object value) {
		if ((key != null) && (value != null)) {

			hashtable.put(((String) key).toLowerCase(), value);
		}
	}

	public Object get(Object key) {
		if (key != null) {
			Object value = hashtable.get(((String) key).toLowerCase());

			return value;
		} else
			return null;
	}

	public Enumeration elements() {
		return hashtable.elements();
	}

	public Enumeration keys() {
		return hashtable.keys();
	}

	/*
	public DefaultCard getCard()
	{
		return item;
	}
	
	public void setCard( DefaultCard item )
	{
		this.item = item;
	}
	*/

	public void setUid(Object uid) {
		this.uid = uid;
	}
	public void setHashtable(Hashtable t) {
		this.hashtable = t;
	}

	public Object getUid() {
		return uid;
	}

	public Object clone() {
		/*
		HeaderItem item = null;
		try{
			item = (HeaderItem)super.clone();
		}catch(CloneNotSupportedException cnse){} //does not occur
		//necessary?
		item.setHashtable((Hashtable) hashtable.clone());
		return item;
		*/

		HeaderItem item = new HeaderItem();
		item.setUid(getUid());
		item.setFolder(getFolder());

		Hashtable t = new Hashtable();
		for (Enumeration keys = hashtable.keys(); keys.hasMoreElements();) {
			String key = (String) keys.nextElement();

			Object value = hashtable.get(key);
			t.put(key,value);
			
		}
		
		item.setHashtable(t);
		//item.setHashtable( (Hashtable) hashtable.clone() );

		return item;
	}
}
