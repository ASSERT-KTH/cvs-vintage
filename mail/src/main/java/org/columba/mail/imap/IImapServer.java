package org.columba.mail.imap;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Observable;

import org.columba.api.command.IStatusObservable;
import org.columba.core.command.CommandCancelledException;
import org.columba.core.filter.FilterRule;
import org.columba.mail.config.ImapItem;
import org.columba.mail.folder.command.MarkMessageCommand;
import org.columba.mail.folder.imap.IMAPFolder;
import org.columba.mail.message.IHeaderList;
import org.columba.ristretto.imap.IMAPException;
import org.columba.ristretto.imap.IMAPFlags;
import org.columba.ristretto.imap.ListInfo;
import org.columba.ristretto.imap.MailboxStatus;
import org.columba.ristretto.imap.NamespaceCollection;
import org.columba.ristretto.imap.SearchKey;
import org.columba.ristretto.imap.SequenceSet;
import org.columba.ristretto.message.Header;
import org.columba.ristretto.message.MailboxInfo;
import org.columba.ristretto.message.MimePart;
import org.columba.ristretto.message.MimeTree;

public interface IImapServer {

	/**
	 * Get mailbox path delimiter
	 * <p>
	 * example: "/" (uw-imap), or "." (cyrus)
	 * 
	 * @return delimiter
	 */
	public String getDelimiter() throws IOException, IMAPException,
			CommandCancelledException;

	/**
	 * Logout cleanly.
	 * 
	 * @throws Exception
	 */
	public void logout() throws Exception;

	public List checkSupportedAuthenticationMethods() throws IOException;

	/**
	 * @param command
	 * @return
	 */
	public boolean isSupported(String command) throws IOException;

	public void setFirstLoginAction(IFirstLoginAction action);

	/**
	 * Check if mailbox is already selected.
	 * <p>
	 * If its not selected -> select it.
	 * 
	 * @param path
	 *            mailbox path
	 * @throws Exception
	 */
	public void ensureSelectedState(IMAPFolder folder) throws IOException,
			IMAPException, CommandCancelledException;

	public MailboxStatus getStatus(IMAPFolder folder) throws IOException,
			IMAPException, CommandCancelledException;

	/**
	 * List available Mailboxes.
	 * 
	 * @param reference
	 * @param pattern
	 * @return
	 * @throws Exception
	 */
	public ListInfo[] list(String reference, String pattern) throws Exception;

	/**
	 * Append message to mailbox.
	 * 
	 * @param messageSource
	 *            message source
	 * @param folder
	 *            name of mailbox
	 * 
	 * @throws Exception
	 */
	public Integer append(InputStream messageSource, IMAPFlags flags,
			IMAPFolder folder) throws Exception;

	/**
	 * Append message to mailbox.
	 * 
	 * @param messageSource
	 *            message source
	 * @param folder
	 *            name of mailbox
	 * 
	 * @throws Exception
	 */
	public Integer append(InputStream messageSource, IMAPFolder folder)
			throws Exception;

	/**
	 * Create new mailbox.
	 * 
	 * @param mailboxName
	 *            name of new mailbox
	 * 
	 * @return
	 * @throws Exception
	 */
	public void createMailbox(String mailboxName, IMAPFolder folder)
			throws IOException, IMAPException, CommandCancelledException;

	/**
	 * Delete mailbox.
	 * 
	 * @param mailboxName
	 *            name of mailbox
	 * @return
	 * @throws Exception
	 */
	public void deleteFolder(String path) throws Exception;

	/**
	 * Rename mailbox.
	 * 
	 * @param oldMailboxName
	 *            old mailbox name
	 * @param newMailboxName
	 *            new mailbox name
	 * @return
	 * @throws Exception
	 */
	public void renameFolder(String oldMailboxName, String newMailboxName)
			throws IOException, IMAPException, CommandCancelledException;

	/**
	 * Subscribe to mailbox.
	 * 
	 * @param mailboxName
	 *            name of mailbox
	 * @return
	 * @throws Exception
	 */
	public void subscribeFolder(String mailboxName) throws IOException,
			IMAPException, CommandCancelledException;

	/**
	 * Unsubscribe to mailbox.
	 * 
	 * @param mailboxNamename
	 *            of mailbox
	 * @return
	 * @throws Exception
	 */
	public void unsubscribeFolder(String mailboxName) throws IOException,
			IMAPException, CommandCancelledException;

	/**
	 * Expunge folder.
	 * <p>
	 * Delete every message mark as expunged.
	 * 
	 * @param folder
	 *            name of mailbox
	 * @return
	 * @throws Exception
	 */
	public void expunge(IMAPFolder folder) throws IOException, IMAPException,
			CommandCancelledException;

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
	 *            UIDs of messages -> this array will get sorted!
	 * @param path
	 *            source mailbox
	 * @throws Exception
	 */
	public Integer[] copy(IMAPFolder destFolder, Object[] uids,
			IMAPFolder folder) throws Exception;

	/**
	 * Fetch the uid for the index.
	 * 
	 * @param index of the message
	 * @param folder the IMAP mailbox
	 * @return uid of the message
	 * @throws IOException
	 * @throws IMAPException
	 * @throws CommandCancelledException
	 */
	public int fetchUid(SequenceSet set, IMAPFolder folder) throws IOException,
			IMAPException, CommandCancelledException;

	/**
	 * Fetch list of UIDs.
	 * 
	 * @param folder
	 *            mailbox name
	 * 
	 * @return list of flags
	 * @throws Exception
	 */
	public Integer[] fetchUids(SequenceSet set, IMAPFolder folder)
			throws IOException, IMAPException, CommandCancelledException;

	/**
	 * Fetch list of flags and parse it.
	 * 
	 * @param folder
	 *            mailbox name
	 * 
	 * @return list of flags
	 * @throws Exception
	 */
	public IMAPFlags[] fetchFlagsListStartFrom(int startIdx, IMAPFolder folder)
			throws IOException, IMAPException, CommandCancelledException;

	/**
	 * Fetch list of flags and parse it.
	 * 
	 * @param folder
	 *            mailbox name
	 * 
	 * @return list of flags
	 * @throws Exception
	 */
	public IMAPFlags[] fetchFlagsListStartFrom2(int startIdx, IMAPFolder folder)
			throws IOException, IMAPException, CommandCancelledException;

	public NamespaceCollection fetchNamespaces() throws IOException,
			IMAPException, CommandCancelledException;

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
	public void fetchHeaderList(IHeaderList headerList, List list,
			IMAPFolder folder) throws Exception;

	/**
	 * Get {@link MimeTree}.
	 * 
	 * @param uid
	 *            message UID
	 * @param folder
	 *            mailbox name
	 * @return mimetree
	 * @throws Exception
	 */
	public MimeTree getMimeTree(Object uid, IMAPFolder folder)
			throws IOException, IMAPException, CommandCancelledException;

	/**
	 * Get {@link MimePart}.
	 * 
	 * @param uid
	 *            message UID
	 * @param address
	 *            address of MimePart in MimeTree
	 * @param folder
	 *            mailbox name
	 * @return mimepart
	 * @throws CommandCancelledException
	 * @throws IMAPException
	 * @throws IOException
	 * @throws Exception
	 */
	public InputStream getMimePartBodyStream(Object uid, Integer[] address,
			IMAPFolder folder) throws IOException, IMAPException,
			CommandCancelledException;

	/**
	 * Get {@link MimePart}.
	 * 
	 * @param uid
	 *            message UID
	 * @param address
	 *            address of MimePart in MimeTree
	 * @param folder
	 *            mailbox name
	 * @return mimepart
	 * @throws CommandCancelledException
	 * @throws IMAPException
	 * @throws IOException
	 * @throws Exception
	 */
	public Header getHeaders(Object uid, String[] keys, IMAPFolder folder)
			throws IOException, IMAPException, CommandCancelledException;

	/**
	 * Get complete headers.
	 * 
	 * @param uid
	 *            message uid
	 * @param folder
	 *            mailbox path
	 * @return
	 * @throws Exception
	 * @throws CommandCancelledException
	 * @throws IMAPException
	 * @throws IOException
	 */
	public Header getAllHeaders(Object uid, IMAPFolder folder)
			throws IOException, IMAPException, CommandCancelledException;

	/**
	 * Get {@link MimePart}.
	 * 
	 * @param uid
	 *            message UID
	 * @param address
	 *            address of MimePart in MimeTree
	 * @param folder
	 *            mailbox name
	 * @return mimepart
	 * @throws CommandCancelledException
	 * @throws IMAPException
	 * @throws IOException
	 * @throws Exception
	 */
	public InputStream getMimePartSourceStream(Object uid, Integer[] address,
			IMAPFolder folder) throws IOException, IMAPException,
			CommandCancelledException;

	/**
	 * Get complete message source.
	 * 
	 * @param uid
	 *            message UID
	 * @param path
	 *            mailbox name
	 * @return message source
	 * @throws CommandCancelledException
	 * @throws IMAPException
	 * @throws IOException
	 * @throws Exception
	 */
	public InputStream getMessageSourceStream(Object uid, IMAPFolder folder)
			throws IOException, IMAPException, CommandCancelledException;

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
	 * @param folder
	 *            mailbox name
	 * @throws Exception
	 */
	public void markMessage(Object[] uids, int variant, IMAPFolder folder)
			throws IOException, IMAPException, CommandCancelledException;

	public void setFlags(Object[] uids, IMAPFlags flags, IMAPFolder folder)
			throws IOException, IMAPException, CommandCancelledException;

	/**
	 * Search messages.
	 * 
	 * @param uids
	 *            message UIDs
	 * @param filterRule
	 *            filter rules
	 * @param folder
	 *            mailbox name
	 * @return list of UIDs which match filter rules
	 * @throws Exception
	 */
	public List search(Object[] uids, FilterRule filterRule, IMAPFolder folder)
			throws Exception;

	public int getIndex(Integer uid, IMAPFolder folder) throws IOException,
			IMAPException, CommandCancelledException;

	public Integer[] search(SearchKey key, IMAPFolder folder)
			throws IOException, IMAPException, CommandCancelledException;

	/**
	 * 
	 * @param filterRule
	 * @param folder
	 * @return
	 * @throws Exception
	 * @throws CommandCancelledException
	 * @throws IMAPException
	 * @throws IOException
	 */
	public List search(FilterRule filterRule, IMAPFolder folder)
			throws IOException, IMAPException, CommandCancelledException;

	/**
	 * @param folder
	 *         
	 * @return
	 * @throws CommandCancelledException
	 * @throws IMAPException
	 * @throws IOException
	 */
	public MailboxInfo getMessageFolderInfo(IMAPFolder folder)
			throws IOException, IMAPException, CommandCancelledException;

	/**
	 * @return
	 */
	public ListInfo[] fetchSubscribedFolders() throws IOException,
			IMAPException, CommandCancelledException;

	/**
	 * @param imapPath
	 * @return
	 * @throws IOException
	 * @throws CommandCancelledException
	 * @throws IMAPException
	 */
	public boolean isSelected(IMAPFolder folder) throws IOException,
			IMAPException, CommandCancelledException;

	/**
	 * @see org.columba.ristretto.imap.IMAPListener#alertMessage(java.lang.String)
	 */
	public void alertMessage(String arg0);

	/**
	 * @see org.columba.ristretto.imap.IMAPListener#connectionClosed(java.lang.String,
	 *      java.lang.String)
	 */
	public void connectionClosed(String arg0, String arg1);

	/**
	 * @see org.columba.ristretto.imap.IMAPListener#existsChanged(java.lang.String,
	 *      int)
	 */
	public void existsChanged(String arg0, int arg1);

	/**
	 * @see org.columba.ristretto.imap.IMAPListener#flagsChanged(java.lang.String,
	 *      org.columba.ristretto.imap.IMAPFlags)
	 */
	public void flagsChanged(String arg0, IMAPFlags arg1);

	/**
	 * @see org.columba.ristretto.imap.IMAPListener#parseError(java.lang.String)
	 */
	public void parseError(String arg0);

	/**
	 * @see org.columba.ristretto.imap.IMAPListener#recentChanged(java.lang.String,
	 *      int)
	 */
	public void recentChanged(String arg0, int arg1);

	/**
	 * @see org.columba.ristretto.imap.IMAPListener#warningMessage(java.lang.String)
	 */
	public void warningMessage(String arg0);

	/**
	 * @return Returns the item.
	 */
	public ImapItem getItem();

	public void update(Observable o, Object arg);

	/**
	 * @param existsChangedAction The existsChangedAction to set.
	 */
	public void setExistsChangedAction(IExistsChangedAction existsChangedAction);

	/**
	 * @param updateFlagAction The updateFlagAction to set.
	 */
	public void setUpdateFlagAction(IUpdateFlagAction updateFlagAction);

	/**
	 * @param observable The observable to set.
	 */
	public void setObservable(IStatusObservable observable);

	
	public int getLargestRemoteUid(IMAPFolder folder) throws IOException, IMAPException, CommandCancelledException;
	
}