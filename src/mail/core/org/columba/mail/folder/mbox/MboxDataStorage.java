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

package org.columba.mail.folder.mbox;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.logging.Logger;

import org.columba.core.io.StreamUtils;
import org.columba.mail.folder.AbstractMessageFolder;
import org.columba.mail.folder.IDataStorage;
import org.columba.ristretto.io.FileSource;
import org.columba.ristretto.io.Source;

public class MboxDataStorage implements IDataStorage {

	/** JDK 1.4+ logging framework logger, used for logging. */
	private static final Logger LOG = Logger
			.getLogger("org.columba.mail.folder.mbox");

	private static final String FROMLINE = "From \n";

	private static final byte[] TERMLINE = new byte[] { '\n' };

	protected AbstractMessageFolder folder;

	protected File mboxFile;

	protected File messageFile;

	protected Hashtable messages;

	protected Object largestUid;

	/**
	 * Constructs the MboxDataStorage.
	 * 
	 * @param folder
	 */
	public MboxDataStorage(AbstractMessageFolder folder) {
		this.folder = folder;
		messages = new Hashtable();

		mboxFile = new File(folder.getDirectoryFile(), Integer.toString(folder
				.getUid()));

		messageFile = new File(folder.getDirectoryFile(), ".messages");

		if (!mboxFile.exists()) {
			try {
				mboxFile.createNewFile();
			} catch (IOException e) {
				LOG.severe(e.getLocalizedMessage());
			}
		} else {
			try {
				load();
			} catch (IOException e) {
				LOG.severe(e.getLocalizedMessage());
					
				throw new RuntimeException("Mailbox is corrupted!");
			}
		}

	}

	/**
	 * @see org.columba.mail.folder.IDataStorage#removeMessage(java.lang.Object)
	 */
	public void removeMessage(Object uid) throws Exception {
		MboxMessage message = (MboxMessage) messages.remove(uid);

		if (uid.equals(largestUid)) {
			FileChannel channel = new RandomAccessFile(mboxFile, "rw")
					.getChannel();
			channel
					.truncate(mboxFile.length()
							- (message.getLength() + FROMLINE.length() + TERMLINE.length));
			channel.close();
		} else {

			int intUid = ((Integer) uid).intValue();

			deleteFilePart(mboxFile, message.getStart() - FROMLINE.length(),
					message.getLength() + FROMLINE.length() + TERMLINE.length);

			// update message starts of following messages
			Enumeration uids = messages.keys();
			while (uids.hasMoreElements()) {
				Integer actUid = (Integer) uids.nextElement();

				if (actUid.intValue() > intUid) {
					MboxMessage m = (MboxMessage) messages.get(actUid);
					m
							.setStart(m.getStart()
									- (message.getLength() + FROMLINE.length() + TERMLINE.length));
				}
			}
		}
	}

	/**
	 * @see org.columba.mail.folder.IDataStorage#getMessageSource(java.lang.Object)
	 */
	public Source getMessageSource(Object uid) throws Exception {

		MboxMessage message = (MboxMessage) messages.get(uid);

		FileInputStream in = new FileInputStream(mboxFile);
		in.skip(message.getStart());

		File tempFile = File.createTempFile("mbox_message", ".tmp");
		tempFile.deleteOnExit();

		FileOutputStream out = new FileOutputStream(tempFile);

		StreamUtils.streamCopy(in, out, (int) message.getLength());

		in.close();
		out.close();

		return new FileSource(tempFile);
	}

	/**
	 * @see org.columba.mail.folder.IDataStorage#getMessageStream(java.lang.Object)
	 */
	public InputStream getMessageStream(Object uid) throws Exception {
		MboxMessage message = (MboxMessage) messages.get(uid);

		FileInputStream in = new FileInputStream(mboxFile);
		in.skip(message.getStart());

		File tempFile = File.createTempFile("mbox_message", ".tmp");
		tempFile.deleteOnExit();

		FileOutputStream out = new FileOutputStream(tempFile);

		StreamUtils.streamCopy(in, out, (int) message.getLength());

		in.close();
		out.close();

		return new FileInputStream(tempFile);
	}

	/**
	 * @see org.columba.mail.folder.IDataStorage#saveMessage(java.lang.Object,
	 *         java.io.InputStream)
	 */
	public void saveMessage(Object uid, InputStream source) throws Exception {
		FileOutputStream out = new FileOutputStream(mboxFile, true);
		out.write(FROMLINE.getBytes("US-ASCII"));

		long pos = mboxFile.length();
		long length = StreamUtils.streamCopy(source, out);

		out.write(TERMLINE);
		out.close();

		messages.put(uid, new MboxMessage(uid, pos, length));

		largestUid = uid;
	}

	/**
	 * @see org.columba.mail.folder.IDataStorage#getMessageCount()
	 */
	public int getMessageCount() {
		return messages.size();
	}

	/**
	 * @see org.columba.mail.folder.IDataStorage#exists(java.lang.Object)
	 */
	public boolean exists(Object uid) throws Exception {
		return messages.containsKey(uid);
	}

	/**
	 * @see org.columba.mail.folder.IDataStorage#getMessageUids()
	 */
	public Object[] getMessageUids() {
		return messages.keySet().toArray();
	}

	/**
	 * 
	 * 
	 * @param file
	 * @param startpos
	 * @param length
	 * @throws IOException
	 */
	protected void deleteFilePart(File file, long startpos, long length)
			throws IOException {
		RandomAccessFile ramFile = new RandomAccessFile(file, "rw");
		long oldlength = file.length();

		FileChannel channel1 = ramFile.getChannel();
		FileChannel channel2 = new FileInputStream(file).getChannel();

		channel2.position(startpos + length);
		channel1.transferFrom(channel2, startpos, oldlength
				- (length + startpos));
		channel2.close();

		channel1.truncate(oldlength - length);
		channel1.close();
	}

	protected void load() throws IOException {
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(
				messageFile));
		MboxMessage message;

		int size = in.readInt();

		messages = new Hashtable(size);
		for (int i = 0; i < size; i++) {
			message = new MboxMessage(new Integer(in.readInt()), in.readLong(),
					in.readLong());
			messages.put(message.getUid(), message);
		}

		in.close();
	}

	public void save() throws IOException {
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(
				messageFile, false));
		MboxMessage message;

		int size = messages.size();
		out.writeInt(size);

		Enumeration message_enum = messages.elements();

		for (int i = 0; i < size; i++) {
			message = (MboxMessage) message_enum.nextElement();

			out.writeInt(((Integer) message.getUid()).intValue());
			out.writeLong(message.getStart());
			out.writeLong(message.getLength());
		}

		out.close();
	}


}