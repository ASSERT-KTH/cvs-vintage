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
package org.columba.mail.gui.composer;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.columba.mail.gui.composer.util.SubjectDialog;
import org.columba.mail.util.MailResourceLoader;

/**
 * @author frd
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class SubjectController implements DocumentListener {

	SubjectView view;
	ComposerController controller;

	public SubjectController(ComposerController controller) {
		this.controller = controller;

		view = new SubjectView(this);
	}

	public void installListener() {
		view.installListener(this);
	}

	public void updateComponents(boolean b) {
		if (b == true) {
			view.setText(((ComposerModel)controller.getModel()).getHeaderField("Subject"));
		} else {
			((ComposerModel)controller.getModel()).setHeaderField("Subject", view.getText());
		}
	}

	public boolean checkState() {
		String subject = ((ComposerModel)controller.getModel()).getHeaderField("Subject");

		if (subject.length() == 0) {
			subject = new String(MailResourceLoader.getString("dialog","composer","composer_no_subject")); //$NON-NLS-1$
			//SubjectDialog dialog = new SubjectDialog(composerInterface.composerFrame);
			SubjectDialog dialog = new SubjectDialog();
			dialog.showDialog(subject);
			if (dialog.success() == true)
				subject = dialog.getSubject();

			((ComposerModel)controller.getModel()).setHeaderField("Subject", subject);
		}
		
		return true;
	}

	/**************** DocumentListener implementation ***************/

	public void insertUpdate(DocumentEvent e) {

	}
	public void removeUpdate(DocumentEvent e) {

	}
	public void changedUpdate(DocumentEvent e) {

	}

}
