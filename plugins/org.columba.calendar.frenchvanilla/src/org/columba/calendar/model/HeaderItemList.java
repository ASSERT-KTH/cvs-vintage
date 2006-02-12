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
package org.columba.calendar.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class HeaderItemList implements IHeaderItemList {

	private List list;

	public HeaderItemList() {
		super();
		list = new ArrayList();
	}

	public void add(IHeaderItem item) {
		list.add(item);
	}

	public IHeaderItem get(int index) {
		return (IHeaderItem) list.get(index);
	}

	public void remove(int index) {
		list.remove(index);
	}

	public void remove(IHeaderItem item) {
		list.remove(item);
	}

	public int count() {
		return list.size();
	}

	public void clear() {
		list.clear();
	}

	public Iterator iterator() {
		return list.iterator();
	}

}
