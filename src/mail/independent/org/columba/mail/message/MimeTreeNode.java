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

import java.util.Vector;

public class MimeTreeNode {
	
	private Vector childs;
	private MimeTreeNode parent;
	
	public MimeTreeNode() {
		childs = new Vector();	
	}
	
	/**
	 * Gets the address.
	 * @return Returns a Integer[]
	 */
	public Integer[] getAddress() {
		Vector result = new Vector();

		if (parent == null)
			result.add(new Integer(0));
		else {
			MimeTreeNode nextParent = parent;
			MimeTreeNode nextChild = this;

			while (nextParent != null) {
				result.insertElementAt(new Integer( nextParent.getNumber(nextChild) ),0);

				nextChild = nextParent;
				nextParent = nextParent.getParent();
			}
		}

		Integer[] returnValue = new Integer[result.size()];
		for( int i=0; i<result.size(); i++ )
			returnValue[i] = (Integer) result.get(i);

		return returnValue;
	}

	/**
	 * Returns the parent.
	 * @return MimeTreeNode
	 */
	public MimeTreeNode getParent() {
		return parent;
	}

	/**
	 * Sets the parent.
	 * @param parent The parent to set
	 */
	public void setParent(MimeTreeNode parent) {
		this.parent = parent;
	}

	public int countChilds() {
		return childs.size();
	}

	public MimeTreeNode getChild(int nr) {
		return (MimeTreeNode) childs.get(nr);
	}

	public void addChild(MimeTreeNode child) {
		if( child == null ) return;
		childs.add(child);
		child.setParent(this);
	}

	public int count() {
		if (countChilds() == 0)
			return 1;

		int result = 0;

		for (int i = 0; i < countChilds(); i++) {
			result += getChild(i).count();
		}

		return result;
	}

	public int getNumber(MimeTreeNode child) {
		return childs.indexOf(child);
	}

	public Vector getChilds() {
		return childs;
	}
}
