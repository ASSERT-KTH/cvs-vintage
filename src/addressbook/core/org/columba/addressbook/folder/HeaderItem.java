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
	
	public String toString()
		{
			String str = (String) get("displayname");
		
			if ( str == null ) str = (String) get("email;internet");
				
			return str;
		}
}
