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
package org.columba.mail.spam.command;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.columba.core.command.DefaultCommandReference;
import org.columba.core.command.StatusObservableImpl;
import org.columba.core.command.Worker;
import org.columba.core.gui.frame.FrameMediator;
import org.columba.core.gui.util.ExceptionDialog;
import org.columba.core.io.CloneStreamMaster;
import org.columba.core.main.MainInterface;
import org.columba.mail.command.FolderCommand;
import org.columba.mail.command.FolderCommandAdapter;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.Folder;
import org.columba.mail.folder.command.MarkMessageCommand;
import org.columba.mail.gui.frame.TableUpdater;
import org.columba.mail.gui.table.model.TableModelChangedEvent;
import org.columba.mail.main.MailInterface;
import org.columba.mail.spam.SpamController;
import org.columba.ristretto.message.Header;
import org.macchiato.Message;

/**
 * 
 *
 * @author fdietz
 */
public class ScoreMessageCommand extends FolderCommand {

	protected FolderCommandAdapter adapter;

	/**
	 * @param references
	 */
	public ScoreMessageCommand(DefaultCommandReference[] references) {
		super(references);

	}

	/**
	 * @param frame
	 * @param references
	 */
	public ScoreMessageCommand(
		FrameMediator frame,
		DefaultCommandReference[] references) {
		super(frame, references);

	}

	public void updateGUI() throws Exception {
		// get source references
		FolderCommandReference[] r= adapter.getSourceFolderReferences();

		// for every source references
		TableModelChangedEvent ev;

		for (int i= 0; i < r.length; i++) {
			// update table
			ev=
				new TableModelChangedEvent(
					TableModelChangedEvent.UPDATE,
					r[i].getFolder());
			TableUpdater.tableChanged(ev);

			// update treemodel
			MailInterface.treeModel.nodeChanged(r[i].getFolder());
		}

		// get update reference
		// -> only available if VirtualFolder is involved in operation
		FolderCommandReference u= adapter.getUpdateReferences();

		if (u != null) {
			ev=
				new TableModelChangedEvent(
					TableModelChangedEvent.UPDATE,
					u.getFolder());

			TableUpdater.tableChanged(ev);
			MailInterface.treeModel.nodeChanged(u.getFolder());
		}
	}

	/**
	 * @see org.columba.core.command.Command#execute(org.columba.core.command.Worker)
	 */
	public void execute(Worker worker) throws Exception {
		//		use wrapper class for easier handling of references array
		adapter=
			new FolderCommandAdapter(
				(FolderCommandReference[]) getReferences());

		// get array of source references
		FolderCommandReference[] r= adapter.getSourceFolderReferences();

		// for every folder
		for (int i= 0; i < r.length; i++) {
			// get array of message UIDs
			Object[] uids= r[i].getUids();

			// get source folder
			Folder srcFolder= (Folder) r[i].getFolder();

			// register for status events
			((StatusObservableImpl) srcFolder.getObservable()).setWorker(
				worker);

			//			update status message
			worker.setDisplayText("Scoring messages ...");
			worker.setProgressBarMaximum(uids.length);

			for (int j= 0; j < uids.length; j++) {

				try {
					InputStream istream=
						CommandHelper.getBodyPart(srcFolder, uids[j]);

					CloneStreamMaster master= new CloneStreamMaster(istream);

					boolean result=
						SpamController.getInstance().scoreMessage(
							master.getClone());

					if (result) {
						// mark message as spam
						srcFolder.markMessage(
							new Object[] { uids[j] },
							MarkMessageCommand.MARK_AS_SPAM);

						// if training mode is enabled
						if (SpamController.getInstance().isTrainingModeEnabled()) {

							Header h=
								srcFolder.getHeaderFields(
									uids[j],
									Message.HEADERFIELDS);
							Enumeration enum= h.getKeys();
							List list= new ArrayList();
							while (enum.hasMoreElements()) {
								String key= (String) enum.nextElement();
								list.add(h.get(key));
							}

							// add this message to frequency database
							SpamController.getInstance().trainMessageAsSpam(
								master.getClone(),
								list);
						}
					} else {
					}

					worker.setProgressBarValue(j);

					if (worker.cancelled())
						break;
				} catch (IOException e) {
					new ExceptionDialog(e);
					
					if (MainInterface.DEBUG)
						e.printStackTrace();
				} catch (Exception e) {
					new ExceptionDialog(e);
					
					if (MainInterface.DEBUG)
						e.printStackTrace();
				}
			}

		}

	}

}
