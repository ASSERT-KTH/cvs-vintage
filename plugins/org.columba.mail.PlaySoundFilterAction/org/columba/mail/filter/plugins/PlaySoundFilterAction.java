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

package org.columba.mail.filter.plugins;

import java.io.File;
import java.net.URL;

import org.columba.core.command.Command;
import org.columba.core.command.DefaultCommandReference;
import org.columba.core.command.WorkerStatusController;
import org.columba.core.gui.frame.AbstractFrameController;
import org.columba.core.util.PlaySound;
import org.columba.mail.command.FolderCommand;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.filter.FilterAction;
import org.columba.mail.filter.plugins.AbstractFilterAction;
import org.columba.mail.folder.MessageFolder;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class PlaySoundFilterAction extends AbstractFilterAction {

	/**
	 * @see org.columba.mail.filter.plugins.AbstractFilterAction#getCommand()
	 */
	public Command getCommand(
		FilterAction filterAction,
		MessageFolder srcFolder,
		Object[] uids)
		throws Exception {

		// just a simple example
		for (int i = 0; i < uids.length; i++) {
			System.out.println("Hello World for message-uid=" + uids[i]);
		}

		// for time consuming tasks you need to create
		// your own Command

		FolderCommandReference[] r =
			{ new FolderCommandReference(srcFolder, uids)};

		PlaySoundCommand c = new PlaySoundCommand(r);

		return c;

	}

	/**
	 * 
	 * @author freddy
	 *
	 * To change this generated comment edit the template variable "typecomment":
	 * Window>Preferences>Java>Templates.
	 * To enable and disable the creation of type comments go to
	 * Window>Preferences>Java>Code Generation.
	 */
	class PlaySoundCommand extends FolderCommand {
		public PlaySoundCommand(DefaultCommandReference[] references) {
			super(references);
		}

		public void execute(WorkerStatusController worker) throws Exception {

			// you need a sound.wav in your program folder
			File soundFile = new File("sound.wav");
			URL url = soundFile.toURL();

			PlaySound.play(url);
		}
	}
}
