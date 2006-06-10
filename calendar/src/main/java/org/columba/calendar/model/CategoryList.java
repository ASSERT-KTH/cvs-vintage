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

import org.columba.calendar.model.api.ICategoryList;

public class CategoryList implements ICategoryList {

	private Vector<String> vector = new Vector<String>();

	public CategoryList() {
		super();
	}

	public void addCategory(String category) {
		vector.add(category);
	}

	public void removeCategory(String category) {
		vector.remove(category);
	}

	public Iterator<String> getCategoryIterator() {
		return vector.iterator();
	}

	public String getCategories() {
		// TODO (@author fdietz): add catgory support
		return null;
	}

	public void setCategories(String categories) {
		// TODO (Sauthor fdietz): add catgory support
		
	}

}
