package org.columba.mail.filter.plugins;

import java.io.File;
import java.net.URL;

import org.columba.core.command.Command;
import org.columba.core.command.DefaultCommandReference;
import org.columba.core.command.Worker;
import org.columba.core.gui.FrameController;
import org.columba.core.util.PlaySound;
import org.columba.mail.command.FolderCommand;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.filter.FilterAction;
import org.columba.mail.filter.plugins.AbstractFilterAction;
import org.columba.mail.folder.Folder;

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
		Folder srcFolder,
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

		public void execute(Worker worker) throws Exception {

			// you need a sound.wav in your program folder
			File soundFile = new File("sound.wav");
			URL url = soundFile.toURL();

			PlaySound.play(url);

		}
	}

}
