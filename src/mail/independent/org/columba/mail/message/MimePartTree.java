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

import java.lang.reflect.Array;
import java.util.LinkedList;

public class MimePartTree {
	MimePart rootMimeNode;

	private static final Integer[] ROOT_ADDRESS = { new Integer(0) };

	public MimePartTree() {
	}

	public MimePartTree(MimePart root) {
		rootMimeNode = root;
	}

	public MimePart get(int number) {
		LinkedList leafs = getAllLeafs();

		return (MimePart) leafs.get(number);
	}

	public int count() {
		if (rootMimeNode == null)
			return 0;
		return rootMimeNode.count();
	}

	public void clear() {
		rootMimeNode = null;
	}

	public LinkedList getAllLeafs() {
		return getLeafs(rootMimeNode);
	}

	public MimePart getFromAddress(Integer[] address) {		
		// If Root-Address and Root has no nodes return rootNode
		if(( Array.getLength( address ) == 1 ) && ( address[0].intValue() == 0 ) && (rootMimeNode.countChilds() == 0) )
			return rootMimeNode;
		
		MimePart actPart = rootMimeNode;

		for (int i = 0; i < Array.getLength(address); i++) {
			actPart = (MimePart) actPart.getChild(address[i].intValue());
		}

		return actPart;
	}

	public MimePart getFirstTextPart(String preferedSubtype) {
		MimePart textPart = getFirstLeafWithContentType(rootMimeNode, "text");

		// Have we found anything ?
		if (textPart == null)
			return null;

		// If nothing prefered return found
		if( preferedSubtype == null )
			return textPart;

		// Is it of prefered Subtype?	
		if (textPart.getHeader().contentSubtype.equals(preferedSubtype))
			return textPart;

		// Try to find better TextPart!

		// Check if part of Multipart/Alternative
		MimePart parent = (MimePart) textPart.getParent();

		if (parent != null) {
			if (parent.getHeader().contentSubtype.equals("alternative")) {
				
				
				MimePart nextTextPart;
				LinkedList alternatives =
					getLeafsWithContentType(parent, "text");

				
				
				// We can leave the first one out because we checked earlier
				for (int i = 1; i < alternatives.size(); i++) {
					
					nextTextPart = (MimePart) alternatives.get(i);
					
					
					if (nextTextPart
						.getHeader()
						.contentSubtype
						.equals(preferedSubtype))
						return nextTextPart;
				}

				// Nothing better found -> return first found!
			}
		}

		return textPart;
	}

	public MimePart getFirstLeafWithContentType(
		MimePart root,
		String contentType) {
		MimePart result = null;

		if (root.countChilds() > 0) {

			for (int i = 0; i < root.countChilds(); i++) {
				result =
					getFirstLeafWithContentType(
						(MimePart) root.getChild(i),
						contentType);
				if (result != null)
					return result;
			}

		} else {
			if (root.getHeader().contentType.equals(contentType))
				return root;
		}

		return null;
	}

	public LinkedList getLeafsWithContentType(
		MimePart root,
		String contentType) {


		LinkedList result = new LinkedList();

		if (root.countChilds() > 0) {			
			for (int i = 0; i < root.countChilds(); i++) {
				result.addAll(
					getLeafsWithContentType(
						(MimePart) root.getChild(i),
						contentType));
			}

		} else {
			if (root.getHeader().contentType.equals(contentType))
				result.add(root);
		}

		return result;
	}

	public LinkedList getLeafs(MimeTreeNode root) {
		LinkedList result = new LinkedList();

		if (root.countChilds() > 0) {

			for (int i = 0; i < root.countChilds(); i++) {
				result.addAll(getLeafs((MimeTreeNode) root.getChild(i)));
			}

		} else {
			result.add(root);
		}

		return result;
	}

	/**
	 * Returns the rootMimeNode.
	 * @return MimePart
	 */
	public MimePart getRootMimeNode() {
		return rootMimeNode;
	}

	/**
	 * Sets the rootMimeNode.
	 * @param rootMimeNode The rootMimeNode to set
	 */
	public void setRootMimeNode(MimePart rootMimeNode) {
		this.rootMimeNode = rootMimeNode;
	}

}
