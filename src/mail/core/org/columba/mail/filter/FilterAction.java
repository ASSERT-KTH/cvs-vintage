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

package org.columba.mail.filter;

import org.columba.mail.config.*;
import org.columba.mail.message.*;
import org.columba.mail.folder.*;
import org.columba.core.config.*;
import org.columba.main.*;

import javax.swing.*;
import java.util.Vector;
import org.w3c.dom.*;

public class FilterAction extends DefaultItem {
	// move = 0, copy = 1, delete = 2, markasread = 3, ...
	//private String action;
	private AdapterNode actionNode;
	private AdapterNode uidNode;

	private AdapterNode node;
	private AdapterNode treeNode;
	//private int uid;

	public FilterAction(AdapterNode node, Document doc) {
		super(doc);

		this.node = node;
		//list = new TreeNodeList();
		if (node != null)
			parseNode();

	}

	public AdapterNode getRootNode() {
		return node;
	}

	protected void parseNode() {
		AdapterNode child;
		//System.out.println("parsing actionnode: ");

		child = node.getChild("name");
		actionNode = child;

		String action = getTextValue(actionNode);

		if ((action.equals("move")) || (action.equals("copy"))) {
			AdapterNode subChild = node.getChild("uid");
			uidNode = subChild;

		}
	}

	public int getUid() {
		uidNode = node.getChild("uid");

		if (uidNode == null) {
			addUidNode();
		}

		Integer value = new Integer(getTextValue(uidNode));

		int uid = value.intValue();

		return uid;
	}

	public void removeUidNode() {
		AdapterNode parent = node;

		AdapterNode treePathNode = parent.getChild("uid");

		treePathNode.remove();
	}

	public void addUidNode() {
		AdapterNode parent = node;

		org.w3c.dom.Element treePathNode = createTextElementNode("uid", "101");

		parent.domNode.appendChild(treePathNode);

		uidNode = node.getChild("uid");
	}

	public void setUid(int i) {
		Integer value = new Integer(i);

		setTextValue(uidNode, value.toString());
	}

	public String getAction() {
		return getTextValue(actionNode);
	}

	public void setAction(String s) {
		setTextValue(actionNode, s);
	}

	public int getActionInt() {
		if (getAction().equals("move"))
			return 0;
		if (getAction().equals("copy"))
			return 1;
		if (getAction().equals("markasread"))
			return 2;
		if (getAction().equals("delete"))
			return 3;
		return -1;
	}

}
