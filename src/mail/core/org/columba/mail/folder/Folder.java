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
//
//$Log: Folder.java,v $
//
package org.columba.mail.folder;

import java.io.File;
import java.util.Vector;

import javax.swing.JDialog;
import javax.swing.tree.TreeNode;

import org.columba.core.command.WorkerStatusController;
import org.columba.core.config.ConfigPath;
import org.columba.core.io.DiskIO;
import org.columba.core.xml.XmlElement;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.config.FolderItem;
import org.columba.mail.filter.Filter;
import org.columba.mail.filter.FilterList;
import org.columba.mail.gui.config.filter.ConfigFrame;
import org.columba.mail.gui.frame.AbstractMailFrameController;
import org.columba.mail.message.AbstractMessage;
import org.columba.mail.message.ColumbaHeader;
import org.columba.mail.message.HeaderList;
import org.columba.mail.message.MimePart;
import org.columba.mail.message.MimePartTree;

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
 * slightly more complex class hierachy in <class>org.columba.mail.folder</class>,
 * <class>org.columba.mail.folder.headercache</class>. An implementation 
 * example can be found in <class>org.columba.mail.folder.mh</class>.
 * <p>
 * Please note, that you only need to implement <class>DataStorageInstance</class>
 * which should be trivial in most cases. Then create a class extending
 * <class>CachedFolder</class> and plug your datastorage in this folder
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
public abstract class Folder extends FolderTreeNode {

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
	 *	set changed to true if the folder data
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
	 * Standard constructor. 
	 * 
	 * @param node <class>AdapterNode</class> this connects the config
	 *             to the JTree TreeModel
	 * @param item <class>FolderItem</class> contains information about
	 *             the folder
	 */
	public Folder(FolderItem item) {
		super(item);

		// FIXME: why is this needed?
		// children is already initialised by DefaultMutableTreeNode
		//children = new Vector();

		init();

		String dir = ConfigPath.getConfigDirectory() + "/mail/" + getUid();
		if (DiskIO.ensureDirectory(dir))
			directoryFile = new File(dir);
			
		
		loadMessageFolderInfo();
	}
	
	
	/**
	 * Constructor for creating temporary-folders or other types
	 * which work only in memory and aren't visible in the <class>
	 * TreeView</class>
	 * 
	 * @param name Name of the folder
	 */
	public Folder(String name) {
		super(null);

		children = new Vector();

		init();

		String dir = ConfigPath.getConfigDirectory() + "/mail/" + name;
		if (DiskIO.ensureDirectory(dir))
			directoryFile = new File(dir);

	}

	
	
	/**
	 * Show Filter Editing Dialog.
	 * <p>
	 * 
	 * 
	 * @param frameController		the framecontroller which acts as parent
	 * 
	 * @return JDialog				the filter editing dialog 
	 */
	public JDialog showFilterDialog(AbstractMailFrameController frameController) {
		return new ConfigFrame(this);
	}

	/**
	 * Copy messages identified by UID to this folder.
	 * 
	 * This method is necessary for optimization reasons.
	 * 
	 * Think about using local and remote folders. If we would have only
	 * methods to add/remove messages this wouldn't be very efficient
	 * when moving messages between for example IMAP folders on the same
	 * server. We would have to download a complete message to remove it
	 * and then upload it again to add it to the destination folder.
	 * 
	 * Using the innercopy method the IMAP server can use its COPY 
	 * command to move the message on the server-side.
	 * 
	 * @param destFolder		the destination folder of the copy operation
	 * @param uids				an array of UID's identifying the messages
	 * @param worker
	 * @throws Exception
	 */
	public void innerCopy(
		Folder destFolder,
		Object[] uids,
		WorkerStatusController worker)
		throws Exception {
	}

	/**
	 * Return the root folder of this folder.
	 * 
	 * This is especially useful when using IMAP. IMAP has a root folder
	 * which is labelled with the account name.
	 * 
	 * 
	 * @return FolderTreeNode		return root parent folder of this folder
	 */
	public FolderTreeNode getRootFolder() {
		FolderTreeNode folderTreeNode = (FolderTreeNode) getParent();
		while (folderTreeNode != null) {

			if (folderTreeNode instanceof Root) {

				return (Root) folderTreeNode;
			}

			folderTreeNode = (FolderTreeNode) folderTreeNode.getParent();

		}

		return null;
	}
	


	/**
	 * Do some initialization work both constructors share
	 * 
	 */
	protected void init() {

		messageFolderInfo = new MessageFolderInfo();

		changed = false;


	}

	/**
	 * Returns the the folder where messages are saved
	 * 
	 * @return File 	the file representing the mailbox directory
	 */
	public File getDirectoryFile() {
		return directoryFile;
	}

	/**
	 * @see org.columba.modules.mail.folder.FolderTreeNode#createChildren(WorkerStatusController)
	 */
	public void createChildren(WorkerStatusController worker) {
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
	 * @param i		the new messagefolderinfo
	 */
	public void setMessageFolderInfo(MessageFolderInfo i) {
		messageFolderInfo = i;
	}

	/**
	 * 
	 * @param list	the new list of <class>Filter</class>
	 */
	public void setFilterList(FilterList list) {
		filterList = list;
	}

	
	/**
	 * 
	 * @return boolean		True, if folder data changed. False, otherwise.
	 */
	public boolean getChanged() {
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
	 * 
	 * @return boolean		True if folder has filters, false otherwise
	 */
	public boolean hasFilters() {
		if (getFilterList() == null)
			return false;

		return getFilterList().count() > 0;
	}

	/**
	 * Removes all messages which are marked as expunged
	 * 	
	 * @param worker
	 * @throws Exception
	 */
	public abstract void expungeFolder(
		WorkerStatusController worker)
		throws Exception;

	/**
	 * Add message to this folder.
	 * 
	 * @param message Message object can be null 
	 * @param source  raw string of message 
	 * @return Object UID of message
	 * @throws Exception
	 */
	public abstract Object addMessage(
		AbstractMessage message,
		WorkerStatusController worker)
		throws Exception;

	/**
	 * Add message to folder.
	 * 
	 * @param source
	 * @param worker
	 * @return Object
	 * @throws Exception
	 */
	public abstract Object addMessage(
		String source,
		WorkerStatusController worker)
		throws Exception;
		
	/**
	 * 
	 * 
	 * @param uid 			UID of message
	 * @return boolean 		true, if message exists
	 * @throws Exception
	 */
	public abstract boolean exists(Object uid, WorkerStatusController worker)
		throws Exception;

	/**
	 * 
	 * 
	 * @param worker for accessing the <class>StatusBar</class>
	 * @return HeaderList		list of headers 
	 * @throws Exception
	 */
	public abstract HeaderList getHeaderList(WorkerStatusController worker)
		throws Exception;

	/**
	 * Mark messages as read/flagged/expunged/etc.
	 * 
	 * See <class>MarkMessageCommand</class> for more information especially
	 * concerning the variant value.
	 * 
	 * @param uid		array of UIDs 
	 * @param variant	variant can be a value between 0 and 6
	 * @param worker
	 * @throws Exception
	 */
	public abstract void markMessage(
		Object[] uids,
		int variant,
		WorkerStatusController worker)
		throws Exception;

	/**
	 * Remove message from folder.
	 * 
	 * @param uid		UID identifying the message to remove
	 * @throws Exception
	 */
	public abstract void removeMessage(
		Object uid,
		WorkerStatusController worker)
		throws Exception;

	/**
	 * 
	 * Read <class>MimePart</class> and <class>MimePartTree</class> for
	 * more details. Especially on the address parameter.
	 * 
	 * @param uid			UID of message
	 * @param address		array of Integer, addressing the MimePart
	 * @param worker
	 * @return MimePart		MimePart of message
	 * @throws Exception
	 */
	public abstract MimePart getMimePart(
		Object uid,
		Integer[] address,
		WorkerStatusController worker)
		throws Exception;

	/**
	 * Return the source of the message.
	 * 
	 * @param uid		UID of message
	 * @param worker
	 * @return String		the source of the message
	 * @throws Exception
	 */
	public abstract String getMessageSource(
		Object uid,
		WorkerStatusController worker)
		throws Exception;

	/**
	 * Return mimepart structure. See <class>MimePartTree</class> for
	 * more details.
	 * 
	 * @param uid				UID of message
	 * @param worker
	 * @return MimePartTree		return mimepart structure
	 * @throws Exception
	 */
	public abstract MimePartTree getMimePartTree(
		Object uid,
		WorkerStatusController worker)
		throws Exception;

	/**
	 * Return header of message
	 * 
	 * @param uid					UID of message
	 * @param worker
	 * @return ColumbaHeader		header of message
	 * @throws Exception
	 */
	public abstract ColumbaHeader getMessageHeader(
		Object uid,
		WorkerStatusController worker)
		throws Exception;

	

	/**
	 * @see javax.swing.tree.DefaultMutableTreeNode#getPathToRoot(TreeNode, int)
	 */
	protected TreeNode[] getPathToRoot(TreeNode aNode, int depth) {
		TreeNode[] retNodes;

		if (aNode == null) {
			if (depth == 0)
				return null;
			else
				retNodes = new TreeNode[depth];
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
	 * @return String		tree path 
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
	 * @see org.columba.modules.mail.folder.FolderTreeNode#getName()
	 */
	public String getName() {
		String name = null;

		FolderItem item = getFolderItem();
		name = item.get("property", "name");

		return name;
	}

	/**
	 * @see org.columba.modules.mail.folder.FolderTreeNode#setName(String)
	 */
	public void setName(String newName) {

		FolderItem item = getFolderItem();
		item.set("property", "name", newName);

	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return getName();
	}

	/**
	 * Rename folder.
	 * 
	 * @param name			new folder name
	 * @return boolean		true if success, false otherwise
	 * @throws Exception
	 */
	public boolean renameFolder(String name) throws Exception {
		setName(name);

		return true;
	}
	

	/**
	 * Return array of uids this folder contains.
	 * 
	 * @return Object[]		array of all UIDs this folder contains
	 */
	public Object[] getUids(WorkerStatusController worker) throws Exception {
		return null;
	}
	

	/**
	 * Search for a set of messages, viewing only a subset of this
	 * folder.
	 * 
	 * @param filter		Filter criteria to use
	 * @param uids			array of UIDs to do the search on
	 * @param worker
	 * @return Object[]		array of matched messages as UIDs
	 * @throws Exception 
	 */
	public abstract Object[] searchMessages(
		Filter filter,
		Object[] uids,
		WorkerStatusController worker)
		throws Exception;

	/**
	 * Search for a set of messages in the complete folder.
	 * 
	 * @param filter		Filter criteria to use
	 * @param worker
	 * @return Object[]		array of matched messages as UIDs
	 * @throws Exception
	 */
	public abstract Object[] searchMessages(
		Filter filter,
		WorkerStatusController worker)
		throws Exception;

	/**
	 * Method getCommandReference.
	 * 
	 * @param r
	 * @return FolderCommandReference[]
	 */
	public FolderCommandReference[] getCommandReference(FolderCommandReference[] r) {
		return r;
	}
	
	/**
	 * save messagefolderinfo to xml-configuration
	 * 
	 */
	public void saveMessageFolderInfo()
	{
		MessageFolderInfo info = getMessageFolderInfo();
		
		FolderItem item = getFolderItem();
		
		XmlElement property = item.getElement("property");
		
		property.addAttribute("exists", new Integer(info.getExists()).toString());
		property.addAttribute("unseen", new Integer(info.getUnseen()).toString());
		property.addAttribute("recent", new Integer(info.getRecent()).toString());
	}
	
	/**
	 * 
	 * get messagefolderinfo from xml-configuration
	 * 
	 */
	protected void loadMessageFolderInfo()
		{
			XmlElement property = getFolderItem().getElement("property");
			if ( property == null ) return;
			
			MessageFolderInfo info = getMessageFolderInfo();
			
			String exists = property.getAttribute("exists");
			if ( exists != null ) info.setExists(Integer.parseInt(exists));
			String recent = property.getAttribute("recent");
			if ( recent != null ) info.setRecent(Integer.parseInt(recent));
			String unseen = property.getAttribute("unseen");
			if ( unseen != null ) info.setUnseen(Integer.parseInt(unseen));
			
			
		}
		
	/**
	 * 
	 * use this method to save folder meta-data when
	 * closing Columba
	 *
	 */
	public void save(WorkerStatusController worker) throws Exception
	{
		saveMessageFolderInfo();
	}
	
	/**
	* Returns the last selected Message for the current folder. If no message was selected, it
	* returns null. The return-value is the uid of the last selected message.
	**/
	public Object getLastSelection() {
		return this.lastSelection;
	}
	
	/**
	* Sets the last selection for the current folder. This should be the uid of the last selected Message
	* for the current folder.
	*/
	public void setLastSelection(Object lastSel) {
		this.lastSelection = lastSel;
	}

}