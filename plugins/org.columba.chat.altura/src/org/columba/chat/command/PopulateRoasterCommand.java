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
package org.columba.chat.command;

import java.util.Iterator;

import javax.swing.tree.DefaultMutableTreeNode;

import org.columba.api.command.ICommandReference;
import org.columba.api.command.IWorkerStatusController;
import org.columba.chat.Connection;
import org.columba.chat.model.BuddyList;
import org.columba.chat.model.BuddyStatus;
import org.columba.chat.ui.frame.api.IChatFrameMediator;
import org.columba.core.command.Command;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.packet.Presence;

public class PopulateRoasterCommand extends Command {

	private IChatFrameMediator mediator;

	private DefaultMutableTreeNode root;

	private DefaultMutableTreeNode uncategorizedNode;

	public PopulateRoasterCommand(IChatFrameMediator mediator,
			ICommandReference reference) {
		super(reference);

		this.mediator = mediator;
	}

	/**
	 * @see org.columba.core.command.Command#updateGUI()
	 */
	@Override
	public void updateGUI() throws Exception {
		mediator.getRoasterTree().populate(root);
	}

	@Override
	public void execute(IWorkerStatusController worker) throws Exception {
		root = new DefaultMutableTreeNode("Roster");
		uncategorizedNode = new DefaultMutableTreeNode("Uncategorized");

		Roster roster = Connection.XMPPConnection.getRoster();

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

	}

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

}
