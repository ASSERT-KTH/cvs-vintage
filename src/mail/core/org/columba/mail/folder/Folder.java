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
package org.columba.mail.folder;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

import javax.swing.tree.TreeNode;

import org.columba.core.command.StatusObservable;
import org.columba.core.command.StatusObservableImpl;
import org.columba.core.io.DiskIO;
import org.columba.core.xml.XmlElement;
import org.columba.mail.config.FolderItem;
import org.columba.mail.filter.Filter;
import org.columba.mail.filter.FilterList;
import org.columba.mail.folder.command.MarkMessageCommand;
import org.columba.mail.folder.search.DefaultSearchEngine;
import org.columba.mail.message.ColumbaHeader;
import org.columba.mail.message.ColumbaMessage;
import org.columba.mail.message.HeaderList;
import org.columba.ristretto.coder.Base64DecoderInputStream;
import org.columba.ristretto.coder.CharsetDecoderInputStream;
import org.columba.ristretto.coder.QuotedPrintableDecoderInputStream;
import org.columba.ristretto.message.Attributes;
import org.columba.ristretto.message.Flags;
import org.columba.ristretto.message.MessageFolderInfo;
import org.columba.ristretto.message.MimeHeader;

/**
 * 
 * Abstract Basic Folder class. It is subclassed by every folder class
 * containing messages and therefore offering methods to alter the mailbox.
 * <p>
 * Folders are plugins and therefore dynamically created. This should make it
 * easy to write new folders in the future.
 * <p>
 * To make it very easy to add new local mailbox formats, we added a slightly
 * more complex class hierachy in org.columba.mail.folder,
 * org.columba.mail.folder.headercache. An implementation example can be found
 * in org.columba.mail.folder.mh.
 * <p>
 * Please note, that you only need to implement {@link DataStorageInstance}
 * which should be trivial in most cases. Then create a class extending
 * {@link LocalFolder}and plug your datastorage in this folder in overwriting
 * getDataStorageInstance() method.
 * <p>
 * Last, don't forget to register your folder plugin:
 * <p>
 * Add your folder to <code>org.columba.mail.plugin.folder.xml</code>. This
 * way you create an association of the folder name and the class which gets
 * loaded.
 * <p>
 * Edit your tree.xml file and replace the MH mailbox implementation with
 * yours.
 * 
 * @author freddy @created 19. Juni 2001
 */
public abstract class Folder extends FolderTreeNode implements MailboxInterface {

    /**
     * total/unread/recent count of messages in this folder
     */
    protected MessageFolderInfo messageFolderInfo;

    /**
     * list of filters
     */
    protected FilterList filterList;

    /**
     * 
     * set changed to true if the folder data changes.
     */
    protected boolean changed = false;

    /**
     * directory where this folders files are stored
     */
    protected File directoryFile;

    /**
     * The last selected uid for the current folder. This information is used
     * to show the last selected message, if you switch to the current folder
     * and the lastSelection field is set. If the lastSelection field is null,
     * the first message in the table for this folder is shown. Have a look to
     * org.columba.mail.gui.table.TableController#showHeaderList
     */
    protected Object lastSelection;

    /**
     * Status information updates are handled in using StatusObservable.
     * <p>
     * Every command has to register its interest to this events before
     * accessing the folder.
     */
    protected StatusObservable observable;

    /**
     * parent directory for mail folders
     * 
     * for example: "/home/donald/.columba/mail/"
     */
    private String parentPath;

    // implement your own search-engine here
    protected DefaultSearchEngine searchEngine;

    protected HeaderListStorage headerListStorage;

    /**
     * Standard constructor.
     * 
     * @param item
     *            <class>FolderItem </class> contains information about the
     *            folder
     */
    public Folder(FolderItem item, String path) {
        super(item);

        // FIXME: why is this needed?
        // children is already initialised by DefaultMutableTreeNode
        //children = new Vector();
        messageFolderInfo = new MessageFolderInfo();

        changed = false;

        // remember parent path
        // (this is necessary for IMAPRootFolder sync operations)
        parentPath = path;

        String dir = path + getUid();

        if (DiskIO.ensureDirectory(dir)) {
            directoryFile = new File(dir);
        }

        loadMessageFolderInfo();

        observable = new StatusObservableImpl();
    }

    protected Folder() {
        super();

        messageFolderInfo = new MessageFolderInfo();

        changed = false;

        observable = new StatusObservableImpl();
    }

    /**
     * @param type
     */
    public Folder(String name, String type, String path) {
        super(name, type);

        messageFolderInfo = new MessageFolderInfo();

        changed = false;

        // remember parent path
        // (this is necessary for IMAPFolder sync operations)
        parentPath = path;

        String dir = path + getUid();

        if (DiskIO.ensureDirectory(dir)) {
            directoryFile = new File(dir);
        }

        loadMessageFolderInfo();

        observable = new StatusObservableImpl();
    }

    /**
     * Return the root folder of this folder.
     * <p>
     * This is especially useful when using IMAP. IMAP has a root folder which
     * is labelled with the account name.
     * 
     * @return FolderTreeNode return root parent folder of this folder
     */
    public FolderTreeNode getRootFolder() {
        FolderTreeNode parent = (FolderTreeNode) getParent();

        // There is no parent
        if (parent == null) { return this; }

        if (parent instanceof RootFolder) {
            return parent;
        } else {
            return ((Folder) parent).getRootFolder();
        }
    }

    /**
     * Returns the directory where the messages are saved
     * 
     * @return File the file representing the mailbox directory
     */
    public File getDirectoryFile() {
        return directoryFile;
    }

    /**
     * Call this method if folder data changed, so that we know if we have to
     * save the header cache.
     * 
     * @param b
     */
    public void setChanged(boolean b) {
        changed = b;
    }

    /**
     * Change the <class>MessageFolderInfo </class>
     * 
     * @param i
     *            the new messagefolderinfo
     */
    public void setMessageFolderInfo(MessageFolderInfo i) {
        messageFolderInfo = i;
    }

    /**
     * Check if folder was modified.
     * 
     * @return boolean True, if folder data changed. False, otherwise.
     */
    protected boolean hasChanged() {
        return changed;
    }

    /**
     * Method getMessageFolderInfo.
     * 
     * @return MessageFolderInfo
     */
    public MessageFolderInfo getMessageFolderInfo() {
        return messageFolderInfo;
    }

    /**
     * Method getFilterList.
     * 
     * @return FilterList
     */
    public FilterList getFilterList() {
        return filterList;
    }

    /**
     * @see javax.swing.tree.DefaultMutableTreeNode#getPathToRoot(TreeNode,
     *      int)
     */
    protected TreeNode[] getPathToRoot(TreeNode aNode, int depth) {
        TreeNode[] retNodes;

        if (aNode == null) {
            if (depth == 0) {
                return null;
            } else {
                retNodes = new TreeNode[depth];
            }
        } else {
            depth++;
            retNodes = getPathToRoot(aNode.getParent(), depth);
            retNodes[retNodes.length - depth] = aNode;
        }

        return retNodes;
    }

    /**
     * Return tree path as string
     * 
     * @return String tree path
     */
    public String getTreePath() {
        TreeNode[] treeNode = getPathToRoot(this, 0);

        StringBuffer path = new StringBuffer();

        for (int i = 1; i < treeNode.length; i++) {
            FolderTreeNode folder = (FolderTreeNode) treeNode[i];
            path.append("/" + folder.getName());
        }

        return path.toString();
    }

    /** ********************************** treenode implementation ********** */
    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return getName();
    }

    /**
     * Rename folder.
     * 
     * @param name
     *            new folder name
     * @return boolean true if success, false otherwise
     * @throws Exception
     */
    public boolean renameFolder(String name) throws Exception {
        setName(name);

        return true;
    }

    /**
     * save messagefolderinfo to xml-configuration
     *  
     */
    public void saveMessageFolderInfo() {
        MessageFolderInfo info = getMessageFolderInfo();

        FolderItem item = getFolderItem();

        XmlElement property = item.getElement("property");

        property.addAttribute("exists", new Integer(info.getExists())
                .toString());
        property.addAttribute("unseen", new Integer(info.getUnseen())
                .toString());
        property.addAttribute("recent", new Integer(info.getRecent())
                .toString());
    }

    /**
     * 
     * get messagefolderinfo from xml-configuration
     *  
     */
    protected void loadMessageFolderInfo() {
        XmlElement property = getFolderItem().getElement("property");

        if (property == null) { return; }

        MessageFolderInfo info = getMessageFolderInfo();

        String exists = property.getAttribute("exists");

        if (exists != null) {
            info.setExists(Integer.parseInt(exists));
        }

        String recent = property.getAttribute("recent");

        if (recent != null) {
            info.setRecent(Integer.parseInt(recent));
        }

        String unseen = property.getAttribute("unseen");

        if (unseen != null) {
            info.setUnseen(Integer.parseInt(unseen));
        }
    }

    /**
     * 
     * use this method to save folder meta-data when closing Columba
     *  
     */
    public void save() throws Exception {
        saveMessageFolderInfo();
    }

    /**
     * Returns the last selected Message for the current folder. If no message
     * was selected, it returns null. The return-value is the uid of the last
     * selected message.
     */
    public Object getLastSelection() {
        return lastSelection;
    }

    /**
     * Sets the last selection for the current folder. This should be the uid
     * of the last selected Message for the current folder.
     */
    public void setLastSelection(Object lastSel) {
        lastSelection = lastSel;
    }

    /**
     * @return observable containing status information
     */
    public StatusObservable getObservable() {
        return observable;
    }

    /**
     * @see org.columba.mail.folder.FolderTreeNode#supportsAddMessage()
     */
    public boolean supportsAddMessage() {
        return true;
    }

    /**
     * Returns true if this folder is an Inbox folder.
     * 
     * @return true if this folder is an Inbox folder.
     */
    public boolean isInboxFolder() {
        return false;
    }

    /**
     * Returns true if this folder is the Trash folder.
     * 
     * @return true if this folder is the Trash folder.
     */
    public boolean isTrashFolder() {
        return false;
    }

    /**
     * Parent directory for mail folders.
     * <p>
     * For example: /home/donald/.columba/mail
     * 
     * @return Returns the parentPath.
     */
    public String getParentPath() {
        return parentPath;
    }

    /** ********************* update of MessageFolderInfo ******************** */

    /**
     * @see org.columba.mail.folder.MailboxInterface#removeMessage(java.lang.Object)
     */
    public void removeMessage(Object uid) throws Exception {
        //      update message folder info
        //      decrement total count of message
        getMessageFolderInfo().decExists();

        // update unseen/recent state
        Flags flags = getFlags(uid);

        if (flags.getSeen()) {
            getMessageFolderInfo().decUnseen();
        }

        if (flags.getRecent()) {
            getMessageFolderInfo().decRecent();
        }

        // after updateing MessageFolderInfo -> remove message
        getHeaderListStorage().removeMessage(uid);

        // notify search-engine
        getSearchEngineInstance().messageRemoved(uid);

        // set folder changed flag
        // -> if not, the header cache wouldn't notice that something
        // -> has changed. And wouldn't save the changes.
        setChanged(true);

    }

    /**
     * @param uid
     * @param variant
     * @param worker
     * @throws Exception
     */
    protected void markMessage(Object uid, int variant) throws Exception {

        Flags flags = getFlags(uid);

        if (flags == null) { return; }

        switch (variant) {
        case MarkMessageCommand.MARK_AS_READ:
            {
                if (flags.getRecent()) {
                    getMessageFolderInfo().decRecent();
                }

                if (!flags.getSeen()) {
                    getMessageFolderInfo().decUnseen();
                }

                flags.setSeen(true);
                flags.setRecent(false);

                break;
            }

        case MarkMessageCommand.MARK_AS_UNREAD:
            {
                if (flags.getSeen()) {
                    getMessageFolderInfo().incUnseen();
                }

                flags.setSeen(false);

                break;
            }

        case MarkMessageCommand.MARK_AS_FLAGGED:
            {
                flags.setFlagged(true);

                break;
            }

        case MarkMessageCommand.MARK_AS_UNFLAGGED:
            {
                flags.setFlagged(false);

                break;
            }

        case MarkMessageCommand.MARK_AS_EXPUNGED:
            {
                if (flags.getSeen()) {
                    getMessageFolderInfo().decUnseen();
                }

                flags.setSeen(true);
                flags.setRecent(false);
                flags.setExpunged(true);

                break;
            }

        case MarkMessageCommand.MARK_AS_UNEXPUNGED:
            {
                flags.setExpunged(false);

                break;
            }

        case MarkMessageCommand.MARK_AS_ANSWERED:
            {
                flags.setAnswered(true);

                break;
            }

        case MarkMessageCommand.MARK_AS_SPAM:
            {
                setAttribute(uid, "columba.spam", Boolean.TRUE);

                break;
            }

        case MarkMessageCommand.MARK_AS_NOTSPAM:
            {
                setAttribute(uid, "columba.spam", Boolean.FALSE);

                break;
            }
        }

    }

    /**
     * @see org.columba.mail.folder.MailboxInterface#markMessage(java.lang.Object[],
     *      int)
     */
    public void markMessage(Object[] uids, int variant) throws Exception {
        for (int i = 0; i < uids.length; i++) {
            if (exists(uids[i])) {
                markMessage(uids[i], variant);
            }
        }

        // set folder changed flag
        // -> if not, the header cache wouldn't notice that something
        // -> has changed. And wouldn't save the changes.
        setChanged(true);
    }

    /**
     * @see org.columba.mail.folder.MailboxInterface#addMessage(java.io.InputStream)
     */
    public Object addMessage(InputStream in) throws Exception {
        // increase total count of messages
        getMessageFolderInfo().incExists();

        setChanged(true);

        return null;
    }

    /**
     * @see org.columba.mail.folder.MailboxInterface#addMessage(java.io.InputStream,
     *      org.columba.ristretto.message.Attributes)
     */
    public Object addMessage(InputStream in, Attributes attributes)
            throws Exception {

        // increase total count of messages
        getMessageFolderInfo().incExists();
        
        setChanged(true);

        // FIXME: why can't we access the attributes directly here?
        //        Do we have to wrap attributes in ColumbaHeader?
        ColumbaHeader header = new ColumbaHeader();
        header.setAttributes(attributes);

        // increment recent count of messages if appropriate
        if (header.getFlags().getRecent()) {
            getMessageFolderInfo().incRecent();
        }

        // increment unseen count of messages if appropriate
        if (header.getFlags().getSeen()) {
            getMessageFolderInfo().incUnseen();
        }

        return null;
    }

    /**
     * @see org.columba.mail.folder.MailboxInterface#addMessage(org.columba.mail.message.ColumbaMessage)
     */
    public Object addMessage(ColumbaMessage message) throws Exception {
        // increase total count of messages
        getMessageFolderInfo().incExists();

        // notify search-engine
        getSearchEngineInstance().messageAdded(message);

        setChanged(true);

        return null;
    }

    /** ****************************** AttributeStorage *********************** */

    /**
     * @return Returns the attributeStorage.
     */
    public abstract HeaderListStorage getHeaderListStorage();

    /**
     * @see org.columba.mail.folder.MailboxInterface#exists(java.lang.Object)
     */
    public boolean exists(Object uid) throws Exception {
        return getHeaderListStorage().exists(uid);
    }

    /**
     * @see org.columba.mail.folder.MailboxInterface#getHeaderList()
     */
    public HeaderList getHeaderList() throws Exception {
        return getHeaderListStorage().getHeaderList();
    }

    /**
     * @see org.columba.mail.folder.MailboxInterface#getUids()
     */
    public Object[] getUids() throws Exception {
        return getHeaderListStorage().getUids();
    }

    /**
     * @see org.columba.mail.folder.MailboxInterface#setAttribute(java.lang.Object,
     *      java.lang.String, java.lang.Object)
     */
    public void setAttribute(Object uid, String key, Object value)
            throws Exception {
        getHeaderListStorage().setAttribute(uid, key, value);
        //  set folder changed flag
        // -> if not, the header cache wouldn't notice that something
        // -> has changed. And wouldn't save the changes.
        setChanged(true);
    }

    /**
     * @see org.columba.mail.folder.MailboxInterface#getFlags(java.lang.Object)
     */
    public Flags getFlags(Object uid) throws Exception {
        return getHeaderListStorage().getFlags(uid);
    }

    /**
     * @see org.columba.mail.folder.MailboxInterface#getAttributes(java.lang.Object)
     */
    public Attributes getAttributes(Object uid) throws Exception {
        return getHeaderListStorage().getAttributes(uid);
    }

    /**
     * @see org.columba.mail.folder.MailboxInterface#getAttribute(java.lang.Object,
     *      java.lang.String)
     */
    public Object getAttribute(Object uid, String key) throws Exception {
        return getHeaderListStorage().getAttribute(uid, key);
    }

    /**
     * ************************ Search Engine
     * ***********************************
     */

    /**
     * @return
     */
    public abstract DefaultSearchEngine getSearchEngineInstance();

    /**
     * @param filter
     * @param uids
     * @return @throws
     *         Exception
     */
    public Object[] searchMessages(Filter filter, Object[] uids)
            throws Exception {
        return getSearchEngineInstance().searchMessages(filter, uids);
    }

    /**
     * @param filter
     * @return @throws
     *         Exception
     */
    public Object[] searchMessages(Filter filter) throws Exception {
        return getSearchEngineInstance().searchMessages(filter);
    }

    /**
     * Set new search engine
     * 
     * @see org.columba.mail.folder.search
     * 
     * @param engine
     *            new search engine
     */
    public void setSearchEngine(DefaultSearchEngine engine) {
        this.searchEngine = engine;
    }

    /**
     * TODO: move this out-of-folder!
     * 
     * @param header
     * @param bodyStream
     * @return
     */
    protected InputStream decodeStream(MimeHeader header, InputStream bodyStream) {
        String charsetName = header.getContentParameter("charset");
        int encoding = header.getContentTransferEncoding();

        switch (encoding) {
        case MimeHeader.QUOTED_PRINTABLE:
            {
                bodyStream = new QuotedPrintableDecoderInputStream(bodyStream);

                break;
            }

        case MimeHeader.BASE64:
            {
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

}
