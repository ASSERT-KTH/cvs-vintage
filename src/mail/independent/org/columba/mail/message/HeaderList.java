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