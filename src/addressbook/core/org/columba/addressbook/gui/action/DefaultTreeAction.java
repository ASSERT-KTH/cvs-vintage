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
package org.columba.addressbook.gui.action;

import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import org.columba.addressbook.folder.AddressbookFolder;
import org.columba.addressbook.gui.frame.AddressbookFrameMediator;
import org.columba.addressbook.gui.tree.AddressbookTreeNode;
import org.columba.core.action.AbstractColumbaAction;
import org.columba.core.gui.frame.FrameMediator;

/**
 * Action which is listening for tree selection changes.
 * 
 * @author fdietz
 */
public abstract class DefaultTreeAction extends AbstractColumbaAction implements
		TreeSelectionListener {
	private AddressbookTreeNode treeNode;

	/**
	 * @param frameMediator
	 * @param name
	 */
	public DefaultTreeAction(FrameMediator frameMediator, String name) {
		super(frameMediator, name);

		// register interest on tree selection changes
		((AddressbookFrameMediator) frameMediator)
				.addTreeSelectionListener(this);
	}

	/**
	 * Enable or disable action on tree selection changes.
	 * <p>
	 * Actions should overwrite this method if they need more fine-grained
	 * control.
	 *  
	 */
	public void valueChanged(TreeSelectionEvent e) {
		TreePath path = e.getNewLeadSelectionPath();

		// remember last selected folder treenode
		if (path != null) {
			treeNode = (AddressbookTreeNode) path.getLastPathComponent();
		}

		// enable, if more than zero treenodes selected
		if ( (path != null) && ( treeNode instanceof AddressbookFolder ) ){
			setEnabled(true);
		} else {
			setEnabled(false);
		}
	}

	/**
	 * @return Returns the treeNode.
	 */
	public AddressbookTreeNode getTreeNode() {
		return treeNode;
	}
}