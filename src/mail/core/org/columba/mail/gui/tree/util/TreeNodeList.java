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

package org.columba.mail.gui.tree.util;

import java.util.Vector;
import java.util.StringTokenizer;
import javax.swing.tree.TreePath;
import java.io.File;

import org.columba.mail.config.*;

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
