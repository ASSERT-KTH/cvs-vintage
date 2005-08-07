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
package org.columba.chat.ui.roaster;

import java.awt.Dimension;
import java.util.Iterator;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.columba.chat.AlturaComponent;
import org.columba.chat.api.IBuddyStatus;
import org.columba.chat.api.IRoasterTree;
import org.columba.chat.jabber.BuddyList;
import org.columba.chat.jabber.BuddyStatus;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.packet.Presence;

/**
 * @author fdietz
 * 
 */
public class RoasterTree extends JTree implements IRoasterTree {

	private DefaultTreeModel model;

	private Roster roster;

	private DefaultMutableTreeNode root;

	private DefaultMutableTreeNode uncategorizedNode;

	public RoasterTree() {

		root = new DefaultMutableTreeNode("Roster");
		uncategorizedNode = new DefaultMutableTreeNode("Uncategorized");

		model = new DefaultTreeModel(root);

		setModel(model);

		setCellRenderer(new RoasterTreeRenderer());

		setPreferredSize(new Dimension(250, 300));
		setRootVisible(false);
		setShowsRootHandles(true);
	}

	/* (non-Javadoc)
	 * @see org.columba.chat.ui.roaster.IRoasterTree#getSelected()
	 */
	public IBuddyStatus getSelected() {
		TreePath path = getSelectionPath();
		if (path == null)
			return null;
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) path
				.getLastPathComponent();

		if (node != null) {
			IBuddyStatus b = (IBuddyStatus) node.getUserObject();

			return b;
		}

		return null;
	}

	/* (non-Javadoc)
	 * @see org.columba.chat.ui.roaster.IRoasterTree#updateBuddyPresence(org.columba.chat.api.IBuddyStatus)
	 */
	public void updateBuddyPresence(IBuddyStatus buddy) {
		DefaultMutableTreeNode node = findBuddy(root, buddy);
		if (node != null) {

			model.nodeChanged(node);

		}

		updateUI();
	}

	/* (non-Javadoc)
	 * @see org.columba.chat.ui.roaster.IRoasterTree#populate()
	 */
	public void populate() {
		root = new DefaultMutableTreeNode("Roster");

		roster = AlturaComponent.connection.getRoster();

		// add all groups as folder to JTree
		Iterator it = roster.getGroups();
		while (it.hasNext()) {

			RosterGroup group = (RosterGroup) it.next();
			DefaultMutableTreeNode child = new DefaultMutableTreeNode(group);

			root.add(child);
		}

		// add "Uncategorized" note
		root.add(uncategorizedNode);

		// add all contacts as leafs of group folders
		it = roster.getEntries();
		while (it.hasNext()) {
			RosterEntry entry = (RosterEntry) it.next();

			// add to global buddy list
			BuddyStatus buddy;
			if (BuddyList.getInstance().exists(entry.getUser())) {
				// buddy already exists
				buddy = BuddyList.getInstance().getBuddy(entry.getUser());

			} else {
				// create new buddy
				buddy = new BuddyStatus(entry.getUser());
				buddy.setName(entry.getName());
				// and add it to the buddylist
				BuddyList.getInstance().add(entry.getUser(), buddy);
			}

			// get presence
			Presence p = roster.getPresence(entry.getUser());
			if (p != null) {
				// update status information

				buddy.setPresenceMode(p.getMode());
				buddy.setStatusMessage(p.getStatus());
			}

			// check if this buddy belongs to a group
			Iterator groups = entry.getGroups();
			boolean notAdded = true;
			while (groups.hasNext()) {
				RosterGroup group = (RosterGroup) groups.next();

				DefaultMutableTreeNode parent = findGroup(root, group);

				if (parent != null) {
					// found group for buddy
					parent.add(new DefaultMutableTreeNode(buddy));
					notAdded = false;
				}
			}

			// didn't find any group for this buddy
			if (notAdded == true)
				// add to "Uncategorized" node
				uncategorizedNode.add(new DefaultMutableTreeNode(buddy));

		}
		model.setRoot(root);

		model.nodeStructureChanged(root);

	}

	/**
	 * Find group node.
	 * 
	 * @param parent
	 *            parent node
	 * @param group
	 *            group
	 * @return group node
	 */
	private DefaultMutableTreeNode findGroup(DefaultMutableTreeNode parent,
			RosterGroup group) {
		for (int i = 0; i < parent.getChildCount(); i++) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) parent
					.getChildAt(i);

			if (group.equals(child.getUserObject()))
				return child;

		}

		return null;
	}

	private DefaultMutableTreeNode findBuddy(DefaultMutableTreeNode parent,
			IBuddyStatus buddy) {
		for (int i = 0; i < parent.getChildCount(); i++) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) parent
					.getChildAt(i);

			Object o = child.getUserObject();

			if (o instanceof BuddyStatus) {
				if (buddy.getJabberId().equals(((IBuddyStatus) o).getJabberId()))
					return child;
			}
		}

		return null;
	}
}