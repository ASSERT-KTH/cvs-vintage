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

package org.columba.mail.gui.composer.command;

import java.io.InputStream;

import org.columba.core.command.DefaultCommandReference;
import org.columba.core.command.WorkerStatusController;
import org.columba.core.main.MainInterface;
import org.columba.mail.command.FolderCommand;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.composer.MessageBuilderHelper;
import org.columba.mail.folder.MessageFolder;
import org.columba.mail.gui.composer.ComposerController;
import org.columba.mail.gui.composer.ComposerModel;
import org.columba.ristretto.message.BasicHeader;
import org.columba.ristretto.message.Header;
import org.columba.ristretto.message.InputStreamMimePart;
import org.columba.ristretto.message.MimeHeader;
import org.columba.ristretto.message.MimeType;

/**
 * Forward message as attachment.
 * 
 * @author fdietz, tstich, karlpeder
 */
public class ForwardCommand extends FolderCommand {

	protected ComposerController controller;

	protected ComposerModel model;

	/**
	 * Constructor for ForwardCommand.
	 * 
	 * @param frameMediator
	 * @param references
	 */
	public ForwardCommand(DefaultCommandReference reference) {
		super(reference);
	}

	public void updateGUI() throws Exception {
		// open composer frame
		controller = (ComposerController) MainInterface.frameModel
				.openView("Composer");

		// apply model
		controller.setComposerModel(model);

		// model->view update
		controller.updateComponents(true);
	}

	public void execute(WorkerStatusController worker) throws Exception {
		// get selected folder
		MessageFolder folder = (MessageFolder) ((FolderCommandReference) getReference())
				.getFolder();

		// get first selected message
		Object[] uids = ((FolderCommandReference) getReference()).getUids();

		// get headerfields
		Header header = folder.getHeaderFields(uids[0],
				new String[] { "Subject" });

		// create composer model
		model = new ComposerModel();

		//      ->set source reference in composermodel
		// when replying this is the original sender's message
		// you selected and replied to
		FolderCommandReference ref = new FolderCommandReference(folder, uids);
		model.setSourceReference(ref);

		// set subject
		model.setSubject(MessageBuilderHelper
				.createForwardSubject(new BasicHeader(header).getSubject()));

		// initialize MimeHeader as RFC822-compliant-message
		MimeHeader mimeHeader = new MimeHeader();
		mimeHeader.setMimeType(new MimeType("message", "rfc822"));

		// add mimepart to model

		InputStream messageSourceStream = folder
				.getMessageSourceStream(uids[0]);
		model.addMimePart(new InputStreamMimePart(mimeHeader,
				messageSourceStream));
	}

	/**
	 * Get composer model.
	 * <p>
	 * Needed for testcases.
	 * 
	 * @return Returns the model.
	 */
	public ComposerModel getModel() {
		return model;
	}
}
