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
package org.columba.mail.imap;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.columba.core.command.Command;
import org.columba.core.command.CommandCancelledException;
import org.columba.core.command.StatusObservable;
import org.columba.core.util.ListTools;
import org.columba.core.util.NullWorkerStatusController;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.config.ImapItem;
import org.columba.mail.filter.FilterCriteria;
import org.columba.mail.filter.FilterRule;
import org.columba.mail.folder.command.MarkMessageCommand;
import org.columba.mail.folder.headercache.CachedHeaderfields;
import org.columba.mail.folder.imap.IMAPFolder;
import org.columba.mail.folder.imap.IMAPRootFolder;
import org.columba.mail.gui.tree.command.FetchSubFolderListCommand;
import org.columba.mail.gui.util.PasswordDialog;
import org.columba.mail.message.ColumbaHeader;
import org.columba.mail.message.HeaderList;
import org.columba.mail.util.MailResourceLoader;
import org.columba.ristretto.imap.IMAPDate;
import org.columba.ristretto.imap.IMAPFlags;
import org.columba.ristretto.imap.IMAPHeader;
import org.columba.ristretto.imap.IMAPResponse;
import org.columba.ristretto.imap.ListInfo;
import org.columba.ristretto.imap.MailboxStatus;
import org.columba.ristretto.imap.SearchKey;
import org.columba.ristretto.imap.SequenceSet;
import org.columba.ristretto.imap.protocol.IMAPException;
import org.columba.ristretto.imap.protocol.IMAPProtocol;
import org.columba.ristretto.imap.protocol.IMAPProtocol;
import org.columba.ristretto.message.Header;
import org.columba.ristretto.message.MailboxInfo;
import org.columba.ristretto.message.MimePart;
import org.columba.ristretto.message.MimeTree;
import org.columba.ristretto.message.io.CharSequenceSource;
import org.columba.ristretto.message.io.SequenceInputStream;
import org.columba.ristretto.parser.HeaderParser;
import org.columba.ristretto.parser.ParserException;

/**
 * IMAPStore encapsulates IMAPProtocol and the parsers for IMAPFolder.
 * <p>
 * This way {@link IMAPFolder}doesn't need to do any parsing work, etc.
 * <p>
 * Every {@link IMAPFolder}of a single account has also an
 * {@link IMAPRootFolder}, which keeps a reference to {@link IMAPServer}.
 * Which itself uses {@link IMAPProtocol}.
 * <p>
 * IMAPStore handles the current state of connection:
 * <ul>
 * <li>STATE_NONAUTHENTICATE - not authenticated</li>
 * <li>STATE_AUTHENTICATE - authenticated</li>
 * <li>STATE_SELECTED - mailbox is selected</li>
 * </ul>
 * <p>
 * It keeps a reference to the currently selected mailbox.
 * <p>
 * IMAPFolder shouldn't use IMAPProtocol directly, instead it should use
 * IMAPStore.
 * 
 * @author fdietz
 */
public class IMAPServer {

	private static final Logger LOG = Logger.getLogger("org.columba.mail.imap");

	private static final Charset UTF8 = Charset.forName("UTF-8");
	private static final Charset DEFAULT = Charset.forName(System.getProperty("file.encoding"));
	
	/**
	 * currently selected mailbox
	 */
	private String selectedFolderPath;

	/**
	 * mailbox name delimiter
	 * <p>
	 * example: "/" (uw-imap), or "." (cyrus)
	 */
	private String delimiter;

	/**
	 * reference to IMAP protocol
	 */
	private IMAPProtocol protocol;

	/**
	 * configuration options of this IMAP account
	 */
	private ImapItem item;

	/**
	 * reference to root folder
	 */
	private IMAPRootFolder imapRoot;

	private MimeTree aktMimeTree;

	private Object aktMessageUid;

	private MailboxInfo messageFolderInfo;
	
	private boolean firstLogin;

	public IMAPServer(ImapItem item, IMAPRootFolder root) {
		this.item = item;
		this.imapRoot = root;

		// create IMAP protocol
		protocol = new IMAPProtocol(item.get("host"), item.getInteger("port"));

		// register interest on status updates
		//protocol.registerInterest((ProgressObserver) root.getObservable());

		firstLogin = true;
	}

	/**
	 * @return
	 */
	protected StatusObservable getObservable() {
		return imapRoot.getObservable();
	}

	/**
	 * @param message
	 */
	protected void printStatusMessage(String message) {
		getObservable().setMessage(item.get("host") + ": " + message);
	}

	/**
	 * Get mailbox path delimiter
	 * <p>
	 * example: "/" (uw-imap), or "." (cyrus)
	 * 
	 * @return delimiter
	 */
	public String getDelimiter() throws IOException, IMAPException, CommandCancelledException {
		if (delimiter == null) {
			// try to determine delimiter
			delimiter = fetchDelimiter();
		}

		return delimiter;
	}

	/**
	 * @return currenlty selected mailbox
	 */
	protected String getSelectedFolderPath() {
		return selectedFolderPath;
	}

	/**
	 * @param s
	 *            currenlty selected mailbox
	 */
	protected void setSelectedFolderPath(String s) {
		selectedFolderPath = s;
	}

	/**
	 * Logout cleanly.
	 * 
	 * @throws Exception
	 */
	public void logout() throws Exception {
		protocol.logout();
	}

	private void openConnection() throws IOException, IMAPException {
		printStatusMessage(MailResourceLoader.getString("statusbar", "message",
				"connecting"));

		protocol.openPort();

		if (item.getBoolean("enable_ssl", false)) {
			protocol.switchToSSL();
		}
		//TODO: Implement IMAPS
	}

	/**
	 * Login to IMAP server.
	 * <p>
	 * Ask user for password. TODO: cleanup if all these ugly if, else cases
	 * 
	 * @throws Exception
	 */
	private void login() throws IOException, IMAPException,
			CommandCancelledException {
		PasswordDialog dialog = new PasswordDialog();
		;

		boolean authenticated = false;
		boolean first = true;

		char[] password = null;

		printStatusMessage(MailResourceLoader.getString("statusbar", "message",
				"authenticating"));

		String loginMethod = item.get("login_method"
				);
		//TODO: Use selected authentication method

		// Try to get Password from Configuration
		if (item.get("password").length() != 0) {
			password = item.get("password").toCharArray();
		} 

		// Login loop until authenticated
		while (!authenticated) {

			// On the first try check if we need to show the password dialog
			// -> not necessary when password was stored
			if (!first || password == null) {
				// Show the password dialog
				if(password == null) password = new char[0]; 
				
				dialog.showDialog(item.get("user"), new String(password), item
						.get("host"), item.getBoolean("save_password"));
				if (dialog.success()) {
					// User pressed OK
					password = dialog.getPassword();

					// Save or Clear the password in the configuration
					item.set("save_password", dialog.getSave());
					if (dialog.getSave()) {
						item.set("password", new String(password));
					} else {
						item.set("password", "");
					}
				} else {
					//User cancelled authentication

					throw new CommandCancelledException();
				}
			}

			// From this point we have a username and password
			// from configuration of from the dialog

			try {
				protocol.login(item.get("user"), password);

				// If no exception happened we have successfully logged
				// in
				authenticated = true;
			} catch (IMAPException ex) {
				// login failed?
				IMAPResponse response = ex.getResponse();
				if (response == null || !response.isNO()) {
					// This exception is not because wrong username or
					// password
					throw ex;
				}
			}
			first = false;
		}
		
		// Sync subscribed folders if this is the first login
		// in this session
		if( firstLogin ) {
	        Command c = new FetchSubFolderListCommand(new FolderCommandReference[] {
                    new FolderCommandReference(imapRoot)
                });
	        try {
				//MainInterface.processor.addOp(c);
				c.execute(NullWorkerStatusController.getInstance());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		firstLogin = false;
	}

	/**
	 * Check if mailbox is already selected.
	 * <p>
	 * If its not selected -> select it.
	 * 
	 * @param path
	 *            mailbox path
	 * @throws Exception
	 */
	protected void ensureSelectedState(String path) throws IOException, IMAPException, CommandCancelledException  {
		// ensure that we are logged in already
		ensureLoginState();

		// if mailbox is not already selected select it
		if( protocol.getState() != IMAPProtocol.SELECTED || !protocol.getSelectedMailbox().equals(path)) {
			
			// Here we get the new mailboxinfo for the folder
			// but we do not use it here
			messageFolderInfo = protocol.select(path);

			// delete any cached information
			aktMimeTree = null;
			aktMessageUid = null;
		}
	}

	public MailboxInfo getMailboxInfo(String path) throws IOException, IMAPException, CommandCancelledException {
		ensureLoginState();
		
		return protocol.select(path);
	}
	
	/**
	 * Fetch delimiter.
	 *  
	 */
	protected String fetchDelimiter() throws IOException, IMAPException, CommandCancelledException {
		// make sure we are already logged in
		ensureLoginState();

		ListInfo[] listInfo = protocol.list("", "");
		
		return listInfo[0].getDelimiter();
	}

	/**
	 * List available Mailboxes.
	 * 
	 * @param reference
	 * @param pattern
	 * @return @throws
	 *         Exception
	 */
	public ListInfo[] list(String reference, String pattern) throws Exception {
		ensureLoginState();

		return protocol.list(reference,pattern);
		
	}

	/**
	 * Append message to mailbox.
	 * 
	 * @param mailboxName
	 *            name of mailbox
	 * @param messageSource
	 *            message source
	 * @throws Exception
	 */
	public Integer append(String mailboxName, InputStream messageSource)
			throws Exception {
		// make sure we are already logged in
		ensureLoginState();
		
		// close the mailbox if it is selected
		if( protocol.getState() == IMAPProtocol.SELECTED && protocol.getSelectedMailbox().equals(mailboxName )) {
			protocol.close();
		}

		MailboxStatus status = protocol.status(mailboxName,new String[] {"UIDNEXT"});

		protocol.append(mailboxName, messageSource);
		
		return new Integer( (int) status.getUidNext() );
	}

	/**
	 * Append message to mailbox.
	 * 
	 * @param mailboxName
	 *            name of mailbox
	 * @param messageSource
	 *            message source
	 * @throws Exception
	 */
	public Integer append(String mailboxName, InputStream messageSource, IMAPFlags flags)
			throws Exception {
		// make sure we are already logged in
		ensureLoginState();
		
		// close the mailbox if it is selected
		if( protocol.getState() == IMAPProtocol.SELECTED && protocol.getSelectedMailbox().equals(mailboxName )) {
			protocol.close();
		}

		MailboxStatus status = protocol.status(mailboxName,new String[] {"UIDNEXT"});

		protocol.append(mailboxName, messageSource, new Object[] { flags });
		
		return new Integer( (int) status.getUidNext() );
	}

	/**
	 * Create new mailbox.
	 * 
	 * @param mailboxName
	 *            name of new mailbox
	 * @return @throws
	 *         Exception
	 */
	public void createMailbox(String path, String mailboxName) throws IOException, IMAPException, CommandCancelledException {
		//make sure we are logged in
		ensureLoginState();
		
		//concate the full name of the new mailbox
		String fullName;
		
		if(path.length() > 0 )
			fullName = path + getDelimiter() + mailboxName;
		else
			fullName = mailboxName;
		
		// create the mailbox on the server
		protocol.create( fullName );
		
		// subscribe to the new mailbox
		protocol.subscribe( fullName );		
	}

	/**
	 * Delete mailbox.
	 * 
	 * @param mailboxName
	 *            name of mailbox
	 * @return @throws
	 *         Exception
	 */
	public void deleteFolder(String path) throws Exception {
		// make sure we are already logged in
		ensureLoginState();

		if( protocol.getState() == IMAPProtocol.SELECTED && protocol.getSelectedMailbox().equals(path )) {
			protocol.close();
		}

		protocol.unsubscribe(path);
		
		protocol.delete(path);
	}

	/**
	 * Rename mailbox.
	 * 
	 * @param oldMailboxName
	 *            old mailbox name
	 * @param newMailboxName
	 *            new mailbox name
	 * @return @throws
	 *         Exception
	 */
	public void renameFolder(String oldMailboxName, String newMailboxName)
			throws IOException, IMAPException, CommandCancelledException {
		// make sure we are already logged in
		ensureLoginState();
		protocol.rename(oldMailboxName, newMailboxName);
	}

	/**
	 * Subscribe to mailbox.
	 * 
	 * @param mailboxName
	 *            name of mailbox
	 * @return @throws
	 *         Exception
	 */
	public void subscribeFolder(String mailboxName) throws IOException, IMAPException, CommandCancelledException {
		// make sure we are already logged in
		ensureLoginState();

		protocol.subscribe(mailboxName);		
	}

	/**
	 * Unsubscribe to mailbox.
	 * 
	 * @param mailboxNamename
	 *            of mailbox
	 * @return @throws
	 *         Exception
	 */
	public void unsubscribeFolder(String mailboxName) throws IOException, IMAPException, CommandCancelledException {
		// make sure we are already logged in
		ensureLoginState();

		protocol.unsubscribe(mailboxName);
	}

	/** ************************** selected state *************************** */
	/**
	 * Fetch UID list and parse it.
	 * 
	 * @param path
	 *            mailbox name
	 * @return list of UIDs
	 * @throws Exception
	 */
	public List fetchUIDList(String path) throws IOException, IMAPException, CommandCancelledException {
		ensureSelectedState(path);

			int count = messageFolderInfo.getExists();

			if (count == 0) {
				return null;
			}

			printStatusMessage(MailResourceLoader.getString("statusbar",
					"message", "fetch_uid_list"));

			Integer[] uids = protocol.fetchUid(SequenceSet.getAll());


			return Arrays.asList(uids);
	}

	/**
	 * Expunge folder.
	 * <p>
	 * Delete every message mark as expunged.
	 * 
	 * @param path
	 *            name of mailbox
	 * @return @throws
	 *         Exception
	 */
	public void expunge(String path) throws IOException, IMAPException, CommandCancelledException {
		ensureSelectedState(path);
		
		protocol.expunge();
	}

	/**
	 * Copy a set of messages to another mailbox on the same IMAP server.
	 * <p>
	 * <p>
	 * We copy messages in pieces of 100 headers. This means we tokenize the
	 * <code>list</code> in sublists of the size of 100. Then we execute the
	 * command and process those 100 results.
	 * 
	 * @param destFolder
	 *            destination mailbox
	 * @param uids
	 *            UIDs of messages
	 * @param path
	 *            source mailbox
	 * @throws Exception
	 */
	public Integer[] copy(String destFolder, Object[] uids, String path)
			throws Exception {
		ensureSelectedState(path);

		protocol.uidCopy(new SequenceSet(Arrays.asList(uids)), destFolder);

		MailboxStatus status = protocol.status(destFolder ,new String[] {"UIDNEXT"});
	
		// the UIDS start UIDNext - uids.length() - 1 till UIDNext - 1
		Integer[] destUids = new Integer[uids.length];
		for( int i=0; i<uids.length; i++) {
			destUids[i] = new Integer( (int) (status.getUidNext() - (uids.length - i)));
		}
		
		return destUids;
	}	

	/**
	 * Fetch list of flags and parse it.
	 * 
	 * @param path
	 *            mailbox name
	 * @return list of flags
	 * @throws Exception
	 */
	public IMAPFlags[] fetchFlagsList(String path) throws IOException, IMAPException, CommandCancelledException {
		ensureSelectedState(path);
		if(messageFolderInfo.getExists() > 0) {
			return protocol.fetchFlags(SequenceSet.getAll());
		} else {
			return new IMAPFlags[0];
		}
	}

	/**
	 * @param headerString
	 * @return
	 */
	private ColumbaHeader parseMessage(String headerString) {
		try {
			ColumbaHeader h = new ColumbaHeader(HeaderParser
					.parse(new CharSequenceSource(headerString)));

			return h;
		} catch (ParserException e) {
			return null;
		}
	}

	/**
	 * Fetch list of headers and parse them.
	 * <p>
	 * We fetch headers in pieces of 100 headers. This means we tokenize the
	 * <code>list</code> in sublists of the size of 100. Then we execute the
	 * command and process those 100 results.
	 * 
	 * @param headerList
	 *            headerlist to add new headers
	 * @param list
	 *            list of UIDs to download
	 * @param path
	 *            mailbox name
	 * @throws Exception
	 */
	public void fetchHeaderList(HeaderList headerList, List list, String path)
			throws Exception {
		// make sure this mailbox is selected
		ensureSelectedState(path);

		//get list of user-defined headerfields
		String[] headerFields = CachedHeaderfields.getCachedHeaderfields();
		
		IMAPHeader[] headers = protocol.uidFetchHeaderFields(new SequenceSet(list), headerFields);

		for(int i=0; i<headers.length; i++) {
			// add it to the headerlist
			ColumbaHeader header = new ColumbaHeader(headers[i].getHeader());
			Object uid = headers[i].getUid();

			header.set("columba.uid", uid);
			header.set("columba.size", headers[i].getSize());

			// set the attachment flag
			String contentType = (String) header.get("Content-Type");

			header.set("columba.attachment", header.hasAttachments());

			headerList.add(header, uid);
		}
		
		/*
		// calculate number of requests
		int requestCount = list.size() / 100;

		// initialize progressbar
		getObservable().setMax(requestCount);
		getObservable().setCurrent(0);

		// we use a token size of 100
		MessageSetTokenizer tok = new MessageSetTokenizer(list, 100);

		int counter = 0;

		while (tok.hasNext()) {
			List l = (List) tok.next();

			//MessageSet set = new MessageSet(l.toArray());
			// fetch headers from server
			IMAPResponse[] r = getProtocol().fetchHeaderList(
					UIDSetParser.parse(l.toArray()),
					headerFields.toString().trim());

			// parse headers
			for (int i = 0; i < r.length; i++) {
				if (r[i].getResponseSubType().equals("FETCH")) {
					// parse the reponse
					IMAPHeader imapHeader = IMAPHeaderParser.parse(r[i]);

					// consume this line
					r[i] = null;

					// add it to the headerlist
					ColumbaHeader header = new ColumbaHeader(imapHeader
							.getHeader());
					Object uid = imapHeader.getUid();

					header.set("columba.uid", uid);
					header.set("columba.size", imapHeader.getSize());

					// set the attachment flag
					String contentType = (String) header.get("Content-Type");

					header.set("columba.attachment", header.hasAttachments());

					headerList.add(header, uid);
				}
			}

			if (getObservable() != null) {
				getObservable().setCurrent(counter++);
			}

			printStatusMessage(MailResourceLoader.getString("statusbar",
					"message", "fetch_headers"));
		}
		
		*/
	}

	/**
	 * Ensure that we are in login state.
	 * 
	 * @throws Exception
	 */
	protected void ensureLoginState() throws IOException, IMAPException, CommandCancelledException {
		int actState;

		actState = protocol.getState();

		while (actState < IMAPProtocol.AUTHENTICATED) {
			switch (actState) {
				case IMAPProtocol.LOGOUT : {
					openConnection();
					break;
				}

				case IMAPProtocol.NON_AUTHENTICATED : {
					login();
					break;
				}

			}

			actState = protocol.getState();
		}

		/*
		 * if ((getState() == STATE_AUTHENTICATE) || (getState() ==
		 * STATE_SELECTED)) { // ok, we are logged in } else { // we are in
		 * Imap4.STATE_NONAUTHENTICATE // -> force new login login(); // if
		 * login was successfull if (getState() == STATE_AUTHENTICATE) { //
		 * synchronize folder list with server parent.syncSubscribedFolders(); } }
		 */
	}

	/**
	 * Get {@link MimeTree}.
	 * 
	 * @param uid
	 *            message UID
	 * @param path
	 *            mailbox name
	 * @return mimetree
	 * @throws Exception
	 */
	public MimeTree getMimePartTree(Object uid, String path) throws IOException, IMAPException, CommandCancelledException {
		ensureSelectedState(path);
		
		// Use a caching mechanism for this 
		if( aktMimeTree == null || !aktMessageUid.equals( uid) ) {
			aktMimeTree = protocol.uidFetchBodystructure(((Integer)uid).intValue());
			aktMessageUid = uid;
		}
			
		return aktMimeTree;
	}

	/**
	 * Get {@link MimePart}.
	 * 
	 * @param uid
	 *            message UID
	 * @param address
	 *            address of MimePart in MimeTree
	 * @param path
	 *            mailbox name
	 * @return mimepart
	 * @throws Exception
	 */
	public InputStream getMimePartBodyStream(Object uid, Integer[] address, String path)
			throws Exception {
		ensureSelectedState(path);

		return protocol.uidFetchBody(((Integer)uid).intValue(), address);
	}

	/**
	 * Get {@link MimePart}.
	 * 
	 * @param uid
	 *            message UID
	 * @param address
	 *            address of MimePart in MimeTree
	 * @param path
	 *            mailbox name
	 * @return mimepart
	 * @throws Exception
	 */
	public Header getHeaders(Object uid, String[] keys, String path)
			throws Exception {
		ensureSelectedState(path);

		IMAPHeader[] headers = protocol.uidFetchHeaderFields(new SequenceSet(((Integer) uid).intValue()), keys);
		
		return headers[0].getHeader();
	}

	/**
	 * Get {@link MimePart}.
	 * 
	 * @param uid
	 *            message UID
	 * @param address
	 *            address of MimePart in MimeTree
	 * @param path
	 *            mailbox name
	 * @return mimepart
	 * @throws Exception
	 */
	public InputStream getMimePartSourceStream(Object uid, Integer[] address, String path)
			throws Exception {
		ensureSelectedState(path);

		InputStream headerSource = protocol.uidFetchMimeHeaderSource(((Integer) uid).intValue(), address);
		InputStream bodySource = protocol.uidFetchBody(((Integer) uid).intValue(), address);
		
		return new SequenceInputStream( headerSource, bodySource );
	}

	/**
	 * Get complete message source.
	 * 
	 * @param uid
	 *            message UID
	 * @param path
	 *            mailbox name
	 * @return message source
	 * @throws Exception
	 */
	public InputStream getMessageSourceStream(Object uid, String path) throws Exception {
		ensureSelectedState(path);
		
		return protocol.uidFetchMessage(((Integer) uid).intValue());
	}

	/**
	 * Mark message as specified by variant.
	 * <p>
	 * See {@link MarkMessageCommand}for a list of variants.
	 * <p>
	 * We mark messages in pieces of 100 headers. This means we tokenize the
	 * <code>list</code> in sublists of the size of 100. Then we execute the
	 * command and process those 100 results.
	 * 
	 * @param uids
	 *            message UID
	 * @param variant
	 *            variant (read/flagged/expunged/etc.)
	 * @param path
	 *            mailbox name
	 * @throws Exception
	 */
	public void markMessage(Object[] uids, int variant, String path)
			throws IOException, IMAPException, CommandCancelledException {
		ensureSelectedState(path);
		
		SequenceSet uidSet = new SequenceSet(Arrays.asList(uids));
		
		protocol.uidStore(uidSet, variant > 0, convertToFlags(variant));	
	}

	public void setFlags(Object[] uids, IMAPFlags flags, String path) throws IOException, IMAPException, CommandCancelledException {
		ensureSelectedState(path);
		SequenceSet uidSet = new SequenceSet(Arrays.asList(uids));

		protocol.uidStore(uidSet, true, flags );		
	}
	
	/**
	 * Search messages.
	 * 
	 * @param uids
	 *            message UIDs
	 * @param filterRule
	 *            filter rules
	 * @param path
	 *            mailbox name
	 * @return list of UIDs which match filter rules
	 * @throws Exception
	 */
	public List search(Object[] uids, FilterRule filterRule, String path)
			throws Exception {
		LinkedList result = new LinkedList (search( filterRule, path));
		
		ListTools.intersect(result, Arrays.asList(uids));
		
		return result;
	}

	/**
	 * @param filterRule
	 * @param path
	 * @return @throws
	 *         Exception
	 */
	public List search(FilterRule filterRule, String path)
			throws Exception {

		ensureSelectedState(path);

		SearchKey[] searchRequest;
		
		searchRequest = createSearchKey(filterRule);

		Integer[] result = null;
		Charset charset = UTF8;
		
		while( result == null) {
			try {
				result = protocol.uidSearch(charset, searchRequest);
			} catch (IMAPException e) {
				if( e.getResponse().isNO() ) {
					// Server does not support UTF-8
					// -> fall back to System default
					if( charset.equals(UTF8) ) {
						charset = DEFAULT;
					} else if ( charset == DEFAULT ) {
						// If this also does not work
						// -> fall back to no charset specified
						charset = null;
					} else {			
						// something else is wrong
						throw e;
					}
				} else throw e;
			}
		}
		
		return Arrays.asList( result );
	}

	/**
	 * @param filterRule
	 */
	private SearchKey[] createSearchKey(FilterRule filterRule) {
		SearchKey[] searchRequest;
		int argumentSize = filterRule.getChildCount();
		// One or many arguments?
		if( argumentSize == 1 ) {
			// One is the easiest case
			searchRequest = new SearchKey[] { getSearchKey( filterRule.get(0) )};
		} else {
			// AND or OR ? -> AND is implicit, OR must be specified
			if( filterRule.getConditionInt() == FilterRule.MATCH_ALL) {
				// AND : simply create a list of arguments
				searchRequest = new SearchKey[argumentSize];
				
				for( int i=0; i<argumentSize; i++) {
					searchRequest[i] = getSearchKey( filterRule.get(i));
				}
				
			} else {
				// OR : the arguments must be glued by a OR SearchKey
				SearchKey orKey;
				
				orKey = new SearchKey(SearchKey.OR, getSearchKey( filterRule.get(argumentSize-1)), getSearchKey( filterRule.get(argumentSize-2)));
				
				for( int i=argumentSize-3; i >= 0; i--) {
					orKey = new SearchKey(SearchKey.OR, getSearchKey( filterRule.get(i)), orKey);
				}
				
				searchRequest = new SearchKey[] {orKey};
			}
		}
		
		return searchRequest;
	}

	/**
	 * @param criteria
	 * @return
	 */
	private SearchKey getSearchKey(FilterCriteria criteria) {
		int operator = criteria.getCriteria();
		int type = criteria.getTypeItem();
		
		switch( type ) {
			case FilterCriteria.FROM : {
				if( operator == FilterCriteria.CONTAINS ) {
					return new SearchKey( SearchKey.FROM, criteria.getPattern() );
				} else {
					// contains not
					return new SearchKey( SearchKey.NOT, new SearchKey( SearchKey.FROM, criteria.getPattern() ));
				}
			}

			case FilterCriteria.CC : {
				if( operator == FilterCriteria.CONTAINS ) {
					return new SearchKey( SearchKey.CC, criteria.getPattern() );
				} else {
					// contains not
					return new SearchKey( SearchKey.NOT, new SearchKey( SearchKey.CC, criteria.getPattern() ));
				}
			}
			
			case FilterCriteria.BCC : {
				if( operator == FilterCriteria.CONTAINS ) {
					return new SearchKey( SearchKey.BCC, criteria.getPattern() );
				} else {
					// contains not
					return new SearchKey( SearchKey.NOT, new SearchKey( SearchKey.BCC, criteria.getPattern() ));
				}
			}

			case FilterCriteria.TO : {
				if( operator == FilterCriteria.CONTAINS ) {
					return new SearchKey( SearchKey.TO, criteria.getPattern() );
				} else {
					// contains not
					return new SearchKey( SearchKey.NOT, new SearchKey( SearchKey.TO, criteria.getPattern() ));
				}
			}

			case FilterCriteria.SUBJECT : {
				if( operator == FilterCriteria.CONTAINS ) {
					return new SearchKey( SearchKey.SUBJECT, criteria.getPattern() );
				} else {
					// contains not
					return new SearchKey( SearchKey.NOT, new SearchKey( SearchKey.SUBJECT, criteria.getPattern() ));
				}
			}

			case FilterCriteria.BODY : {
				if( operator == FilterCriteria.CONTAINS ) {
					return new SearchKey( SearchKey.BODY, criteria.getPattern() );
				} else {
					// contains not
					return new SearchKey( SearchKey.NOT, new SearchKey( SearchKey.BODY, criteria.getPattern() ));
				}
			}
			
			case FilterCriteria.CUSTOM_HEADERFIELD : {
				if( operator == FilterCriteria.CONTAINS ) {
					return new SearchKey( SearchKey.HEADER, criteria.getHeaderItemString(), criteria.getPattern() );
				} else {
					// contains not
					return new SearchKey( SearchKey.NOT, new SearchKey( SearchKey.HEADER, criteria.getHeaderItemString(), criteria.getPattern() ));
				}
			}
			
			case FilterCriteria.DATE : {
		        DateFormat df = DateFormat.getDateInstance();

		        IMAPDate searchPattern = null;

		        try {
		            searchPattern = new IMAPDate( df.parse(criteria.getPattern()));
		        } catch (java.text.ParseException ex) {
		            // should never happen
		            ex.printStackTrace();
		        }
				
				if( operator == FilterCriteria.DATE_BEFORE ) {
					return new SearchKey( SearchKey.BEFORE, searchPattern );
				} else {
					// AFTER
					return new SearchKey( SearchKey.NOT, new SearchKey( SearchKey.BEFORE, searchPattern ));
				}
			}

			case FilterCriteria.SIZE : {
				if( operator == FilterCriteria.SIZE_SMALLER ) {
					return new SearchKey( SearchKey.SMALLER, criteria.getPattern() );
				} else {
					// contains not
					return new SearchKey( SearchKey.NOT, new SearchKey( SearchKey.SMALLER, criteria.getPattern() ));
				}
			}
		}

		return null;
	}

	/**
	 * Check if string contains US-ASCII characters.
	 * 
	 * @param s
	 * @return true, if string contains US-ASCII characters
	 */
	protected static boolean isAscii(String s) {
		int l = s.length();

		for (int i = 0; i < l; i++) {
			if ((int) s.charAt(i) > 0177) { // non-ascii

				return false;
			}
		}

		return true;
	}

	/**
	 * Create string representation of {@ link MarkMessageCommand}constants.
	 * 
	 * @param variant
	 * @return
	 */
	private IMAPFlags convertToFlags(int variant) {
		IMAPFlags result = new IMAPFlags();
		
		switch (variant) {
			case MarkMessageCommand.MARK_AS_READ :
			case MarkMessageCommand.MARK_AS_UNREAD : {
				result.setSeen(true);
				
				break;
			}

			case MarkMessageCommand.MARK_AS_FLAGGED :
			case MarkMessageCommand.MARK_AS_UNFLAGGED : {
				result.setFlagged(true);

				break;
			}

			case MarkMessageCommand.MARK_AS_EXPUNGED :
			case MarkMessageCommand.MARK_AS_UNEXPUNGED : {
				result.setExpunged(true);

				break;
			}

			case MarkMessageCommand.MARK_AS_ANSWERED : {
				result.setAnswered(true);

				break;
			}

			case MarkMessageCommand.MARK_AS_SPAM :
			case MarkMessageCommand.MARK_AS_NOTSPAM : {
				result.setJunk(true);

				break;
			}
			case MarkMessageCommand.MARK_AS_DRAFT : {
				result.setDraft(true);

				break;
			}
		}
		
		return result;
	}

	/**
	 * @return
	 */
	public MailboxInfo getSelectedFolderMessageFolderInfo() {
		return messageFolderInfo;
	}

	/**
	 * @return
	 */
	public ListInfo[] fetchSubscribedFolders() throws IOException, IMAPException, CommandCancelledException  {
		ensureLoginState();		
		ListInfo[] lsub = protocol.lsub("","*");
		
		// Also set the delimiter
		if( lsub.length > 0 ) {
			delimiter = lsub[0].getDelimiter();
		}
		
		return lsub;
	}

	/**
	 * @param imapPath
	 * @return
	 */
	public boolean isSelected(String path) {
		return (protocol.getState() == IMAPProtocol.SELECTED && protocol.getSelectedMailbox().equals(path ));
	}
}