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

import java.util.Iterator;
import java.util.Vector;

import org.columba.calendar.model.api.IComponentInfo;
import org.columba.calendar.model.api.IComponentInfoList;

public class ComponentInfoList implements IComponentInfoList {

	private Vector<IComponentInfo> vector = new Vector<IComponentInfo>();

	public ComponentInfoList() {
		super();
	}

	public void add(IComponentInfo item) {
		vector.add(item);
	}

	public IComponentInfo get(int index) {
		return vector.get(index);
	}

	public void remove(int index) {
		vector.remove(index);
	}

	public void remove(IComponentInfo item) {
		vector.remove(item);
	}

	public int count() {
		return vector.size();
	}

	public void clear() {
		vector.clear();
	}

	public Iterator<IComponentInfo> iterator() {
		return vector.iterator();
	}

}
