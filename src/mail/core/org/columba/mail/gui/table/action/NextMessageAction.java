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

import org.columba.core.action.AbstractColumbaAction;
import org.columba.core.gui.frame.FrameMediator;
import org.columba.core.gui.selection.SelectionChangedEvent;
import org.columba.core.gui.selection.SelectionListener;
import org.columba.core.gui.util.ImageLoader;
import org.columba.core.main.MainInterface;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.gui.frame.MailFrameMediator;
import org.columba.mail.gui.frame.TableViewOwner;
import org.columba.mail.gui.message.command.ViewMessageCommand;
import org.columba.mail.gui.table.TableController;
import org.columba.mail.gui.table.model.MessageNode;
import org.columba.mail.gui.table.selection.TableSelectionChangedEvent;
import org.columba.mail.util.MailResourceLoader;

/**
 * Select next message in message list.
 * <p>
 * Note that this action is also used in the message-frame (frame without
 * folder tree and without message list), which depends on the parent frame
 * for referencing messages. 
 * 
 * @see org.columba.mail.gui.messageframe.MessageFrameController
 * 
 * @author fdietz
 */
public class NextMessageAction extends AbstractColumbaAction implements
		SelectionListener {
	public NextMessageAction(FrameMediator frameMediator) {
		super(frameMediator, MailResourceLoader.getString("menu", "mainframe",
				"menu_view_nextmessage"));

		// tooltip text
		putValue(SHORT_DESCRIPTION, MailResourceLoader.getString("menu",
				"mainframe", "menu_view_nextmessage_tooltip").replaceAll("&",
				""));

		// icons
		putValue(LARGE_ICON, ImageLoader.getSmallImageIcon("next-message.png"));

		// shortcut key
		//putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F, 0));

		// disable toolbar text
		setShowToolBarText(false);

		//setEnabled(false);

		// uncomment to enable action

		/*
		 * ((MailFrameMediator)
		 * frameMediator).registerTableSelectionListener(this);
		 */
	}

	/**
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {
		FolderCommandReference r = ((MailFrameMediator) getFrameMediator())
				.getTableSelection();

		TableController table = ((TableViewOwner) getFrameMediator())
				.getTableController();
		if ( table == null ) return;
		
		if (r == null)
			return;

		MessageNode[] nodes = table.getView().getSelectedNodes();
		if (nodes.length == 0)
			return;

		MessageNode node = nodes[0];
		MessageNode nextNode = (MessageNode) node.getNextNode();
		if (nextNode == null)
			return;

		// necessary for the message-frame only
		r.setUids(new Object[] { nextNode.getUid() });
		((MailFrameMediator) getFrameMediator()).setTableSelection(r);
		MainInterface.processor.addOp(new ViewMessageCommand(
				getFrameMediator(), r));
		
		// select message in message list
		table.setSelected(new Object[] { nextNode.getUid() });
	}

	/**
	 * 
	 * @see org.columba.core.gui.util.SelectionListener#selectionChanged(org.columba.core.gui.util.SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent e) {
		setEnabled(((TableSelectionChangedEvent) e).getUids().length > 0);
	}
}