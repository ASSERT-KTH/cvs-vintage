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
package org.columba.mail.spam;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import org.columba.core.config.Config;
import org.columba.core.gui.frame.FrameManager;
import org.columba.core.io.CloneStreamMaster;
import org.columba.core.logging.Logging;
import org.columba.mail.folder.IMailbox;
import org.columba.mail.spam.command.CommandHelper;
import org.columba.mail.spam.rules.RuleList;
import org.columba.ristretto.message.Header;
import org.macchiato.DBWrapper;
import org.macchiato.Message;
import org.macchiato.SpamFilter;
import org.macchiato.SpamFilterImpl;
import org.macchiato.db.FrequencyDB;
import org.macchiato.db.MD5SumHelper;
import org.macchiato.db.berkleydb.BerkleyFrequencyDBImpl;
import org.macchiato.log.MacchiatoLogger;
import org.macchiato.maps.ProbabilityMap;

/**
 * Built-in spam filter using the Macchiato library.
 * <p>
 * Note, that its necessary for this filter to train a few hundred messages,
 * before its starting to work. I'm usually starting with around 1000 messages
 * while keeping it up-to-date with messages which are scored wrong.
 * <p>
 * If training mode is enabled, the spam filter automatically adds messages to
 * its frequency database.
 * 
 * @author fdietz
 */
public class MacchiatoPlugin implements ISpamPlugin {

	/** JDK 1.4+ logging framework logger, used for logging. */
	private static final Logger LOG = Logger
			.getLogger("org.columba.core.gui.htmlviewer");

	/**
	 * Delete messages from DB, if DB size > THRESHOLD
	 */
	public final static int THRESHOLD = 200000;

	/**
	 * Delete messages from DB after 7 days, if they don't affect the scoring
	 * process because of low occurences.
	 */
	public final static int AGE = 7;

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
	 * dirty flag for database changes
	 */
	private boolean hasChanged = false;

	/**
	 * is cache already loaded?
	 */
	private boolean alreadyLoaded = false;

	/**
	 * 
	 */
	public MacchiatoPlugin() {
		// create directory <config-folder>/mail/spamdb
		File configDirectory = Config.getInstance().getConfigDirectory();
		File mailDirectory = new File(configDirectory, "mail");
		file = new File(mailDirectory, "spamdb");
		if (!file.exists())
			file.mkdir();
		db = new DBWrapper(new BerkleyFrequencyDBImpl(file));

		filter = new SpamFilterImpl(db);

		// make Columba logger parent of macchiato logger
		MacchiatoLogger.setParentLogger(Logger
				.getLogger("org.columba.mail.spam"));

	}

	/**
	 * Score message. Using a threshold of 90% here. Every message with at least
	 * 90% is spam. This value should be increased in the future.
	 * 
	 * @see org.columba.mail.spam.ISpamPlugin#scoreMessage(org.columba.mail.folder.IMailbox,
	 *      java.lang.Object)
	 */
	public boolean scoreMessage(IMailbox mailbox, Object uid) throws Exception {
		// load database from file
		load();

		// get inputstream of message body
		InputStream istream = CommandHelper.getBodyPart(mailbox, uid);

		// we are using this inpustream multiple times
		// --> istream will be closed by CloneStreamMaster
		CloneStreamMaster master = new CloneStreamMaster(istream);

		// get stream
		istream = master.getClone();

		// apply additional handcrafted rules
		ProbabilityMap map = RuleList.getInstance().getProbabilities(mailbox,
				uid);

		float score = filter.scoreMessage(new Message(istream), map);

		return score >= 0.9f;
	}

	/**
	 * @see org.columba.mail.spam.ISpamPlugin#trainMessageAsSpam(org.columba.mail.folder.IMailbox,
	 *      java.lang.Object)
	 */
	public void trainMessageAsSpam(IMailbox mailbox, Object uid)
			throws Exception {
		// get inputstream of message body
		InputStream istream = CommandHelper.getBodyPart(mailbox, uid);

		// get headers
		Header h = mailbox.getHeaderFields(uid, Message.HEADERFIELDS);

		// put headers in list
		Enumeration e = h.getKeys();
		List list = new ArrayList();

		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			list.add(h.get(key));
		}

		// load database from file
		load();

		try {
			CloneStreamMaster master = new CloneStreamMaster(istream);
			InputStream inputStream = master.getClone();

			byte[] md5sum = MD5SumHelper.createMD5(inputStream);
			// close stream
			inputStream.close();

			// get new inputstream
			inputStream = master.getClone();

			Message message = new Message(inputStream, list, md5sum);
			// check if this message was already learned
			// -> only add if this is not the case
			if (db.MD5SumExists(md5sum)) {
				// message already exists
				// --> correct token data
				filter.correctMessageAsSpam(message);
			} else {
				// new message
				filter.trainMessageAsSpam(message);
			}

			// close stream
			inputStream.close();

			// set dirty flag
			hasChanged = true;
		} catch (IOException e1) {
			LOG.severe(e1.getMessage());
			if (Logging.DEBUG)
				e1.printStackTrace();
		} catch (NoSuchAlgorithmException nsae) {
		} // does not occur

	}

	/**
	 * @see org.columba.mail.spam.ISpamPlugin#trainMessageAsHam(org.columba.mail.folder.IMailbox,
	 *      java.lang.Object)
	 */
	public void trainMessageAsHam(IMailbox mailbox, Object uid)
			throws Exception {
		// get inputstream of message body
		InputStream istream = CommandHelper.getBodyPart(mailbox, uid);

		// get headers
		Header h = mailbox.getHeaderFields(uid, Message.HEADERFIELDS);

		// put headers in list
		Enumeration e = h.getKeys();
		List list = new ArrayList();

		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			list.add(h.get(key));
		}

		// load database from file
		load();

		try {
			CloneStreamMaster master = new CloneStreamMaster(istream);
			InputStream inputStream = master.getClone();

			byte[] md5sum = MD5SumHelper.createMD5(inputStream);
			// close stream
			inputStream.close();

			// get new inputstream
			inputStream = master.getClone();
			Message message = new Message(inputStream, list, md5sum);

			// check if this message was already learned
			if (db.MD5SumExists(md5sum)) {
				// message already exists

				// --> correct token data
				filter.correctMessageAsHam(message);
			} else {
				// new message

				filter.trainMessageAsHam(message);
			}

			// close stream
			inputStream.close();

			// set dirty flag
			hasChanged = true;
		} catch (IOException e1) {
			LOG.severe(e1.getMessage());
			if (Logging.DEBUG)
				e1.printStackTrace();
		} catch (NoSuchAlgorithmException nsae) {
		} // does not occur

	}

	/**
	 * @see org.columba.mail.spam.ISpamPlugin#save()
	 */
	public void save() {
		try {
			// only save if changes exist
			if (alreadyLoaded && hasChanged) {
				// cleanup DB -> remove old tokens
				db.cleanupDB(THRESHOLD);

				// close DB
				db.close();
			}
		} catch (Exception e) {
			if (Logging.DEBUG) {
				e.printStackTrace();
			}
			// TODO (@author fdietz): i18n
			int value = JOptionPane.showConfirmDialog(FrameManager.getInstance()
					.getActiveFrame(),
					"An error occured while saving the spam database.\n"
							+ "Try again?", "Error saving database",
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (value == JOptionPane.YES_OPTION) {
				save();
			}
		}

	}

	/**
	 * @see org.columba.mail.spam.ISpamPlugin#load()
	 */
	public void load() {
		/*
		 * try { // only load if necessary if (!alreadyLoaded && file.exists()) {
		 * FrequencyIO.load(db, file); }
		 * 
		 * alreadyLoaded = true; } catch (IOException e) {
		 * JOptionPane.showMessageDialog(
		 * MainInterface.frameModel.getActiveFrame(), "An error occured while
		 * loading the spam database.\n" + "I will use an empty one.", "Error
		 * loading database", JOptionPane.ERROR_MESSAGE); if
		 * (MainInterface.DEBUG) { e.printStackTrace(); } // fail-case db = new
		 * FrequencyDBImpl();
		 * 
		 * alreadyLoaded = true; }
		 */
	}

}
