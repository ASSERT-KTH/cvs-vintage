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

import org.columba.api.gui.frame.IFrameMediator;
import org.columba.api.selection.ISelectionListener;
import org.columba.api.selection.SelectionChangedEvent;
import org.columba.core.command.CommandProcessor;
import org.columba.core.gui.action.AbstractColumbaAction;
import org.columba.mail.command.IMailFolderCommandReference;
import org.columba.mail.gui.frame.MailFrameMediator;
import org.columba.mail.gui.table.selection.TableSelectionChangedEvent;
import org.columba.mail.spam.command.LearnMessageAsSpamCommand;

/**
 * 
 * 
 * @author fdietz
 */
public class TrainMessageAsSpamAction extends AbstractColumbaAction implements
		ISelectionListener {
	/**
	 * @param frameMediator
	 * @param name
	 */
	public TrainMessageAsSpamAction(IFrameMediator frameMediator) {
		super(frameMediator, "Train Message as Spam");

		setEnabled(false);

		((MailFrameMediator) frameMediator)
				.registerTableSelectionListener(this);
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent arg0) {
		IMailFolderCommandReference r = ((MailFrameMediator) getFrameMediator())
				.getTableSelection();
		CommandProcessor.getInstance().addOp(new LearnMessageAsSpamCommand(r));
	}

	/**
	 * Ensures that the action is only enabled when at least one message is
	 * selected in the GUI.
	 * 
	 * @see org.columba.core.gui.util.ISelectionListener#connectionChanged(org.columba.core.gui.util.SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent e) {
		setEnabled(((TableSelectionChangedEvent) e).getUids().length > 0);
	}
}