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
package org.columba.mail.gui.tree.util;

import java.io.File;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.tree.TreePath;

public class TreeNodeList {
	Vector list;

	public TreeNodeList() {
		list = new Vector();
	}

	public TreeNodeList(Vector v) {
		list = v;
	}

	public TreeNodeList(String[] str) {
		for (int i = 0; i < str.length; i++) {
			list.add(str[i]);
		}

	}

	public TreeNodeList(String s) {
		list = new Vector();

		StringTokenizer tok = new StringTokenizer(s, "/");

		while (tok.hasMoreTokens()) {
			String next = tok.nextToken();

			list.add(next);
		}

	}

	public TreePath getTreePath() {
		TreePath path = new TreePath(get(0));

		for (int i = 1; i < count(); i++) {

			Object o = get(i);
			path = path.pathByAddingChild(o);
		}

		return path;
	}

	public void removeElementAt(int index) {
		getList().removeElementAt(index);
	}

	public Vector getList() {
		return list;
	}

	public void setElementAt(String s, int i) {
		list.setElementAt(s, i);

	}

	public void insertElementAt(String s, int i) {
		list.insertElementAt(s, i);
	}

	public void add(String s) {
		list.add(s);
	}

	public String get(int i) {
		if (count() > 0)
			return (String) list.get(i);
		else
			return new String("");
	}

	public int count() {
		return list.size();
	}

	public void clear() {
		list.clear();
	}

	public String lastElement() {
		return (String) list.lastElement();
	}

	public void removeLastElement() {
		list.removeElementAt(count() - 1);
	}

	public boolean equals(TreeNodeList v) {
		String s1, s2;

		if ((count() == 0) && (v.count() == 0))
			return true;

		if (count() != v.count())
			return false;

		for (int i = 0; i < count(); i++) {
			s1 = get(i);
			s2 = v.get(i);

			if (!s1.equals(s2))
				return false;
		}
		return true;
	}

	public File getFile(File programDirectory) {
		File file = programDirectory;

		for (int i = 0; i < count(); i++) {
			file = new File(file, get(i));
		}

		return file;

	}

}
