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

package org.columba.mail.gui.table.util;

import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;

import org.columba.mail.message.HeaderInterface;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author
 * @version 1.0
 */

public class MessageNode extends DefaultMutableTreeNode {
	
	protected  Object uid;

	protected boolean hasRecentChildren;
	 
	public MessageNode(Object header, Object uid) {
		super(header);

		this.uid = uid;
	}

	public Vector getVector() {
		return children;
	}

	public void setUid(Object uid) {
		this.uid = uid;
	}
	public Object getUid() {
		return uid;
	}

	public HeaderInterface getHeader() {
		return (HeaderInterface) getUserObject();
	}

	public static Object[] toUidArray(Object[] nodes) {
		if (nodes[0] instanceof MessageNode) {
			Object[] newUidList = new Object[nodes.length];
			for (int i = 0; i < nodes.length; i++) {
				newUidList[i] = ((MessageNode) nodes[i]).getUid();
				//System.out.println("node=" + newUidList[i]);
			}
			return newUidList;
		} else
			return nodes;
	}

	/**
	 * Returns the hasRecentChildren.
	 * @return boolean
	 */
	public boolean isHasRecentChildren() {
		return hasRecentChildren;
	}

	/**
	 * Sets the hasRecentChildren.
	 * @param hasRecentChildren The hasRecentChildren to set
	 */
	public void setHasRecentChildren(boolean hasRecentChildren) {
		this.hasRecentChildren = hasRecentChildren;
	}

}
