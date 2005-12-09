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

package org.columba.mail.folder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.columba.core.base.ListTools;
import org.columba.core.filter.FilterList;
import org.columba.core.io.DiskIO;
import org.columba.core.xml.XmlElement;
import org.columba.mail.config.FolderItem;
import org.columba.mail.folder.headercache.BerkeleyDBHeaderList;
import org.columba.mail.folder.headercache.CachedHeaderfields;
import org.columba.mail.folder.headercache.IHeaderListCorruptedListener;
import org.columba.mail.folder.headercache.SyncHeaderList;
import org.columba.mail.folder.search.DefaultSearchEngine;
import org.columba.mail.message.ColumbaHeader;
import org.columba.mail.message.ColumbaMessage;
import org.columba.mail.message.IColumbaHeader;
import org.columba.mail.message.IColumbaMessage;
import org.columba.mail.message.IHeaderList;
import org.columba.mail.message.IPersistantHeaderList;
import org.columba.ristretto.io.Source;
import org.columba.ristretto.io.SourceInputStream;
import org.columba.ristretto.message.Attributes;
import org.columba.ristretto.message.Flags;
import org.columba.ristretto.message.Header;
import org.columba.ristretto.message.LocalMimePart;
import org.columba.ristretto.message.MimeTree;
import org.columba.ristretto.parser.HeaderParser;
import org.columba.ristretto.parser.MessageParser;
import org.columba.ristretto.parser.ParserException;

/**
 * AbstractLocalFolder is a near-to working folder, which only needs a specific
 * {@link IDataStorage},{@link DefaultSearchEngine}and
 * {@link IHeaderListStorage}"plugged in" to make it work.
 * <p>
 * This class is abstract becaused, instead use {@link MHCachedFolder}a
 * complete implementation.
 * <p>
 * AbstractLocalFolder uses an internal {@link ColumbaMessage}object as cache. This
 * allows parsing of a message only once, while accessing the data of the
 * message multiple times.
 * <p>
 * Attribute <code>nextMessageUid</code> handles the next unique message ID.
 * When adding a new message to this folder, it gets this ID assigned for later
 * reference. Then nextMessageUid is simply increased.
 * <p>
 * 
 * @see org.columba.mail.folder.mh.MHCachedFolder
 * 
 * @author fdietz
 */
public abstract class AbstractLocalFolder extends AbstractMessageFolder {

	/** JDK 1.4+ logging framework logger, used for logging. */
	private static final Logger LOG = Logger
			.getLogger("org.columba.mail.folder");

	/**
	 * the next messag which gets added to this folder receives this unique ID
	 */
	protected int nextMessageUid;

	/**
	 * we keep one message in cache in order to not needing to parse it twice
	 * times
	 */
	protected ColumbaMessage aktMessage;

	private boolean firstOpen = true;
	
	/**
	 * implement your own mailbox format here
	 */

	protected IDataStorage dataStorage;

	protected IPersistantHeaderList headerList;

	/**
	 * @param item
	 *            <class>FolderItem </class> contains xml configuration of this
	 *            folder
	 */
	public AbstractLocalFolder(FolderItem item, String path) {
		super(item, path);

		// TODO (@author fdietz): move this to AbstractMessageFolder constructor
		// create filterlist datastructure
		XmlElement filterListElement = node.getElement(FilterList.XML_NAME);

		if (filterListElement == null) {
			// no filterlist treenode found
			// -> create a new one
			filterListElement = new XmlElement(FilterList.XML_NAME);
			getConfiguration().getRoot().addElement(filterListElement);
		}

		filterList = new FilterList(filterListElement);
		
        //headerList = new PersistantHeaderList(new LocalHeaderCache(this));
		headerList = new BerkeleyDBHeaderList(new File(this.getDirectoryFile(),"headerlist"));
		final AbstractMessageFolder folder = this;		
		headerList.addHeaderListCorruptedListener(new IHeaderListCorruptedListener() {

			public void headerListCorrupted(IHeaderList headerList) {
				try {
					SyncHeaderList.sync(folder, headerList);
				} catch (IOException e) {
					LOG.severe(e.getMessage());
				}
			}});
        
        setSearchEngine(new DefaultSearchEngine(this));        
	}

	/**
	 * @param name
	 *            the name of the folder.
	 * @param type
	 *            type of folder.
	 */
	public AbstractLocalFolder(String name, String type, String path) {
		super(name, type, path);

		// create filterlist datastructure
		XmlElement filterListElement = node.getElement(FilterList.XML_NAME);

		if (filterListElement == null) {
			// no filterlist treenode found
			// -> create a new one
			filterListElement = new XmlElement(FilterList.XML_NAME);
			getConfiguration().getRoot().addElement(filterListElement);
		}

		filterList = new FilterList(filterListElement);

        //headerList = new PersistantHeaderList(new LocalHeaderCache(this));	
		headerList = new BerkeleyDBHeaderList(new File(this.getDirectoryFile(),"headerlist"));
		final AbstractMessageFolder folder = this;		
		headerList.addHeaderListCorruptedListener(new IHeaderListCorruptedListener() {

			public void headerListCorrupted(IHeaderList headerList) {
				try {
					SyncHeaderList.sync(folder, headerList);
				} catch (IOException e) {
					LOG.severe(e.getMessage());
				}
			}});

        setSearchEngine(new DefaultSearchEngine(this));        	
	}

	/**
	 * Remove folder from tree
	 * 
	 * @see org.columba.mail.folder.FolderTreeNode#removeFolder()
	 */
	public void removeFolder() throws Exception {
		// delete folder from your harddrive
		boolean b = DiskIO.deleteDirectory(directoryFile);

		// if this worked, remove it from tree.xml configuration, too
		if (b) {
			super.removeFolder();
		}
	}

	/**
	 * 
	 * Generate new unique message ID
	 * 
	 * @return <class>Integer </class> containing UID
	 */
	protected Object generateNextMessageUid() {
		return new Integer(nextMessageUid++);
	}

	/**
	 * 
	 * Set next unique message ID
	 * 
	 * @param next
	 *            number of next message
	 */
	public void setNextMessageUid(int next) {
		nextMessageUid = next;
	}

	/**
	 * 
	 * Implement a <class>IDataStorage </class> for the mailbox format
	 * of your pleasure.
	 * 
	 * @return instance of <class>IDataStorage </class>
	 */
	public abstract IDataStorage getDataStorageInstance();

	/**
	 * @see org.columba.mail.folder.IMailbox#getMimePartTree(java.lang.Object)
	 */
	public MimeTree getMimePartTree(Object uid) throws Exception {
		// get message with UID
		IColumbaMessage message = getMessage(uid);

		// get tree-like structure of mimeparts
		MimeTree mptree = message.getMimePartTree();

		return mptree;
	}

	/** {@inheritDoc} */
	public InputStream getMessageSourceStream(Object uid) throws Exception {
		return getDataStorageInstance().getMessageStream(uid);
	}

	/** {@inheritDoc} */
	public InputStream getMimePartBodyStream(Object uid, Integer[] address)
			throws Exception {
		// get message with UID
		IColumbaMessage message = getMessage(uid);

		// Get the mimepart
		LocalMimePart mimepart = (LocalMimePart) message.getMimePartTree()
				.getFromAddress(address);

		return mimepart.getInputStream();
	}

	/** {@inheritDoc} */
	public InputStream getMimePartSourceStream(Object uid, Integer[] address)
			throws Exception {
		// get message with UID
		IColumbaMessage message = getMessage(uid);

		// Get the mimepart
		LocalMimePart mimepart = (LocalMimePart) message.getMimePartTree()
				.getFromAddress(address);

		return new SourceInputStream(mimepart.getSource());
	}

	/**
	 * Copies a set of messages from this folder to a destination folder.
	 * <p>
	 * First we copy the message source to the destination folder. Then we also
	 * copy the flags attribute of this message.
	 * 
	 * @see org.columba.mail.folder.IMailbox#innerCopy(org.columba.mail.folder.IMailbox,
	 *      java.lang.Object[])
	 */
	public void innerCopy(IMailbox destFolder, Object[] uids)
			throws Exception {
		if (getObservable() != null) {
			getObservable().setMax(uids.length);
		}

		for (int i = 0; i < uids.length; i++) {
			// skip this message, if it doesn't exist in source folder
			if (!exists(uids[i])) {
				continue;
			}

			InputStream messageSourceStream = getMessageSourceStream(uids[i]);
			destFolder.addMessage(messageSourceStream,getAttributes(uids[i]), getFlags(uids[i]));
			messageSourceStream.close();

			/*
			 * ((AbstractLocalFolder) destFolder).setFlags(destuid, (Flags) getFlags(
			 * uids[i]).clone());
			 */
			//destFolder.fireMessageAdded(uids[i]);
			if (getObservable() != null) {
				getObservable().setCurrent(i);
			}
		}

		// we are done - clear the progress bar
		if (getObservable() != null) {
			getObservable().resetCurrent();
		}
	}

	public Object addMessage(InputStream in) throws Exception {
		return addMessage(in, null, null);
	}

	public Object addMessage(InputStream in, Attributes attributes, Flags flags)
			throws Exception {
		// generate UID for new message
		Object newUid = generateNextMessageUid();

		// save message stream to file
		getDataStorageInstance().saveMessage(newUid, in);

		// close stream
		in.close();

		// parse header
		Source source = getDataStorageInstance().getMessageSource(newUid);
		int messageSize = source.length();		
		
		Header header = HeaderParser.parse(source);
		ColumbaHeader h;
		if ((attributes != null) && (flags != null)) {
			// save header and attributes. Copy the flags!
			h = new ColumbaHeader( header, (Attributes)attributes.clone(), new Flags(flags.getFlags()));
		} else {
			h = new ColumbaHeader(header);
			h.set("columba.size",new Integer(messageSize / 1024));
		}
		source.close();
		h.set("columba.uid", newUid);
		getHeaderList().add(h,newUid);

		fireMessageAdded(newUid, getFlags(newUid));
		return newUid;
	}

	/** {@inheritDoc} */
	public boolean isInboxFolder() {
		return getUid() == 101;
	}

	/** {@inheritDoc} */
	public boolean isTrashFolder() {
		return getUid() == 105;
	}

	/** {@inheritDoc} */
	public boolean supportsAddFolder(String newFolderType) {
		return (FolderFactory.getInstance().getGroup(newFolderType).equals("local") || newFolderType.equals("VirtualFolder"));
	}

	/**
	 * Returns true since local folders can be moved.
	 * 
	 * @return true.
	 */
	public boolean supportsMove() {
		return true;
	}

	/**
	 * @param uid
	 * @return @throws
	 *         Exception
	 */
	protected ColumbaMessage getMessage(Object uid) throws Exception {
		//Check if the message is already cached
		if (aktMessage != null) {
			if (aktMessage.getUID().equals(uid)) {
				// this message is already cached
				return aktMessage;
			}
		}

		ColumbaMessage message;

		try {

			Source source = null;

			source = getDataStorageInstance().getMessageSource(uid);

			// Parse Message from DataStorage
			try {
				message = new ColumbaMessage(MessageParser.parse(source));
			} catch (ParserException e1) {
				LOG.fine(e1.getSource().toString());
				throw e1;
			}
			source.close();

			message.setUID(uid);

			aktMessage = message;

			// TODO: fix parser exception
		} catch (FolderInconsistentException e) {
			// update message folder info
			Flags flags = getFlags(uid);

			if (flags.getSeen()) {
				getMessageFolderInfo().decUnseen();
			}

			if (flags.getRecent()) {
				getMessageFolderInfo().decRecent();
			}

			// remove message from headercache
			getHeaderList().remove(uid);

			throw e;
		}

		//We use the attributes and flags from the cache
		//but the parsed header from the parsed message
		IColumbaHeader header = getHeaderList().get(uid);
		header.setHeader(message.getHeader().getHeader());
		message.setHeader(header);

		return message;
	}

	/**
	 * @see org.columba.mail.folder.IMailbox#getMessageHeader(java.lang.Object)
	 * @TODO dont use deprecated method
	 */
	protected IColumbaHeader getMessageHeader(Object uid) throws Exception {
		if ((aktMessage != null) && (aktMessage.getUID().equals(uid))) {
			// message is already cached
			// try to compare the headerfield count of
			// the actually parsed message with the cached
			// headerfield count
			IColumbaMessage message = getMessage(uid);

			// number of headerfields
			int size = message.getHeader().count();

			// get header from cache
			IColumbaHeader h = getHeaderList().get(uid);

			// message doesn't exist (this shouldn't happen here)
			if (h == null) {
				return null;
			}

			// number of headerfields
			int cachedSize = h.count();

			// if header contains more fields than the cached header
			if (size > cachedSize) {
				return (ColumbaHeader) message.getHeader();
			}

			return (ColumbaHeader) h;
		} else {
			// message isn't cached
			// -> just return header from cache
			return getHeaderList().get(uid);
		}
	}

	/**
	 * @see org.columba.mail.folder.IMailbox#remove(java.lang.Object)
	 */
	public void removeMessage(Object uid) throws Exception {
		// remove message from disk
		getDataStorageInstance().removeMessage(uid);

		//fireMessageRemoved(uid, getFlags(uid));
		super.removeMessage(uid);

	}

	/**
	 * Changes the selected message flags and updates the {@link MailFolderInfo}
	 * accordingly.
	 * <p>
	 * This method is only used for innerCopy().
	 * 
	 * @param uid
	 *            selected message UID
	 * @param flags
	 *            new flags
	 * @throws Exception
	 */
	protected void setFlags(Object uid, Flags flags) throws Exception {
		IColumbaHeader h = getHeaderList().get(uid);

		Flags oldFlags = h.getFlags();
		h.setFlags(flags);

		// update MessageFolderInfo
		if (oldFlags.get(Flags.RECENT) && !flags.get(Flags.RECENT)) {
			getMessageFolderInfo().decRecent();
		}

		if (!oldFlags.get(Flags.RECENT) && flags.get(Flags.RECENT)) {
			getMessageFolderInfo().incRecent();
		}

		if (oldFlags.get(Flags.SEEN) && !flags.get(Flags.SEEN)) {
			getMessageFolderInfo().incUnseen();
		}

		if (!oldFlags.get(Flags.SEEN) && flags.get(Flags.SEEN)) {
			getMessageFolderInfo().decUnseen();
		}
	}

	/**
	 * This method first tries to find the requested header in the header cache.
	 * If the headerfield is not cached, the message source is parsed.
	 * 
	 * @see org.columba.mail.folder.IMailbox#getHeaderFields(java.lang.Object,
	 *      java.lang.String[])
	 *  
	 */
	public Header getHeaderFields(Object uid, String[] keys) throws Exception {
		// cached headerfield list
		List cachedList = Arrays.asList(CachedHeaderfields
				.getDefaultHeaderfields());

		LinkedList keyList = new LinkedList(Arrays.asList(keys));

		ListTools.substract(keyList, cachedList);

		if (keyList.size() == 0) {
			return getHeaderList().get(uid).getHeader();
		} else {
			// We need to parse
			// get message with UID
			IColumbaMessage message = getMessage(uid);

			Header header = message.getHeader().getHeader();

			Header subHeader = new Header();
			String value;

			for (int i = 0; i < keys.length; i++) {
				value = header.get(keys[i]);

				if (value != null) {
					subHeader.set(keys[i], value);
				}
			}

			return subHeader;
		}
	}

	/**
	 * @see org.columba.mail.folder.IMailbox#expungeFolder()
	 */
	public void expungeFolder() throws Exception {
		// make sure to close all file handles
		// to the currently cached message
		// -> necessary for windows to be able to delete the local file
		if (aktMessage != null) {
			aktMessage.close();
			aktMessage = null;
		}

		super.expungeFolder();
	}

	/**
	 * @see org.columba.mail.folder.IMailbox#getAllHeaderFields(java.lang.Object)
	 */
	public Header getAllHeaderFields(Object uid) throws Exception {

		IColumbaMessage message = getMessage(uid);

		Header header = message.getHeader().getHeader();

		return header;
	}

	public IHeaderList getHeaderList() throws Exception {
		if( firstOpen ) {
		
			if( headerList.count() != getDataStorageInstance().getMessageCount()) {
				// 	Must be out of sync!
				SyncHeaderList.sync(this, headerList);
			}
			
			firstOpen = false;
		}
	
		return headerList;
	}

	/**
	 * @see org.columba.mail.folder.AbstractMessageFolder#save()
	 */
	public void save() throws Exception {
		super.save();
		headerList.persist();
	}
}