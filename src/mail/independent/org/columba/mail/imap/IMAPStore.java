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

package org.columba.mail.imap;

import java.io.IOException;
import java.net.SocketException;
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.columba.core.command.StatusObservable;
import org.columba.core.logging.ColumbaLogger;
import org.columba.mail.config.ImapItem;
import org.columba.mail.filter.FilterRule;
import org.columba.mail.folder.command.MarkMessageCommand;
import org.columba.mail.folder.headercache.CachedHeaderfieldOwner;
import org.columba.mail.folder.imap.IMAPRootFolder;
import org.columba.mail.gui.util.PasswordDialog;
import org.columba.mail.message.ColumbaHeader;
import org.columba.mail.message.HeaderList;
import org.columba.mail.util.MailResourceLoader;
import org.columba.ristretto.imap.IMAPResponse;
import org.columba.ristretto.imap.parser.FlagsParser;
import org.columba.ristretto.imap.parser.ImapHeaderParser;
import org.columba.ristretto.imap.parser.ListInfo;
import org.columba.ristretto.imap.parser.MessageFolderInfoParser;
import org.columba.ristretto.imap.parser.MessageSet;
import org.columba.ristretto.imap.parser.MessageSourceParser;
import org.columba.ristretto.imap.parser.MimePartParser;
import org.columba.ristretto.imap.parser.MimeTreeParser;
import org.columba.ristretto.imap.parser.SearchResultParser;
import org.columba.ristretto.imap.parser.UIDParser;
import org.columba.ristretto.imap.protocol.Arguments;
import org.columba.ristretto.imap.protocol.BadCommandException;
import org.columba.ristretto.imap.protocol.CommandFailedException;
import org.columba.ristretto.imap.protocol.DisconnectedException;
import org.columba.ristretto.imap.protocol.IMAPException;
import org.columba.ristretto.imap.protocol.IMAPProtocol;
import org.columba.ristretto.message.Flags;
import org.columba.ristretto.message.LocalMimePart;
import org.columba.ristretto.message.MessageFolderInfo;
import org.columba.ristretto.message.MimePart;
import org.columba.ristretto.message.MimeTree;
import org.columba.ristretto.message.io.CharSequenceSource;
import org.columba.ristretto.parser.HeaderParser;
import org.columba.ristretto.parser.ParserException;
import org.columba.ristretto.progress.ProgressObserver;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class IMAPStore {

	public static final int STATE_NONAUTHENTICATE = 0;
	public static final int STATE_AUTHENTICATE = 1;
	public static final int STATE_SELECTED = 2;

	private List capabilities;
	private int state = STATE_NONAUTHENTICATE;
	// non-authenticate=0, authenticate=1, selected=2,
	private String selectedFolderPath;
	private String delimiter;

	private IMAPProtocol imap;

	private ImapItem item;

	private IMAPRootFolder parent;

	private MimeTree aktMimePartTree;
	private String aktMessageUid;

	private MessageFolderInfo messageFolderInfo;

	public IMAPStore(ImapItem item, IMAPRootFolder root) {

		this.item = item;
		this.parent = root;

		imap =
			new IMAPProtocol(
				item.get("host"),
				item.getInteger("port"),
				item.getBoolean("enable_ssl", true));

		imap.registerInterest((ProgressObserver) root.getObservable());
		state = 0;
	}

	public StatusObservable getObservable() {
		return parent.getObservable();
	}

	protected void printStatusMessage(String message) {
		getObservable().setMessage(item.get("host") + ": " + message);
	}

	public String getDelimiter() {
		if (delimiter == null) {
			try {
				delimiter = fetchDelimiter();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return delimiter;
	}

	public String getSelectedFolderPath() {
		return selectedFolderPath;
	}

	public void setSelectedFolderPath(String s) {
		selectedFolderPath = s;
	}

	public void logout() throws Exception {
		if (state != STATE_NONAUTHENTICATE)
			getProtocol().logout();
	}

	public int getState() {
		return state;
	}

	public void login() throws Exception {

		/*
		if (worker != null) {
			worker.setText(" Login " + item.getHost());
			worker.startTimer();
		}
		*/

		PasswordDialog dialog = null;

		boolean answer = false;
		boolean cancel = false;
		boolean first = true;

		boolean openport = false;
		int portNumber = -1;

		try {
			portNumber = item.getInteger("port");
		} catch (NumberFormatException e) {
			// fall back to default IMAP port
			portNumber = 143;
		}

		try {
			printStatusMessage(
				MailResourceLoader.getString(
					"statusbar",
					"message",
					"authenticating"));

			openport = getProtocol().openPort();

		} catch (Exception e) {
			if (e instanceof SocketException)
				throw new IMAPException(e.getMessage());

			e.printStackTrace();
		}

		if (openport) {
			while (!cancel) {
				if (first) {

					if (item.get("password").length() != 0) {
						getProtocol().login(
							item.get("user"),
							item.get("password"));

						state = STATE_AUTHENTICATE;
						answer = true;
						break;
					}

					first = false;
				}

				dialog = new PasswordDialog();
				dialog.showDialog(
					item.get("host") + "@" + item.get("user"),
					item.get("password"),
					item.getBoolean("save_password"));

				char[] name;

				if (dialog.success()) {
					// ok pressed
					name = dialog.getPassword();
					String password = new String(name);
					//String user = dialog.getUser();
					boolean save = dialog.getSave();

					getProtocol().login(item.get("user"), password);

					answer = true;

					state = STATE_AUTHENTICATE;

					if (answer) {
						cancel = true;

						//item.setUser(user);

						state = STATE_AUTHENTICATE;

						item.set("save_password", save);

						if (save)
							item.set("password", password);

					} else
						cancel = false;
				} else {
					cancel = true;
					answer = false;
					// cancel pressed
				}
			}

		} else {
			answer = false;
		}

		//System.out.println("login successful");
	}

	public boolean isSelected(String path) throws Exception {
		//System.out.println("isSelected");

		if (getState() == STATE_SELECTED) {

			if (path.equals(getSelectedFolderPath()))
				return true;
			else {
				select(path);
				return false;
			}
			/*
			if (getSelectedFolderPath().equals(path))
				return true;
			else {
				select(worker, path);
				return false;
			}
			*/
		} else if (getState() == STATE_AUTHENTICATE) {
			select(path);
			return false;
		} else {
			// we are in Imap4.STATE_NONAUTHENTICATE

			/*
			getImapRootFolder().lsub(worker);
			*/

			//isLogin(worker);

			select(path);

			return false;
		}
	}

	private void ensureState(int state) throws Exception {
		if (state == state)
			return;

		login();
	}

	public boolean select(String path) throws Exception {
		ensureState(STATE_AUTHENTICATE);

		ColumbaLogger.log.info("selecting path=" + path);

		if (getSelectedFolderPath() != null) {
			// if another folder is selected, close this one first
			// -> this is necessary, because the close command
			// -> also deletes are messages that are marked as expunged
			try {
				getProtocol().close();
			} catch (CommandFailedException ex) {
			} catch (DisconnectedException ex) {
				state = STATE_NONAUTHENTICATE;
				selectedFolderPath=null;
				select(path);

			}
		}

		try {

			printStatusMessage(
				MessageFormat.format(
					MailResourceLoader.getString(
						"statusbar",
						"message",
						"select_path"),
					new Object[] { path }));
			IMAPResponse[] responses = getProtocol().select(path);

			StringBuffer buf = new StringBuffer();
			for (int i = 0; i < responses.length; i++) {
				buf.append(responses[i].getSource() + "\n");
			}

			messageFolderInfo =
				MessageFolderInfoParser.parseMessageFolderInfo(buf.toString());

			ColumbaLogger.log.info("exists:" + messageFolderInfo.getExists());

			state = STATE_SELECTED;
			selectedFolderPath = path;
		} catch (BadCommandException ex) {
			state = STATE_AUTHENTICATE;
			selectedFolderPath = null;
			JOptionPane.showMessageDialog(
				null,
				"Error while selecting mailbox: " + path);
		} catch (CommandFailedException ex) {
			JOptionPane.showMessageDialog(
				null,
				"Error while selecting mailbox: " + path + ex.getMessage());

			state = STATE_AUTHENTICATE;
			selectedFolderPath = null;
		} catch (DisconnectedException ex) {
			state = STATE_NONAUTHENTICATE;
			selectedFolderPath = null;
			select(path);

		}
		return true;
	}

	/**
	 * Returns the IMAPProtocol
	 * @return IMAPProtocol
	 */
	protected IMAPProtocol getProtocol() {
		return imap;
	}

	public List convertIndexToUid(List v) throws Exception {
		if (v.size() == 0)
			return v;

		List result = new Vector();
		String messageSet = new MessageSet(v.toArray()).getString();

		try {

			IMAPResponse[] responses =
				imap.fetchUIDList(messageSet, messageFolderInfo.getExists());

			result = UIDParser.parse(responses);

			return v;
		} catch (BadCommandException ex) {
		} catch (CommandFailedException ex) {
		}
		return result;
	}

	/**************************** authenticate state ************************/

	protected String fetchDelimiter() throws Exception {
		isLogin();

		try {
			printStatusMessage(
				MailResourceLoader.getString(
					"statusbar",
					"message",
					"fetch_folder_list"));
			IMAPResponse[] responses = getProtocol().list("", "");

			ListInfo listInfo = new ListInfo();
			listInfo.parse(responses[0]);

			return listInfo.getDelimiter();
		} catch (BadCommandException ex) {
		} catch (CommandFailedException ex) {
		} catch (DisconnectedException ex) {
			state = STATE_NONAUTHENTICATE;
			return fetchDelimiter();
		}

		return null;
	}

	public ListInfo[] lsub(String reference, String pattern) throws Exception {

		isLogin();

		try {
			printStatusMessage(
				MailResourceLoader.getString(
					"statusbar",
					"message",
					"fetch_folder_list"));
			IMAPResponse[] responses = getProtocol().lsub(reference, pattern);

			List v = new Vector();
			ListInfo[] list = null;
			for (int i = 0; i < responses.length - 1; i++) {
				if (responses[i] == null) {
					continue;
				}

				ListInfo listInfo = new ListInfo();
				listInfo.parse(responses[i]);
				v.add(listInfo);
			}

			if (v.size() > 0) {
				list = new ListInfo[v.size()];
				((Vector) v).copyInto(list);

				return list;
			}

		} catch (BadCommandException ex) {
		} catch (CommandFailedException ex) {
		} catch (DisconnectedException ex) {
			state = STATE_NONAUTHENTICATE;
			lsub(reference, pattern);
		}

		return null;
	}

	public void append(String mailboxName, String messageSource)
		throws Exception {

		isLogin();

		try {
			getProtocol().append(mailboxName, messageSource);
		} catch (BadCommandException ex) {
		} catch (CommandFailedException ex) {
		} catch (DisconnectedException ex) {
			state = STATE_NONAUTHENTICATE;
			append(mailboxName, messageSource);
		}
	}

	public boolean createFolder(String mailboxName) throws Exception {
		try {
			getProtocol().create(mailboxName);
			getProtocol().subscribe(mailboxName);
		} catch (BadCommandException ex) {
		} catch (CommandFailedException ex) {
			JOptionPane.showMessageDialog(
				null,
				"Error while creating mailbox: "
					+ mailboxName
					+ ex.getMessage());
			return false;
		} catch (DisconnectedException ex) {
			state = STATE_NONAUTHENTICATE;
			createFolder(mailboxName);
		}
		return true;
	}

	public boolean deleteFolder(String mailboxName) throws Exception {
		// we need to ensure that this folder is closed
		try {
			getProtocol().close();
		} catch (BadCommandException ex) {
		} catch (CommandFailedException ex) {
		} catch (DisconnectedException ex) {
			state = STATE_NONAUTHENTICATE;
			deleteFolder(mailboxName);
		}

		try {
			getProtocol().delete(mailboxName);
		} catch (BadCommandException ex) {
		} catch (CommandFailedException ex) {
			return false;
		} catch (DisconnectedException ex) {
			state = STATE_NONAUTHENTICATE;
			deleteFolder(mailboxName);
		}

		try {
			getProtocol().unsubscribe(mailboxName);
		} catch (BadCommandException ex) {
		} catch (CommandFailedException ex) {
		} catch (DisconnectedException ex) {
			state = STATE_NONAUTHENTICATE;
			deleteFolder(mailboxName);
		}

		return true;
	}

	public boolean renameFolder(String oldMailboxName, String newMailboxName)
		throws Exception {

		ensureState(STATE_AUTHENTICATE);

		// we need to ensure that this folder is closed
		try {
			getProtocol().rename(oldMailboxName, newMailboxName);
		} catch (BadCommandException ex) {
		} catch (CommandFailedException ex) {
			return false;
		} catch (DisconnectedException ex) {
			state = STATE_NONAUTHENTICATE;
			renameFolder(oldMailboxName, newMailboxName);
		}
		return true;
	}

	public boolean subscribeFolder(String mailboxName) throws Exception {
		ensureState(STATE_AUTHENTICATE);
		try {
			getProtocol().subscribe(mailboxName);
		} catch (BadCommandException ex) {
		} catch (CommandFailedException ex) {
			return false;
		} catch (DisconnectedException ex) {
			state = STATE_NONAUTHENTICATE;
			subscribeFolder(mailboxName);
		}
		return true;
	}

	public boolean unsubscribeFolder(String mailboxName) throws Exception {
		ensureState(STATE_AUTHENTICATE);
		try {
			getProtocol().unsubscribe(mailboxName);
		} catch (BadCommandException ex) {
		} catch (CommandFailedException ex) {
			return false;
		} catch (DisconnectedException ex) {
			state = STATE_NONAUTHENTICATE;
			unsubscribeFolder(mailboxName);
		}
		return true;
	}

	/**************************** selected state ****************************/

	public List fetchUIDList(String path) throws Exception {

		isLogin();
		isSelected(path);

		// selection of mailbox failed
		// -> don't try to go any further
		if (state == STATE_AUTHENTICATE)
			return null;

		try {
			int count = messageFolderInfo.getExists();
			if (count == 0)
				return null;

			printStatusMessage(
				MailResourceLoader.getString(
					"statusbar",
					"message",
					"fetch_uid_list"));

			IMAPResponse[] responses = imap.fetchUIDList("1:*", count);
			return UIDParser.parse(responses);
		} catch (BadCommandException ex) {
		} catch (CommandFailedException ex) {
		} catch (DisconnectedException ex) {
			state = STATE_NONAUTHENTICATE;
			fetchUIDList(path);
		}
		return null;
	}

	public boolean expunge(String path) throws Exception {

		isLogin();
		isSelected(path);

		//Object[] expungedUids = null;
		try {
			IMAPResponse[] responses = imap.expunge();

			//expungedUids = FlagsParser.parseFlags(responses);

		} catch (BadCommandException ex) {
		} catch (CommandFailedException ex) {
		} catch (DisconnectedException ex) {
			state = STATE_NONAUTHENTICATE;
			expunge(path);
		}
		return true;
	}

	public void copy(String destFolder, Object[] uids, String path)
		throws Exception {

		isLogin();
		isSelected(path);

		//Object[] expungedUids = null;
		try {
			MessageSet set = new MessageSet(uids);

			IMAPResponse[] responses = imap.copy(set.getString(), destFolder);

			//expungedUids = FlagsParser.parseFlags(responses);

		} catch (BadCommandException ex) {
		} catch (CommandFailedException ex) {
		} catch (DisconnectedException ex) {
			state = STATE_NONAUTHENTICATE;
		}
	}

	public Flags[] fetchFlagsList(String path) throws Exception {

		Flags[] result = null;

		isLogin();
		isSelected(path);

		try {
			printStatusMessage(
				MailResourceLoader.getString(
					"statusbar",
					"message",
					"fetch_flags_list"));
			IMAPResponse[] responses =
				imap.fetchFlagsList("1:*", messageFolderInfo.getExists());

			result = FlagsParser.parseFlags(responses);
		} catch (BadCommandException ex) {
		} catch (CommandFailedException ex) {
		} catch (DisconnectedException ex) {
			state = STATE_NONAUTHENTICATE;
			fetchFlagsList(path);
		}
		return result;
	}

	private ColumbaHeader parseMessage(String headerString) {

		try {
			ColumbaHeader h =
				new ColumbaHeader(
					HeaderParser.parse(new CharSequenceSource(headerString)));

			return h;
		} catch (ParserException e) {
			return null;
		}
	}

	/*
	private ColumbaHeader fetchHeader(
		Object uid,
		WorkerStatusController worker,
		String path)
		throws Exception {
	
		isLogin(worker);
	
		isSelected(worker, path);
	
		boolean answer = false;
		ColumbaHeader h = new ColumbaHeader();
	
		Vector v = new Vector();
		String buffer = new String();
	
		TableItem items = MailConfig.getMainFrameOptionsConfig().getTableItem();
		StringBuffer headerFields = new StringBuffer();
	
		for (int i = 0; i < items.count(); i++) {
			HeaderItem headerItem = items.getHeaderItem(i);
	
			String name = headerItem.get("name");
			if ((!name.equals("Status"))
				|| (!name.equals("Flagged"))
				|| (!name.equals("Attachment"))
				|| (!name.equals("Priority"))
				|| (!name.equals("Size"))) {
				headerFields.append(name + " ");
			}
		}
	
		try {
	
			IMAPResponse[] responses =
				getProtocol().fetchHeaderList(
					(String) uid,
					headerFields.toString().trim());
	
			buffer = HeaderParser.parse(responses);
			//System.out.println("buffer=" + buffer);
	
			h = parseMessage(buffer);
	
			return h;
	
		} catch (BadCommandException ex) {
			System.out.println("bad command exception");
			System.out.println("no messages on server");
		} catch (CommandFailedException ex) {
			System.out.println("command failed exception");
		} catch (DisconnectedException ex) {
			state = STATE_NONAUTHENTICATE;
			fetchHeader(uid, worker, path);
		} finally {
	
		}
	
		return null;
	
	}
	*/

	public void fetchHeaderList(HeaderList headerList, List list, String path)
		throws Exception {

		ColumbaLogger.log.debug("list-count=" + list.size());

		isLogin();
		isSelected(path);

		getObservable().setMax(list.size());
		getObservable().setCurrent(0);
		MessageSet set = new MessageSet(list.toArray());

		//	get list of used-defined headerfields
		String[] headercacheList =
			CachedHeaderfieldOwner.getCachedHeaderfieldArray();
		StringBuffer headerFields = new StringBuffer();
		String name;
		for (int j = 0; j < headercacheList.length; j++) {
			name = (String) headercacheList[j];
			headerFields.append(name + " ");
		}

		boolean finished = false;
		String clientTag = null;
		IMAPResponse imapResponse = null;

		ColumbaLogger.log.debug("messageSet=" + set.getString());
		ColumbaLogger.log.debug(
			"headerFields=" + headerFields.toString().trim());

		try {
			clientTag =
				getProtocol().sendString(
					"UID FETCH "
						+ set.getString().trim()
						+ " BODY.PEEK[HEADER.FIELDS ("
						+ headerFields.toString().trim()
						+ ")]",
					null);
		} catch (IOException ex) {
			// disconnect exception
			ex.printStackTrace();
		}

		int i = 0;
		while (!finished) {
			try {

				imapResponse = getProtocol().getResponse();
			} catch (IOException ex) {
				// disconnect exception
				ex.printStackTrace();
			}

			if (imapResponse.getStatus() == IMAPResponse.STATUS_BYE)
				finished = true;

			if (imapResponse.isTagged()
				&& imapResponse.getTag().equals(clientTag))
				finished = true;

			if (!finished) {
				IMAPResponse[] r = new IMAPResponse[1];
				r[0] = imapResponse;
				//System.out.println("header=" + imapResponse.getSource());

				String buffer = imapResponse.getSource();

				if (buffer.length() != 0) {

					if (buffer.indexOf("FLAGS") != -1) {
						// flags updated
					} else {
						Object uid = null;
						ColumbaHeader header =
							parseMessage(ImapHeaderParser.parse(r));

						if (header != null) {
							header.set("columba.uid", list.get(i));
							headerList.add(header, list.get(i));
						}

						i++;
						if ((getObservable() != null) && (i % 100 == 0))
							getObservable().setCurrent(i);
						printStatusMessage(
							MessageFormat.format(
								MailResourceLoader.getString(
									"statusbar",
									"message",
									"fetch_headers"),
								new Object[] {
									new Integer(i),
									new Integer(list.size())}));
					}
				}
			}
		}
	}

	public boolean isLogin() throws Exception {
		if ((getState() == STATE_AUTHENTICATE)
			|| (getState() == STATE_SELECTED))
			return true;
		else {
			// we are in Imap4.STATE_NONAUTHENTICATE
			login();
			return false;
		}
	}

	public MimeTree getMimePartTree(Object uid, String path) throws Exception {

		isLogin();
		isSelected(path);
		try {

			IMAPResponse[] responses =
				getProtocol().fetchMimePartTree((String) uid);

			MimeTree mptree = MimeTreeParser.parse(responses);

			aktMessageUid = (String) uid;
			aktMimePartTree = mptree;

			return mptree;
		} catch (BadCommandException ex) {
		} catch (CommandFailedException ex) {
		} catch (DisconnectedException ex) {
			state = STATE_NONAUTHENTICATE;
			getMimePartTree(uid, path);
		}
		return null;
	}

	public MimePart getMimePart(Object uid, Integer[] address, String path)
		throws Exception {

		isLogin();
		isSelected(path);

		if (!aktMessageUid.equals(uid)) {
			getMimePartTree(uid, path);
		}

		LocalMimePart part =
			new LocalMimePart(
				aktMimePartTree.getFromAddress(address).getHeader());

		try {
			IMAPResponse[] responses =
				getProtocol().fetchMimePart((String) uid, address);

			part.setBody(
				new CharSequenceSource(MimePartParser.parse(responses)));

			return part;
		} catch (BadCommandException ex) {
		} catch (CommandFailedException ex) {
		} catch (DisconnectedException ex) {
			state = STATE_NONAUTHENTICATE;
			getMimePart(uid, address, path);
		}
		return null;
	}

	public String getMessageSource(Object uid, String path) throws Exception {

		isLogin();
		isSelected(path);

		try {
			IMAPResponse[] responses =
				getProtocol().fetchMessageSource((String) uid);

			String source = MessageSourceParser.parse(responses);

			return source;
		} catch (BadCommandException ex) {
		} catch (CommandFailedException ex) {
		} catch (DisconnectedException ex) {
			state = STATE_NONAUTHENTICATE;
			getMessageSource(uid, path);
		}
		return null;
	}

	public void markMessage(Object[] uids, int variant, String path)
		throws Exception {

		isLogin();
		isSelected(path);

		try {
			MessageSet set = new MessageSet(uids);

			String flagsString = parseVariant(variant);
			ColumbaLogger.log.debug("flags=" + flagsString);

			// unset flags command
			if (variant >= 4) {
				IMAPResponse[] responses =
					getProtocol().removeFlags(
						set.getString(),
						flagsString,
						true);
			} else {
				IMAPResponse[] responses =
					getProtocol().storeFlags(
						set.getString(),
						flagsString,
						true);
			}

		} catch (BadCommandException ex) {
		} catch (CommandFailedException ex) {
		} catch (DisconnectedException ex) {
			state = STATE_NONAUTHENTICATE;
			markMessage(uids, variant, path);
		}
	}

	public LinkedList search(Object[] uids, FilterRule filterRule, String path)
		throws Exception {
		LinkedList result = new LinkedList();

		isLogin();
		isSelected(path);

		try {
			printStatusMessage(
				MessageFormat.format(
					MailResourceLoader.getString(
						"statusbar",
						"message",
						"search_in"),
					new Object[] { path }));

			MessageSet set = new MessageSet(uids);

			SearchRequestBuilder b = new SearchRequestBuilder();
			b.setCharset("UTF-8");
			List list = b.generateSearchArguments(filterRule);
			Arguments searchArguments =
				b.generateSearchArguments(filterRule, list);

			IMAPResponse[] responses = null;

			// try to use UTF-8 first
			// -> fall back to system default charset
			try {
				responses =
					imap.searchWithCharsetSupport("UTF-8", searchArguments);
			} catch (BadCommandException ex) {
				// this probably means that UTF-8 isn't support by server
				// -> lets try the system  default charset instead

				try {
					String charset =
						(String) System.getProperty("file.encoding");
					b.setCharset(charset);
					list = b.generateSearchArguments(filterRule);
					searchArguments =
						b.generateSearchArguments(filterRule, list);
					responses =
						imap.searchWithCharsetSupport(charset, searchArguments);
				} catch (BadCommandException ex2) {
					// this is the last possible fall back

					String charset = "US-ASCII";
					b.setCharset(charset);
					list = b.generateSearchArguments(filterRule);
					searchArguments =
						b.generateSearchArguments(filterRule, list);
					responses = imap.search(searchArguments);
				}
			}

			result = SearchResultParser.parse(responses);

			//result = convertIndexToUid(result, worker);

			return result;
		} catch (BadCommandException ex) {
		} catch (CommandFailedException ex) {
		} catch (DisconnectedException ex) {
			state = STATE_NONAUTHENTICATE;
			//search(uids, searchString, path, worker);
		}
		return null;
	}

	public LinkedList search(FilterRule filterRule, String path)
		throws Exception {
		LinkedList result = new LinkedList();

		isLogin();
		isSelected(path);

		try {
			//MessageSet set = new MessageSet(uids);
			printStatusMessage(
				MessageFormat.format(
					MailResourceLoader.getString(
						"statusbar",
						"message",
						"search_in"),
					new Object[] { path }));
			SearchRequestBuilder b = new SearchRequestBuilder();
			b.setCharset("UTF-8");
			List list = b.generateSearchArguments(filterRule);
			Arguments searchArguments =
				b.generateSearchArguments(filterRule, list);

			IMAPResponse[] responses = null;

			// try to use UTF-8 first
			// -> fall back to system default charset
			try {
				responses =
					imap.searchWithCharsetSupport("UTF-8", searchArguments);
			} catch (BadCommandException ex) {
				// this probably means that UTF-8 isn't support by server
				// -> lets try the system  default charset instead

				try {
					String charset =
						(String) System.getProperty("file.encoding");
					b.setCharset(charset);
					list = b.generateSearchArguments(filterRule);
					searchArguments =
						b.generateSearchArguments(filterRule, list);
					responses =
						imap.searchWithCharsetSupport(charset, searchArguments);
				} catch (BadCommandException ex2) {
					// this is the last possible fall back

					String charset = "US-ASCII";
					b.setCharset(charset);
					list = b.generateSearchArguments(filterRule);
					searchArguments =
						b.generateSearchArguments(filterRule, list);
					responses = imap.search(searchArguments);
				}
			}

			result = SearchResultParser.parse(responses);

			return result;
		} catch (BadCommandException ex) {
		} catch (CommandFailedException ex) {
		} catch (DisconnectedException ex) {
			state = STATE_NONAUTHENTICATE;
			//search(searchString, path, worker);
		}
		return null;
	}

	protected static boolean isAscii(String s) {
		int l = s.length();

		for (int i = 0; i < l; i++) {
			if ((int) s.charAt(i) > 0177) // non-ascii
				return false;
		}
		return true;
	}

	private String parseVariant(int variant) {
		StringBuffer buf = new StringBuffer();
		List arg = new Vector();
		switch (variant) {
			case MarkMessageCommand.MARK_AS_READ :
			case MarkMessageCommand.MARK_AS_UNREAD :
				{
					arg.add("\\Seen");
					break;
				}
			case MarkMessageCommand.MARK_AS_FLAGGED :
			case MarkMessageCommand.MARK_AS_UNFLAGGED :
				{
					arg.add("\\Flagged");
					break;
				}
			case MarkMessageCommand.MARK_AS_EXPUNGED :
			case MarkMessageCommand.MARK_AS_UNEXPUNGED :
				{
					arg.add("\\Deleted");
					break;
				}
			case MarkMessageCommand.MARK_AS_ANSWERED :
				{
					arg.add("\\Answered");
					break;
				}
		}

		//if (arg.size() > 1)
		buf.append("(");
		for (int i = 0; i < arg.size(); i++) {
			buf.append((String) arg.get(i));
			if (i != arg.size() - 1)
				buf.append(" ");
		}
		//if (arg.size() > 1)
		buf.append(")");

		return buf.toString();
	}

}
