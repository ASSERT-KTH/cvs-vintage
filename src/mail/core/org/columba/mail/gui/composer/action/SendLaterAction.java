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
package org.columba.mail.gui.composer.action;

import java.awt.event.ActionEvent;

import org.columba.api.gui.frame.IFrameMediator;
import org.columba.core.command.CommandProcessor;
import org.columba.core.gui.action.AbstractColumbaAction;
import org.columba.core.resourceloader.ImageLoader;
import org.columba.mail.command.ComposerCommandReference;
import org.columba.mail.command.MailFolderCommandReference;
import org.columba.mail.config.AccountItem;
import org.columba.mail.config.SpecialFoldersItem;
import org.columba.mail.folder.IMailbox;
import org.columba.mail.folder.command.MarkMessageCommand;
import org.columba.mail.folder.outbox.OutboxFolder;
import org.columba.mail.gui.composer.ComposerController;
import org.columba.mail.gui.composer.ComposerModel;
import org.columba.mail.gui.composer.command.SaveMessageCommand;
import org.columba.mail.gui.tree.FolderTreeModel;
import org.columba.mail.util.MailResourceLoader;

/**
 * @author frd
 * 
 * To change this generated comment go to Window>Preferences>Java>Code
 * Generation>Code and Comments
 */
public class SendLaterAction extends AbstractColumbaAction {
	public SendLaterAction(IFrameMediator FrameController) {
		super(FrameController, MailResourceLoader.getString("menu", "composer",
				"menu_file_sendlater"));

		// tooltip text
		putValue(SHORT_DESCRIPTION, MailResourceLoader.getString("menu",
				"composer", "menu_file_sendlater").replaceAll("&", ""));

		// small icon for menu
		putValue(SMALL_ICON, ImageLoader.getSmallImageIcon("send-later-16.png"));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {
		final ComposerController composerController = (ComposerController) getFrameMediator();

		if (composerController.checkState()) {
			return;
		}

		AccountItem item = ((ComposerModel) composerController.getModel())
				.getAccountItem();
		SpecialFoldersItem folderItem = item.getSpecialFoldersItem();
		OutboxFolder destFolder = (OutboxFolder) FolderTreeModel.getInstance()
				.getFolder(103);

		ComposerCommandReference r =new ComposerCommandReference(composerController, destFolder);

		SaveMessageCommand c = new SaveMessageCommand(r);
		CommandProcessor.getInstance().addOp(c);

		//      -> get source reference of message
		// when replying this is the original sender's message
		// you selected and replied to
		try {
			MailFolderCommandReference ref2 = composerController.getModel()
					.getSourceReference();
			if (ref2 != null && ((IMailbox)ref2.getSourceFolder()).exists(ref2.getUids()[0])) {
				// mark message as answered
				ref2.setMarkVariant(MarkMessageCommand.MARK_AS_ANSWERED);
				MarkMessageCommand c1 = new MarkMessageCommand(ref2);
				CommandProcessor.getInstance().addOp(c1);
			}
		} catch (Exception e) {
		}

		// close composer view
		if (composerController.getView().getFrame() != null) {
			composerController.getView().getFrame().setVisible(false);
		}
	}
}