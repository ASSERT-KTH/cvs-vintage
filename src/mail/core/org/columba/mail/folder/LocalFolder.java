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

import org.columba.core.io.DiskIO;
import org.columba.core.logging.ColumbaLogger;
import org.columba.core.xml.XmlElement;

import org.columba.mail.config.FolderItem;
import org.columba.mail.filter.Filter;
import org.columba.mail.filter.FilterList;
import org.columba.mail.folder.search.DefaultSearchEngine;
import org.columba.mail.folder.search.LuceneQueryEngine;
import org.columba.mail.folder.virtual.VirtualFolder;
import org.columba.mail.message.ColumbaHeader;
import org.columba.mail.message.ColumbaMessage;

import org.columba.ristretto.coder.Base64DecoderInputStream;
import org.columba.ristretto.coder.CharsetDecoderInputStream;
import org.columba.ristretto.coder.QuotedPrintableDecoderInputStream;
import org.columba.ristretto.message.Flags;
import org.columba.ristretto.message.Header;
import org.columba.ristretto.message.LocalMimePart;
import org.columba.ristretto.message.Message;
import org.columba.ristretto.message.MimeHeader;
import org.columba.ristretto.message.MimePart;
import org.columba.ristretto.message.MimeTree;
import org.columba.ristretto.message.io.CharSequenceSource;
import org.columba.ristretto.message.io.Source;
import org.columba.ristretto.message.io.SourceInputStream;
import org.columba.ristretto.parser.MessageParser;
import org.columba.ristretto.parser.ParserException;

import java.io.IOException;
import java.io.InputStream;

import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;


/**
 * @author fdietz
 *
 * <class>LocalFolder</class> is a near-working folder,
 * which only needs a specific datastorage and
 * search-engine "plugged in" to make it work.
 *
 * This class is abstract becaused we use
 * <class>CachedLocalFolder</class> instead which
 * contains a header-cache facility which
 * Columba needs to be able to quickly show
 * a message summary, etc.
 *
 */
public abstract class LocalFolder extends Folder implements MailboxInterface {
    // the next messag which gets added to this folder
    // receives this unique ID
    protected int nextMessageUid;

    // we keep one message in cache in order to not
    // needing to parse it twice times
    protected ColumbaMessage aktMessage;

    // implement your own mailbox format here
    protected DataStorageInterface dataStorage;

    // implement your own search-engine here
    protected DefaultSearchEngine searchEngine;

    /**
 * @param item        <class>FolderItem</class> contains xml configuration of this folder
 */
    public LocalFolder(FolderItem item, String path) {
        super(item, path);

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
 * @return        <class>Integer</class> containing UID
 */
    protected Object generateNextMessageUid() {
        return new Integer(nextMessageUid++);
    }

    /**
 *
 * Set next unique message ID
 *
 * @param next                number of next message
 */
    public void setNextMessageUid(int next) {
        nextMessageUid = next;
    }

    /**
 *
 * Implement a <class>DataStorageInterface</class> for the
 * mailbox format of your pleasure
 *
 * @return        instance of <class>DataStorageInterface</class>
 */
    public abstract DataStorageInterface getDataStorageInstance();

    /**

 *
 * @see org.columba.mail.folder.Folder#exists(java.lang.Object, org.columba.core.command.WorkerStatusController)
 */
    public boolean exists(Object uid) throws Exception {
        return getDataStorageInstance().exists(uid);
    }

    /**

 *
 * @see org.columba.mail.folder.Folder#addMessage(org.columba.mail.message.AbstractMessage, org.columba.core.command.WorkerStatusController)
 */
    public Object addMessage(ColumbaMessage message) throws Exception {
        if (message == null) {
            return null;
        }

        // load headerlist before adding a message
        getHeaderList();

        // generate UID for new message
        Object newUid = generateNextMessageUid();

        // apply UID for message
        message.setUID(newUid);

        ColumbaLogger.log.info("new UID=" + newUid);

        // get message source
        Source source = message.getSource();

        if (source == null) {
            System.out.println("source is null " + newUid);

            return null;
        }

        // save message to disk
        getDataStorageInstance().saveMessage(newUid,
            new SourceInputStream(source));

        // increase total count of messages
        getMessageFolderInfo().incExists();

        // notify search-engine
        getSearchEngineInstance().messageAdded(message);

        // this folder has changed
        changed = true;

        // free memory
        // -> we don't need the message object anymore
        message.freeMemory();

        return newUid;
    }

    /**
 * @see org.columba.mail.folder.Folder#addMessage(java.lang.String, org.columba.core.command.WorkerStatusController)
 */
    public Object addMessage(String source) throws Exception {
        Message message = MessageParser.parse(new CharSequenceSource(source));

        // generate message object
        ColumbaMessage m = new ColumbaMessage(message);

        // this folder was modified
        changed = true;

        return addMessage(m);
    }

    /**
 * @see org.columba.mail.folder.Folder#removeMessage(java.lang.Object, org.columba.core.command.WorkerStatusController)
 */
    public void removeMessage(Object uid) throws Exception {
        // remove message from disk
        getDataStorageInstance().removeMessage(uid);

        // notify search-engine
        getSearchEngineInstance().messageRemoved(uid);

        // decrement total count of message
        getMessageFolderInfo().decExists();

        // this folder was modified
        changed = true;
    }

    /**
 *
 * Return message with certain UID
 *
 *
 * @param uid                        unique message ID
 * @return                                a message object referring to this UID
 * @throws Exception        <class>Exception</class>
 */
    protected ColumbaMessage getMessage(Object uid) throws Exception {
        //Check if the message is already cached
        if (aktMessage != null) {
            if (aktMessage.getUID().equals(uid)) {
                // this message is already cached
                return aktMessage;
            }
        }

        //Parse Message from DataStorage
        Source source = null;

        try {
            source = getDataStorageInstance().getMessageSource(uid);
        } catch (IOException e) {
            // File is no longer present -> someone else deleted it
            // from the file system
            // notify search-engine
            getSearchEngineInstance().messageRemoved(uid);

            // decrement total count of message
            getMessageFolderInfo().decExists();

            // this folder was modified
            changed = true;

            throw new FolderInconsistentException(e);
        }

        ColumbaMessage message;

        try {
            message = new ColumbaMessage(MessageParser.parse(source));
        } catch (ParserException e1) {
            ColumbaLogger.log.fine(e1.getSource().toString());
            throw e1;
        }

        message.setUID(uid);

        aktMessage = message;

        return message;
    }

    /**
 * @see org.columba.mail.folder.Folder#getMimePart(java.lang.Object, java.lang.Integer[], org.columba.core.command.WorkerStatusController)
 */
    public MimePart getMimePart(Object uid, Integer[] address)
        throws Exception {
        // get message with UID
        ColumbaMessage message = getMessage(uid);

        // get mimepart of message
        MimePart mimePart = message.getMimePartTree().getFromAddress(address);

        return mimePart;
    }

    /**
 * @see org.columba.mail.folder.Folder#getMessageHeader(java.lang.Object, org.columba.core.command.WorkerStatusController)
 */
    public ColumbaHeader getMessageHeader(Object uid) throws Exception {
        // get message with UID
        ColumbaMessage message = getMessage(uid);

        // get header of message
        ColumbaHeader header = (ColumbaHeader) message.getHeader();

        return header;
    }

    /* (non-Javadoc)
 * @see org.columba.mail.folder.Folder#getMimePartTree(java.lang.Object, org.columba.core.command.WorkerStatusController)
 */
    public MimeTree getMimePartTree(Object uid) throws Exception {
        // get message with UID
        ColumbaMessage message = getMessage(uid);

        // get tree-like structure of mimeparts
        MimeTree mptree = message.getMimePartTree();

        return mptree;
    }

    /********************** searching/filtering ***********************/
    /**
* @return                instance of search-engine implementation
*/
    public DefaultSearchEngine getSearchEngineInstance() {
        // only use lucene backend if specified in tree.xml
        if (searchEngine == null) {
            boolean enableLucene = getFolderItem().getBoolean("property",
                    "enable_lucene", false);

            searchEngine = new DefaultSearchEngine(this);

            if (enableLucene) {
                searchEngine.setNonDefaultEngine(new LuceneQueryEngine(this));
            }
        }

        return searchEngine;
    }

    /**
 * Set new search engine
 *
 * @see org.columba.mail.folder.search
 *
 * @param engine                new search engine
 */
    public void setSearchEngine(DefaultSearchEngine engine) {
        this.searchEngine = engine;
    }

    /**
 * @see org.columba.mail.folder.Folder#searchMessages(org.columba.mail.filter.Filter, java.lang.Object[], org.columba.core.command.WorkerStatusController)
 */
    public Object[] searchMessages(Filter filter, Object[] uids)
        throws Exception {
        return getSearchEngineInstance().searchMessages(filter, uids);
    }

    /**
 * @see org.columba.mail.folder.Folder#searchMessages(org.columba.mail.filter.Filter, org.columba.core.command.WorkerStatusController)
 */
    public Object[] searchMessages(Filter filter) throws Exception {
        return getSearchEngineInstance().searchMessages(filter);
    }

    /**
 * @see org.columba.mail.folder.Folder#size()
 */
    public int size() {
        // return number of messages
        return getDataStorageInstance().getMessageCount();
    }

    /** {@inheritDoc} */
    public void expungeFolder() throws Exception {
    	if( aktMessage != null ) {
    		aktMessage.close();
        	aktMessage = null;
    	}
    }

    /*
        public Flags getFlags(Object uid) throws Exception {
                // get message with UID
                ColumbaMessage message = getMessage(uid);

                return message.getFlags();
        }
*/

    /** {@inheritDoc} */
    public Header getHeaderFields(Object uid, String[] keys)
        throws Exception {
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

    /** {@inheritDoc} */
    public InputStream getMessageSourceStream(Object uid)
        throws Exception {
        return new SourceInputStream(getDataStorageInstance().getMessageSource(uid));
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

    private InputStream decodeStream(MimeHeader header, InputStream bodyStream) {
        String charsetName = header.getContentParameter("charset");
        int encoding = header.getContentTransferEncoding();

        switch (encoding) {
        case MimeHeader.QUOTED_PRINTABLE: {
            bodyStream = new QuotedPrintableDecoderInputStream(bodyStream);

            break;
        }

        case MimeHeader.BASE64: {
            bodyStream = new Base64DecoderInputStream(bodyStream);

            break;
        }
        }

        if (charsetName != null) {
            Charset charset;

            try {
                charset = Charset.forName(charsetName);
            } catch (UnsupportedCharsetException e) {
                charset = Charset.forName(System.getProperty("file.encoding"));
            }

            bodyStream = new CharsetDecoderInputStream(bodyStream, charset);
        }

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
 * First we copy the message source to the destination folder. Then we
 * also copy the flags attribute of this message.
 *
 * @see org.columba.mail.folder.MailboxInterface#innerCopy(org.columba.mail.folder.MailboxInterface, java.lang.Object[])
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

            Object destuid = destFolder.addMessage(getMessageSourceStream(
                        uids[i]), getAttributes(uids[i]));
            ((LocalFolder) destFolder).setFlags(destuid,
                (Flags) getFlags(uids[i]).clone());

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
    public abstract void markMessage(Object[] uids, int variant)
        throws Exception;

    /** {@inheritDoc} */
    public Object addMessage(InputStream in) throws Exception {
        // generate UID for new message
        Object newUid = generateNextMessageUid();

        getDataStorageInstance().saveMessage(newUid, in);

        in.close();

        // increase total count of messages
        getMessageFolderInfo().incExists();

        // notify search-engine
        //getSearchEngineInstance().messageAdded(message);
        // this folder has changed
        changed = true;

        return newUid;
    }

    /** {@inheritDoc} */
    public void setFlags(Object uid, Flags flags) throws Exception {
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
        return ((newFolder instanceof LocalFolder) ||
        (newFolder instanceof VirtualFolder));
    }

    /**
 * Returns true since local folders can be moved.
 * @return true.
 */
    public boolean supportsMove() {
        return true;
    }
}
