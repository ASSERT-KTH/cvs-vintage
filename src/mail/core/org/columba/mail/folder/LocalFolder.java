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

import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.columba.core.io.DiskIO;
import org.columba.core.util.ListTools;
import org.columba.core.xml.XmlElement;
import org.columba.mail.config.FolderItem;
import org.columba.mail.filter.FilterList;
import org.columba.mail.folder.headercache.CachedHeaderfields;
import org.columba.mail.folder.virtual.VirtualFolder;
import org.columba.mail.message.ColumbaHeader;
import org.columba.mail.message.ColumbaMessage;
import org.columba.ristretto.message.*;
import org.columba.ristretto.message.io.Source;
import org.columba.ristretto.message.io.SourceInputStream;
import org.columba.ristretto.parser.HeaderParser;
import org.columba.ristretto.parser.MessageParser;
import org.columba.ristretto.parser.ParserException;

/**
 * LocalFolder is a near-to working folder, which only needs a specific
 * {@link DataStorageInterface},{@link DefaultSearchEngine}and
 * {@link HeaderListStorage}"plugged in" to make it work.
 * <p>
 * This class is abstract becaused, instead use {@link MHCachedFolder}a
 * complete implementation.
 * <p>
 * LocalFolder uses an internal {@link ColumbaMessage}object as cache. This
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
public abstract class LocalFolder extends Folder implements MailboxInterface {

    /** JDK 1.4+ logging framework logger, used for logging. */
    private static final Logger LOG = Logger.getLogger("org.columba.mail.folder");

    /**
     * the next messag which gets added to this folder receives this unique ID
     */
    protected int nextMessageUid;

    /**
     * we keep one message in cache in order to not needing to parse it twice
     * times
     */
    protected ColumbaMessage aktMessage;

    /**
     * implement your own mailbox format here
     */

    protected DataStorageInterface dataStorage;

    /**
     * @param item <class>FolderItem</class> contains xml configuration of this
     *            folder
     */
    public LocalFolder(FolderItem item, String path) {
        super(item, path);

        // TODO: move this to Folder constructor
        // create filterlist datastructure
        XmlElement filterListElement = node.getElement(FilterList.XML_NAME);

        if (filterListElement == null) {
            // no filterlist treenode found
            // -> create a new one
            filterListElement = new XmlElement(FilterList.XML_NAME);
            getFolderItem().getRoot().addElement(filterListElement);
        }

        filterList = new FilterList(filterListElement);
    }

    // constructor

    /**
     * @param name the name of the folder.
     * @param type type of folder.
     */
    public LocalFolder(String name, String type, String path) {
        super(name, type, path);

        // create filterlist datastructure
        XmlElement filterListElement = node.getElement(FilterList.XML_NAME);

        if (filterListElement == null) {
            // no filterlist treenode found
            // -> create a new one
            filterListElement = new XmlElement(FilterList.XML_NAME);
            getFolderItem().getRoot().addElement(filterListElement);
        }

        filterList = new FilterList(filterListElement);
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
     * @param next number of next message
     */
    public void setNextMessageUid(int next) {
        nextMessageUid = next;
    }

    /**
     *
     * Implement a <class>DataStorageInterface </class> for the mailbox format
     * of your pleasure.
     *
     * @return instance of <class>DataStorageInterface </class>
     */
    public abstract DataStorageInterface getDataStorageInstance();


    /**
     * @see org.columba.mail.folder.MailboxInterface#getMimePart(java.lang.Object, java.lang.Integer[])
     */
    public MimePart getMimePart(Object uid, Integer[] address) throws Exception {
        // get message with UID
        ColumbaMessage message = getMessage(uid);

        // get mimepart of message
        MimePart mimePart = message.getMimePartTree().getFromAddress(address);

        return mimePart;
    }


    /**
     * @see org.columba.mail.folder.MailboxInterface#getMimePartTree(java.lang.Object)
     */
    public MimeTree getMimePartTree(Object uid) throws Exception {
        // get message with UID
        ColumbaMessage message = getMessage(uid);

        // get tree-like structure of mimeparts
        MimeTree mptree = message.getMimePartTree();

        return mptree;
    }

    /** {@inheritDoc} */
    public void expungeFolder() throws Exception {
        if (aktMessage != null) {
            aktMessage.close();
            aktMessage = null;
        }

        // get list of all uids
        Object[] uids = getUids();

        for (int i = 0; i < uids.length; i++) {
            Object uid = uids[i];

            if (uid == null) {
                continue;
            }

            // if message with uid doesn't exist -> skip
            if (!exists(uid)) {
                LOG.info("uid " + uid + " doesn't exist");

                continue;
            }

            if (getFlags(uid).getExpunged()) {
                // move message to trash if marked as expunged
                LOG.info("removing uid=" + uid);

                // remove message
                removeMessage(uid);
            }
        }

        // folder was modified
        changed = true;
    }

    /** {@inheritDoc} */
    public InputStream getMessageSourceStream(Object uid) throws Exception {
        return new SourceInputStream(getDataStorageInstance().getMessageSource(
                uid));
    }

    /** {@inheritDoc} */
    public InputStream getMimePartBodyStream(Object uid, Integer[] address)
            throws Exception {
        // get message with UID
        ColumbaMessage message = getMessage(uid);

        // Get the mimepart
        LocalMimePart mimepart = (LocalMimePart) message.getMimePartTree()
                .getFromAddress(address);

        InputStream bodyStream = mimepart.getInputStream();
        MimeHeader header = mimepart.getHeader();

        bodyStream = decodeStream(header, bodyStream);

        return bodyStream;
    }

    /** {@inheritDoc} */
    public InputStream getMimePartSourceStream(Object uid, Integer[] address)
            throws Exception {
        // get message with UID
        ColumbaMessage message = getMessage(uid);

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
     * @see org.columba.mail.folder.MailboxInterface#innerCopy(org.columba.mail.folder.MailboxInterface,
     *      java.lang.Object[])
     */
    public void innerCopy(MailboxInterface destFolder, Object[] uids)
            throws Exception {
        if (getObservable() != null) {
            getObservable().setMax(uids.length);
        }

        for (int i = 0; i < uids.length; i++) {
            // skip this message, if it doesn't exist in source folder
            if (!exists(uids[i])) {
                continue;
            }

            Object destuid = destFolder.addMessage(
                    getMessageSourceStream(uids[i]), getAttributes(uids[i]));
            ((LocalFolder) destFolder).setFlags(destuid, (Flags) getFlags(
                    uids[i]).clone());

            if (getObservable() != null) {
                getObservable().setCurrent(i);
            }
        }

        // we are done - clear the progress bar
        if (getObservable() != null) {
            getObservable().resetCurrent();
        }
    }

    /** {@inheritDoc} */
    public Object addMessage(InputStream in) throws Exception {
        // increase total count of messages
        super.addMessage(in);

        // generate UID for new message
        Object newUid = generateNextMessageUid();

        // save message stream to file
        getDataStorageInstance().saveMessage(newUid, in);

        // close stream
        in.close();

        return newUid;
    }


    /**
     * @see org.columba.mail.folder.MailboxInterface#addMessage(java.io.InputStream, org.columba.ristretto.message.Attributes)
     */
    public Object addMessage(InputStream in, Attributes attributes)
            throws Exception {

        Object newUid = addMessage(in);

        if (newUid == null) { return null; }

        Source source = getDataStorageInstance().getMessageSource(newUid);

        // parse header
        Header header = HeaderParser.parse(source);

        // save header and attributes
        getHeaderListStorage().addMessage(newUid, header, attributes);

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
    public boolean supportsAddFolder(FolderTreeNode newFolder) {
        return ((newFolder instanceof LocalFolder) || (newFolder instanceof VirtualFolder));
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
     * @return
     * @throws Exception
     */
    protected ColumbaMessage getMessage(Object uid) throws Exception {
        //Check if the message is already cached
        if (aktMessage != null) {
            if (aktMessage.getUID().equals(uid)) {
            // this message is already cached
            return aktMessage; }
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
        ColumbaHeader header = (ColumbaHeader) getHeaderListStorage()
                .getHeaderList().get(uid);
        header.setHeader(message.getHeader().getHeader());
        message.setHeader(header);

        return message;
    }

    /**
     * @see org.columba.mail.folder.MailboxInterface#getMessageHeader(java.lang.Object)
     */
    public ColumbaHeader getMessageHeader(Object uid) throws Exception {
        if ((aktMessage != null) && (aktMessage.getUID().equals(uid))) {
            // message is already cached
            // try to compare the headerfield count of
            // the actually parsed message with the cached
            // headerfield count
            ColumbaMessage message = getMessage(uid);

            // number of headerfields
            int size = message.getHeader().count();

            // get header from cache
            ColumbaHeader h = (ColumbaHeader) getHeaderListStorage()
                    .getHeaderList().get(uid);

            // message doesn't exist (this shouldn't happen here)
            if (h == null) { return null; }

            // number of headerfields
            int cachedSize = h.count();

            // if header contains more fields than the cached header
            if (size > cachedSize) { return (ColumbaHeader) message.getHeader(); }

            return (ColumbaHeader) h;
        } else {
            // message isn't cached
            // -> just return header from cache
            return (ColumbaHeader) getHeaderListStorage().getHeaderList().get(
                    uid);
        }
    }

    /**
     * @see org.columba.mail.folder.MailboxInterface#removeMessage(java.lang.Object)
     */
    public void removeMessage(Object uid) throws Exception {
        super.removeMessage(uid);

        // remove message from disk
        getDataStorageInstance().removeMessage(uid);

        // this folder was modified
        changed = true;
    }

    /**
     * @see org.columba.mail.folder.Folder#save()
     */
    public void save() throws Exception {
        // only save header-cache if folder data changed
        if (hasChanged()) {
            getHeaderListStorage().save();
            setChanged(false);
        }

        // call Folder.save() to be sure that messagefolderinfo is saved
        super.save();
    }

    /**
     * @param uid
     * @param flags
     * @throws Exception
     */
    protected void setFlags(Object uid, Flags flags) throws Exception {
        ColumbaHeader h = (ColumbaHeader) getHeaderListStorage()
                .getHeaderList().get(uid);

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
     * This method first tries to find the requested header in the header
     * cache. If the headerfield is not cached, the message source is parsed.
     *
     * @see org.columba.mail.folder.MailboxInterface#getHeaderFields(java.lang.Object, java.lang.String[])
     *
     */
    public Header getHeaderFields(Object uid, String[] keys) throws Exception {
        // cached headerfield list
        List cachedList = Arrays.asList(CachedHeaderfields
                .getCachedHeaderfields());

        LinkedList keyList = new LinkedList(Arrays.asList(keys));

        ListTools.substract(keyList, cachedList);

        if (keyList.size() == 0) {
            return getHeaderListStorage().getHeaderFields(uid, keys);
        } else {
            // We need to parse
            // get message with UID
            ColumbaMessage message = getMessage(uid);

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
}
