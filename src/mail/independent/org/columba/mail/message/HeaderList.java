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

package org.columba.mail.message;

import java.util.Hashtable;

public class HeaderList extends Hashtable{
	/*
	Vector headers;
	Vector uids;
	Vector flags;
	*/
	
	
	
	public HeaderList() {
		super();
		/*	
		headers = new Vector();
		uids = new Vector();
		*/
		
	}
	
	
	public HeaderList(int initialCapacity)
	{
		super(initialCapacity);
	}

	public void add(HeaderInterface header, Object uid) {
		put(uid,header);
		
		/*
		headers.add(header);
		uids.add(uid);
		*/
		
	}

	public int count() {
		return size();
		/*
		return headers.size();
		*/
	}

	/*
	public ColumbaHeader getHeader(int index) {
		return (ColumbaHeader) headers.get(index);
	}

	public Object getUid(int index) {
		return uids.get(index);
	}
	*/
		
	
	/*
	public ColumbaHeader get(int i) {
		return (ColumbaHeader) headers.get(i);
	}
	*/
	

	public HeaderInterface getHeader(Object uid) {
		return (HeaderInterface) get(uid);
	}

}