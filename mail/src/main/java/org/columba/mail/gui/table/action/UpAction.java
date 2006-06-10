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
package org.columba.mail.gui.table.action;

import java.awt.event.ActionEvent;
import java.util.logging.Logger;

import javax.swing.tree.DefaultMutableTreeNode;

import org.columba.api.gui.frame.IFrameMediator;
import org.columba.core.command.CommandProcessor;
import org.columba.core.gui.action.AbstractColumbaAction;
import org.columba.mail.command.IMailFolderCommandReference;
import org.columba.mail.command.MailFolderCommandReference;
import org.columba.mail.folder.IMailbox;
import org.columba.mail.gui.frame.MailFrameMediator;
import org.columba.mail.gui.frame.TableViewOwner;
import org.columba.mail.gui.message.command.ViewMessageCommand;
import org.columba.mail.gui.table.IMessageNode;
import org.columba.mail.gui.table.ITableController;
import org.columba.mail.gui.table.model.MessageNode;

/**
 * @author waffel
 * 
 * The upAction is the action when you pressing the up key (not on NUM-PAD). If
 * you do so, the previouseMessage up your key is selected and shown in the
 * message-view. If no more message up your key, then nothing changed.
 */
public class UpAction extends AbstractColumbaAction {

	/** JDK 1.4+ logging framework logger, used for logging. */
	private static final Logger LOG = Logger
			.getLogger("org.columba.mail.gui.table.action");

	ITableController tableController;

	IFrameMediator frameController;

	public UpAction(IFrameMediator frameController) {
		super(frameController, "UpAction");
		this.tableController = ((TableViewOwner) frameController)
				.getTableController();
		this.frameController = frameController;
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent arg0) {
		LOG.info("action up performed");

		// getting last selection
		IMailFolderCommandReference r = ((MailFrameMediator) frameController)
				.getTableSelection();
		IMailFolderCommandReference ref = r;
		LOG.info("folderCommandRef: " + ref);

		// getting current uid
		Object[] uids = ref.getUids();
		LOG.info("curr uids: " + uids);

		// at any time i get here uids of length 0. If this is so we should
		// return and do nothing
		if (uids.length == 0) {
			return;
		}

		// getting current node (under the selection)
		DefaultMutableTreeNode currNode = (DefaultMutableTreeNode) tableController
				.getMessageNode(uids[0]);
		LOG.info("currNode: " + currNode);

		// getting prev node
		DefaultMutableTreeNode prevNode = currNode.getPreviousNode();
		LOG.info("prevNode: " + prevNode);

		Object[] prevUids = new Object[1];
		prevUids[0] = ((MessageNode) prevNode).getUid();
		LOG.info("prevUids: " + prevUids);
		ref.setUids(prevUids);

		// check if the node is not null
		IMessageNode[] nodes = new MessageNode[prevUids.length];

		for (int i = 0; i < prevUids.length; i++) {
			nodes[i] = tableController.getMessageNode(prevUids[i]);
		}

		boolean node_ok = true;

		for (int i = 0; i < nodes.length; i++) {
			if (nodes[i] == null) {
				node_ok = false;

				break;
			}
		}

		// if the node is not null
		if (node_ok) {
			// select it
			tableController.setSelected(prevUids);

			// saving the last selection for the current folder
			((IMailbox) ref.getSourceFolder()).setLastSelection(prevUids[0]);

			tableController.makeSelectedRowVisible();

			MailFolderCommandReference refNew = new MailFolderCommandReference(
					ref.getSourceFolder(), prevUids);

			// view the message under the new node
			CommandProcessor.getInstance().addOp(
					new ViewMessageCommand(this.frameController, refNew));
		}
	}
}