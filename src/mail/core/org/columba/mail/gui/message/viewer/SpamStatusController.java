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
package org.columba.mail.gui.message.viewer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;

import org.columba.core.main.MainInterface;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.MessageFolder;
import org.columba.mail.folder.command.MarkMessageCommand;
import org.columba.mail.gui.frame.MailFrameMediator;
import org.columba.mail.spam.command.LearnMessageAsHamCommand;

/**
 * Viewer displaying spam status information.
 * 
 * @author fdietz
 *  
 */
public class SpamStatusController implements Viewer, ActionListener {

	private SpamStatusView label;

	private boolean visible;

	private MailFrameMediator mediator;

	public SpamStatusController(MailFrameMediator mediator) {
		super();

		this.mediator = mediator;

		label = new SpamStatusView();
		label.addActionListener(this);

		visible = false;
	}

	/**
	 * @see org.columba.mail.gui.message.status.Status#show(org.columba.mail.folder.Folder,
	 *      java.lang.Object)
	 */
	public void view(MessageFolder folder, Object uid,
			MailFrameMediator mediator) throws Exception {
		Boolean spam = (Boolean) folder.getAttribute(uid, "columba.spam");

		visible = spam.booleanValue();

	}

	/**
	 * @see org.columba.mail.gui.message.viewer.Viewer#getView()
	 */
	public JComponent getView() {
		return label;
	}

	/**
	 * @see org.columba.mail.gui.message.viewer.Viewer#isVisible()
	 */
	public boolean isVisible() {
		// only show view if message is marked as spam
		return visible;
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent arg0) {
		// get selected message
		FolderCommandReference[] r = mediator.getTableSelection();

		// learn message as non spam
		MainInterface.processor.addOp(new LearnMessageAsHamCommand(r));

		// mark as not spam
		r[0].setMarkVariant(MarkMessageCommand.MARK_AS_NOTSPAM);
		MarkMessageCommand c = new MarkMessageCommand(r);
		MainInterface.processor.addOp(c);
	}

	/**
	 * @see org.columba.mail.gui.message.viewer.Viewer#updateGUI()
	 */
	public void updateGUI() throws Exception {
		label.setSpam(visible);

	}
}