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

package org.columba.mail.folder;

import javax.swing.tree.TreeNode;


/**
 * Iterate through all children using depth-first search.
 * 
 * @author tstich
 */
public class FolderChildrenIterator {

	private IMailFolder parent;
	private IMailFolder nextChild;
	
	public FolderChildrenIterator(IMailFolder parent) {
		this.parent = parent;
		
		if( parent.getChildCount() > 0 ) {
			nextChild = (IMailFolder) findNext(parent.getChildAt(0));
		}
	}
	
	/**
	 * @param f
	 */
	private TreeNode findNext(TreeNode f) {
		if( f == parent ) return null;
		
		if( f.getChildCount() > 0 ) {
			return f.getChildAt(0);
		} else {
			TreeNode parent = f.getParent();
			int childIndex = parent.getIndex(f);
			
			if( childIndex < parent.getChildCount()-1 ) {
				return parent.getChildAt(childIndex + 1);
			} else {
				return findNext(parent);
			}
		}
	}

	public boolean hasMoreChildren() {
		return nextChild != null;
	}

	public IMailFolder nextChild() {
		IMailFolder result = nextChild;
		nextChild = (IMailFolder) findNext(nextChild);
		
		return result;
	}
	
}
