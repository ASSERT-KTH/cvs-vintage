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
