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
package org.columba.mail.spam;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import org.columba.core.gui.util.NotifyDialog;
import org.columba.core.io.CloneStreamMaster;
import org.columba.core.main.MainInterface;
import org.macchiato.DBWrapper;
import org.macchiato.Message;
import org.macchiato.SpamFilter;
import org.macchiato.SpamFilterImpl;
import org.macchiato.db.FrequencyDB;
import org.macchiato.db.FrequencyDBImpl;
import org.macchiato.db.FrequencyIO;
import org.macchiato.db.MD5SumHelper;

/**
 * High-level wrapper for the spam filter.
 * <p>
 * Class should be used by Columba, to add ham or spam messages
 * to the training database. And to score messages using this
 * training set.
 * <p>
 * Note, that its necessary for this filter to train a few hundred
 * messages, before its starting to work. I'm usually starting with
 * around 1000 messages while keeping it up-to-date with messages 
 * which are scored wrong. 
 * <p>
 * If training mode is enabled, the spam filter automatically adds
 * messages to its frequency database.
 * 
 * @author fdietz
 */
public class SpamController {

	/**
	 * spam filter in macchiator library doing the actual work
	 */
	private SpamFilter filter;

	/**
	 * database of tokens, storing occurences of tokens, etc.
	 */
	private FrequencyDB db;

	/**
	 * file to store the token database
	 */
	private File file;

	/**
	 * singleton pattern instance of this class
	 */
	private static SpamController instance;

	private boolean trainingMode;

	/**
	 * private constructor 
	 *
	 */
	private SpamController() {

		db= new DBWrapper(new FrequencyDBImpl());

		filter= new SpamFilterImpl(db);

		trainingMode= false;

	}

	/**
	 * Get instance of class.
	 * 
	 * @return		spam controller
	 */
	public static SpamController getInstance() {
		if (instance == null) {

			instance= new SpamController();

			File configDirectory= MainInterface.config.getConfigDirectory();

			File mailDirectory= new File(configDirectory, "mail");

			instance.file= new File(mailDirectory, "spam.db");

			// load database from file
			instance.load();

		}

		return instance;
	}

	/**
	 * Add this message to the token database as spam.
	 * 
	 * @param istream		
	 */
	public void trainMessageAsSpam(InputStream istream, List list) {
		try {
			CloneStreamMaster master= new CloneStreamMaster(istream);
			InputStream inputStream= master.getClone();

			byte[] md5sum= MD5SumHelper.createMD5(inputStream);

			if (isTrainingModeEnabled()) {
				// we are in training mode
				// -> even if message was already learned, it can be re-learned again

				filter.trainMessageAsSpam(
					new Message(master.getClone(), list, md5sum));
			} else {
				// we are *not* in training mode
				// -> check if this message was already learned
				// -> only add if this is not the case

				if (db.MD5SumExists(md5sum) == false)
					filter.trainMessageAsSpam(
						new Message(master.getClone(), list, md5sum));
			}
		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	/**
	 * Add this message to the token database as ham.
	 * 
	 * @param istream
	 * @param list
	 */
	public void trainMessageAsHam(InputStream istream, List list) {

		try {
			CloneStreamMaster master= new CloneStreamMaster(istream);
			InputStream inputStream= master.getClone();

			byte[] md5sum= MD5SumHelper.createMD5(inputStream);

			if (isTrainingModeEnabled()) {
				// we are in training mode
				// -> even if message was already learned, it can be re-learned again

				filter.trainMessageAsHam(
					new Message(master.getClone(), list, md5sum));
			} else {
				// we are *not* in training mode
				// -> check if this message was already learned
				// -> only add if this is not the case

				if (db.MD5SumExists(md5sum) == false)
					filter.trainMessageAsHam(
						new Message(master.getClone(), list, md5sum));
			}
		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	/**
	 * Score message.
	 * 
	 * @param istream
	 * @return			probability this message is spam (0.0-1.0 float values)
	 */
	private float score(InputStream istream) {
		return filter.scoreMessage(new Message(istream));
	}

	/**
	 * Score message. Using a threshold of 90% here. Every message
	 * with at least 90% is spam. 
	 * 
	 * @param istream
	 * 
	 * @return		true, if message is spam. False, otherwise.
	 */
	public boolean scoreMessage(InputStream istream) {
		if (score(istream) > 0.9)
			return true;

		return false;
	}

	public void printDebug() {
		((FrequencyDBImpl) db).printDebug();
	}

	/**
	 * Load frequency DB from file.
	 *
	 */
	private void load() {
		try {
			if (file.exists())
				FrequencyIO.load(db, file);
		} catch (Exception e) {
			NotifyDialog d= new NotifyDialog();
			d.showDialog(e);
			if (MainInterface.DEBUG)
				e.printStackTrace();

			// fail-case 
			db= new FrequencyDBImpl();
		}
	}

	/**
	 * Save frequency DB to file.
	 *
	 */
	public void save() {
		try {
			FrequencyIO.save(db, file);
		} catch (Exception e) {
			NotifyDialog d= new NotifyDialog();
			d.showDialog(e);
			if (MainInterface.DEBUG)
				e.printStackTrace();
		}
	}

	/**
	 * Checks if training mode is enabled.
	 * 
	 * @return		true, if enabled. False,otherwise.
	 */
	public boolean isTrainingModeEnabled() {
		return trainingMode;
	}

}
