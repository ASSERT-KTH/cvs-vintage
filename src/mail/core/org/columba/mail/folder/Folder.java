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

import javax.swing.JDialog;
import javax.swing.tree.TreeNode;

import org.columba.core.command.StatusObservable;
import org.columba.core.command.StatusObservableImpl;
import org.columba.core.main.MainInterface;
import org.columba.core.io.DiskIO;
import org.columba.core.xml.XmlElement;
import org.columba.mail.config.FolderItem;
import org.columba.mail.filter.Filter;
import org.columba.mail.filter.FilterList;
import org.columba.mail.gui.config.filter.ConfigFrame;
import org.columba.mail.gui.frame.AbstractMailFrameController;
import org.columba.ristretto.message.MessageFolderInfo;

/**
 *
 * Abstract Basic Folder class. It is subclassed by every folder
 * class containing messages and therefore offering methods
 * to alter the mailbox.
 * <p>
 * Folders are plugins and therefore dynamically created. This should
 * make it easy to write new folders in the future.
 * <p>
 * To make it very easy to add new local mailbox formats, we added a
 * slightly more complex class hierachy in org.columba.mail.folder,
 * org.columba.mail.folder.headercache. An implementation
 * example can be found in org.columba.mail.folder.mh.
 * <p>
 * Please note, that you only need to implement {@link DataStorageInstance}
 * which should be trivial in most cases. Then create a class extending
 * {@link CachedFolder} and plug your datastorage in this folder
 * in overwriting getDataStorageInstance() method.
 * <p>
 * Last, don't forget to register your folder plugin:<p>
 * Add your folder to org.columba.mail.plugin.folder.xml. This way you create
 * an association of the folder name and the class which gets loaded.
 * <p>
 * Edit your tree.xml file and replace the MH mailbox implementation with
 * yours.
 *
 * @author       freddy
 * @created      19. Juni 2001
 */
public abstract class Folder
    extends FolderTreeNode
    implements MailboxInterface {
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
     *        set changed to true if the folder data
     *  changes.
     */
    protected boolean changed = false;

    /**
     * directory where this folders files are stored
     */
    protected File directoryFile;

    /**
    * The last selected uid for the current folder. This information is used to show the
    * last selected message, if you switch to the current folder and the lastSelection field
    * is set. If the lastSelection field is null, the first message in the table for this
    * folder is shown. Have a look to org.columba.mail.gui.table.TableController#showHeaderList
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
     * Standard constructor.
     *
     * @param item <class>FolderItem</class> contains information about
     *             the folder
     */
    public Folder(FolderItem item) {
        super(item);

        // FIXME: why is this needed?
        // children is already initialised by DefaultMutableTreeNode
        //children = new Vector();
        messageFolderInfo = new MessageFolderInfo();

        changed = false;

        String dir = MainInterface.config.getConfigDirectory() + "/mail/" + getUid();

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
    public Folder(String name, String type) {
        super(name, type);

        messageFolderInfo = new MessageFolderInfo();

        changed = false;

        String dir = MainInterface.config.getConfigDirectory() + "/mail/" + getUid();

        if (DiskIO.ensureDirectory(dir)) {
            directoryFile = new File(dir);
        }

        loadMessageFolderInfo();

        observable = new StatusObservableImpl();
    }

    /**
     * Show Filter Editing Dialog.
     * <p>
     *
     *
     * @param frameController                the framecontroller which acts as parent
     *
     * @return JDialog                                the filter editing dialog
     */
    public JDialog showFilterDialog(AbstractMailFrameController frameController) {
        return new ConfigFrame(this);
    }

    /**
     * Return the root folder of this folder.
     *
     * This is especially useful when using IMAP. IMAP has a root folder
     * which is labelled with the account name.
     *
     *
     * @return FolderTreeNode                return root parent folder of this folder
     */
    public FolderTreeNode getRootFolder() {
        FolderTreeNode parent = (FolderTreeNode) getParent();

        if (parent instanceof RootFolder) {
            return parent;
        } else {
            return ((Folder) parent).getRootFolder();
        }
    }

    /**
     * Returns the the folder where messages are saved
     *
     * @return File         the file representing the mailbox directory
     */
    public File getDirectoryFile() {
        return directoryFile;
    }

    /**
     * Call this method if folder data changed, so that we know if we
     * have to save the header cache.
     *
     * @param b
     */
    public void setChanged(boolean b) {
        changed = b;
    }

    /**
     * Change the <class>MessageFolderInfo</class>
     *
     *
     * @param i                the new messagefolderinfo
     */
    public void setMessageFolderInfo(MessageFolderInfo i) {
        messageFolderInfo = i;
    }

    /**
     *
     * @return boolean                True, if folder data changed. False, otherwise.
     */
    protected boolean hasChanged() {
        return changed;
    }

    /**
     * Method getMessageFolderInfo.
     * @return MessageFolderInfo
     */
    public MessageFolderInfo getMessageFolderInfo() {
        return messageFolderInfo;
    }

    /**
     * Method getFilterList.
     * @return FilterList
     */
    public FilterList getFilterList() {
        return filterList;
    }

    /**
     * @see javax.swing.tree.DefaultMutableTreeNode#getPathToRoot(TreeNode, int)
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
     * @return String                tree path
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

    /************************************ treenode implementation ***********/
    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return getName();
    }

    /**
     * Rename folder.
     *
     * @param name                        new folder name
     * @return boolean                true if success, false otherwise
     * @throws Exception
     */
    public boolean renameFolder(String name) throws Exception {
        setName(name);

        return true;
    }

    /**
     * Return array of uids this folder contains.
     *
     * @return Object[]                array of all UIDs this folder contains
     */
    public Object[] getUids() throws Exception {
        return null;
    }

    /**
     * Search for a set of messages, viewing only a subset of this
     * folder.
     *
     * @param filter                Filter criteria to use
     * @param uids                        array of UIDs to do the search on
     * @return Object[]                array of matched messages as UIDs
     * @throws Exception
     */
    public abstract Object[] searchMessages(Filter filter, Object[] uids)
        throws Exception;

    /**
     * Search for a set of messages in the complete folder.
     *
     * @param filter                Filter criteria to use
     * @return Object[]                array of matched messages as UIDs
     * @throws Exception
     */
    public abstract Object[] searchMessages(Filter filter) throws Exception;

    /**
     * save messagefolderinfo to xml-configuration
     *
     */
    public void saveMessageFolderInfo() {
        MessageFolderInfo info = getMessageFolderInfo();

        FolderItem item = getFolderItem();

        XmlElement property = item.getElement("property");

        property.addAttribute(
            "exists",
            new Integer(info.getExists()).toString());
        property.addAttribute(
            "unseen",
            new Integer(info.getUnseen()).toString());
        property.addAttribute(
            "recent",
            new Integer(info.getRecent()).toString());
    }

    /**
     *
     * get messagefolderinfo from xml-configuration
     *
     */
    protected void loadMessageFolderInfo() {
        XmlElement property = getFolderItem().getElement("property");

        if (property == null) {
            return;
        }

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
     * use this method to save folder meta-data when
     * closing Columba
     *
     */
    public void save() throws Exception {
        saveMessageFolderInfo();
    }

    /**
    * Returns the last selected Message for the current folder. If no message was selected, it
    * returns null. The return-value is the uid of the last selected message.
    **/
    public Object getLastSelection() {
        return lastSelection;
    }

    /**
    * Sets the last selection for the current folder. This should be the uid of the last selected Message
    * for the current folder.
    */
    public void setLastSelection(Object lastSel) {
        lastSelection = lastSel;
    }

    /**
     * @return        observable containing status information
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
     * @return true if this folder is an Inbox folder.
     */
    public boolean isInboxFolder() {
        return false;
    }

    /**
     * Returns true if this folder is the Trash folder.
     * @return true if this folder is the Trash folder.
     */
    public boolean isTrashFolder() {
        return false;
    }
}
