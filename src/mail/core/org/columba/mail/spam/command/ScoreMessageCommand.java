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
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
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
import org.columba.core.command.WorkerStatusController;
import org.columba.core.gui.frame.FrameMediator;
import org.columba.core.gui.util.ExceptionDialog;
import org.columba.core.io.CloneStreamMaster;
import org.columba.core.main.MainInterface;
import org.columba.mail.command.FolderCommand;
import org.columba.mail.command.FolderCommandAdapter;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.config.AccountItem;
import org.columba.mail.filter.plugins.AddressbookFilter;
import org.columba.mail.folder.MessageFolder;
import org.columba.mail.folder.command.MarkMessageCommand;
import org.columba.mail.spam.SpamController;
import org.columba.mail.spam.rules.RuleList;
import org.columba.ristretto.message.Header;
import org.macchiato.Message;
import org.macchiato.maps.ProbabilityMap;

/**
 * Score selected messages as spam, meaning calculate the likelyhood that the
 * message is spam.
 * 
 * @author fdietz
 */
public class ScoreMessageCommand extends FolderCommand {

	protected FolderCommandAdapter adapter;

	private Object[] uids;

	private MessageFolder srcFolder;

	private WorkerStatusController worker;

	private CloneStreamMaster master;

	private MarkMessageCommand markAsSpamCommand;
	private MarkMessageCommand markAsNotSpamCommand;

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
	public ScoreMessageCommand(FrameMediator frame,
			DefaultCommandReference[] references) {
		super(frame, references);
	}

	public void updateGUI() throws Exception {
		// update table
		markAsSpamCommand.updateGUI();
		markAsNotSpamCommand.updateGUI();
	}

	/**
	 * @see org.columba.core.command.Command#execute(org.columba.core.command.Worker)
	 */
	public void execute(WorkerStatusController worker) throws Exception {
		this.worker = worker;

		//		use wrapper class for easier handling of references array
		adapter = new FolderCommandAdapter(
				(FolderCommandReference[]) getReferences());

		// get array of source references
		FolderCommandReference[] r = adapter.getSourceFolderReferences();

		// for every folder
		for (int i = 0; i < r.length; i++) {
			// get array of message UIDs
			uids = r[i].getUids();

			// get source folder
			srcFolder = (MessageFolder) r[i].getFolder();

			// register for status events
			((StatusObservableImpl) srcFolder.getObservable())
					.setWorker(worker);

			// update status message
			worker.setDisplayText("Scoring messages ...");
			worker.setProgressBarMaximum(uids.length);

			ArrayList spamList = new ArrayList();
			ArrayList nonspamList = new ArrayList();

			for (int j = 0; j < uids.length; j++) {
				try {
					// apply additional handcrafted rules
					ProbabilityMap map = applyAdditionalRules(j);

					// score message
					boolean result = scoreMessage(j, map);

					// if message is spam
					if (result) {
						// mark message as spam
						
						spamList.add(uids[j]);
					} else {
						// mark message as *not* spam
						
						nonspamList.add(uids[j]);
					}

					// train message as spam or non spam
					trainMessage(j, result);

					worker.setProgressBarValue(j);

					if (worker.cancelled()) {
						break;
					}
				} catch (IOException e) {
					new ExceptionDialog(e);

					if (MainInterface.DEBUG) {
						e.printStackTrace();
					}
				} catch (Exception e) {
					new ExceptionDialog(e);

					if (MainInterface.DEBUG) {
						e.printStackTrace();
					}
				}
			}

			// mark spam messages
			if (spamList.size() != 0) {
				FolderCommandReference[] ref = new FolderCommandReference[1];
				ref[0] = new FolderCommandReference(srcFolder, spamList
						.toArray());
				ref[0].setMarkVariant(MarkMessageCommand.MARK_AS_SPAM);
				markAsSpamCommand = new MarkMessageCommand(ref);
				MainInterface.processor.addOp(markAsSpamCommand);
			}

			// mark non spam messages
			if (nonspamList.size() != 0) {
				FolderCommandReference[] ref = new FolderCommandReference[1];
				ref[0] = new FolderCommandReference(srcFolder, nonspamList
						.toArray());
				ref[0].setMarkVariant(MarkMessageCommand.MARK_AS_NOTSPAM);
				markAsNotSpamCommand = new MarkMessageCommand(ref);
				MainInterface.processor.addOp(markAsNotSpamCommand);
			}

		}
	}

	private ProbabilityMap applyAdditionalRules(int j) throws Exception {
		ProbabilityMap map = RuleList.getInstance().getProbabilities(srcFolder,
				uids[j]);

		return map;
	}

	/**
	 * Score message, meaning decide if message is spam or non spam.
	 * 
	 * @param j
	 *            message UID index
	 * @return true, if spam. False, otherwise.
	 * @throws Exception
	 * @throws IOException
	 */
	private boolean scoreMessage(int j, ProbabilityMap map) throws Exception,
			IOException {
		// get inputstream of message body
		InputStream istream = CommandHelper.getBodyPart(srcFolder, uids[j]);

		// we are using this inpustream multiple times
		// --> istream will be closed by CloneStreamMaster
		master = new CloneStreamMaster(istream);

		// get stream
		istream = master.getClone();
		// calculate message score
		boolean result = SpamController.getInstance().scoreMessage(
				istream, map);
		
		// close stream
		istream.close();
		
		// message belongs to which account?
		AccountItem item = CommandHelper
				.retrieveAccountItem(srcFolder, uids[j]);

		// check if sender is in user's addressbook
		boolean checkAddressbook = item.getSpamItem().checkAddressbook();

		boolean isInAddressbook = false;
		if (checkAddressbook) {
			// check if sender is already in addressbook
			isInAddressbook = new AddressbookFilter().process(srcFolder,
					uids[j]);
			
			result = result && !isInAddressbook;
		} 
		
		return result;
	}

	/**
	 * Train selected message as spam or non spam.
	 * 
	 * @param j
	 *            UID index
	 * @param result
	 *            true, if spam. False, otherwise.
	 * @throws Exception
	 */
	private void trainMessage(int j, boolean result) throws Exception {
		// get headers
		Header h = srcFolder.getHeaderFields(uids[j], Message.HEADERFIELDS);

		// put headers in list
		Enumeration enum = h.getKeys();
		List list = new ArrayList();

		while (enum.hasMoreElements()) {
			String key = (String) enum.nextElement();
			list.add(h.get(key));
		}

		// get another inputstream
		InputStream istream = master.getClone();
		
		// add this message to frequency database
		if (result)
			SpamController.getInstance().trainMessageAsSpam(istream,
					list);
		else
			SpamController.getInstance().trainMessageAsHam(istream,
					list);
		// close stream
		istream.close();
	}
}