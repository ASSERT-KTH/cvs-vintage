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

package org.columba.mail.folder.imap;

import java.util.List;

import javax.swing.ImageIcon;

import org.columba.core.command.StatusObservable;
import org.columba.core.command.StatusObservableImpl;
import org.columba.core.gui.util.ImageLoader;
import org.columba.core.logging.ColumbaLogger;
import org.columba.mail.config.AccountItem;
import org.columba.mail.config.FolderItem;
import org.columba.mail.main.MailInterface;
import org.columba.mail.config.SpecialFoldersItem;
import org.columba.mail.filter.Filter;
import org.columba.mail.folder.FolderTreeNode;
import org.columba.mail.folder.RootFolder;
import org.columba.mail.imap.IMAPStore;
import org.columba.mail.main.MailInterface;
import org.columba.mail.util.MailResourceLoader;
import org.columba.ristretto.imap.ListInfo;
import org.columba.ristretto.imap.protocol.IMAPProtocol;

public class IMAPRootFolder extends FolderTreeNode implements RootFolder {
	protected final static ImageIcon imapRootIcon=
		//ImageLoader.getSmallImageIcon("imap-16.png");
	ImageLoader.getSmallImageIcon("stock_internet-16.png");
	private final static int ONE_SECOND= 1000;
	private static final String[] specialFolderNames=
		{ "trash", "drafts", "templates", "sent" };
	private IMAPProtocol imap;

	//private boolean select=false;
	private boolean fetch= false;
	private StringBuffer cache;
	private int state;
	private List lsubList;

	//    private ImapOperator operator;
	private AccountItem accountItem;
	private IMAPStore store;

	/**
	 * Status information updates are handled in using StatusObservable.
	 * <p>
	 * Every command has to register its interest to this events before
	 * accessing the folder.
	 */
	protected StatusObservable observable;

	public IMAPRootFolder(FolderItem folderItem) {
		//super(node, folderItem);
		super(folderItem);
		observable= new StatusObservableImpl();

		accountItem=
			MailInterface.config.getAccountList().uidGet(
				folderItem.getInteger("account_uid"));

		updateConfiguration();
	}

	public IMAPRootFolder(AccountItem accountItem) {
		//super(node, folderItem);
		//super(getDefaultItem("IMAPRootFolder", getDefaultProperties()));
		super(accountItem.get("name"), "IMAPRootFolder");
		observable= new StatusObservableImpl();

		this.accountItem= accountItem;

		getFolderItem().set("account_uid", accountItem.getInteger("uid"));

		updateConfiguration();
	}

	/**
	 * @param type
	 */
	public IMAPRootFolder(String name, String type) {
		super(name, type);

		FolderItem item= getFolderItem();
		item.set("property", "accessrights", "system");
		item.set("property", "subfolder", "true");
	}

	public ImageIcon getCollapsedIcon() {
		return imapRootIcon;
	}

	public ImageIcon getExpandedIcon() {
		return imapRootIcon;
	}

	public String getDefaultChild() {
		return "IMAPFolder";
	}

	/**
	 * @return        observable containing status information
	 */
	public StatusObservable getObservable() {
		return observable;
	}

	protected void syncFolder(
		FolderTreeNode parent,
		String name,
		ListInfo info)
		throws Exception {
		ColumbaLogger.log.info("creating folder=" + name);

		if ((name.indexOf(store.getDelimiter()) != -1)
			&& (name.indexOf(store.getDelimiter()) != (name.length() - 1))) {
			// delimiter found
			//  -> recursively create all necessary folders to create
			//  -> the final folder 
			String subchild=
				name.substring(0, name.indexOf(store.getDelimiter()));
			FolderTreeNode subFolder=
				(FolderTreeNode) parent.findChildWithName(subchild, false);

			// if folder doesn't exist already
			if (subFolder == null) {
				subFolder= new IMAPFolder(subchild, "IMAPFolder");
				parent.add(subFolder);
				parent.getNode().addElement(subFolder.getNode());
				MailInterface.treeModel.insertNodeInto(
					subFolder,
					parent,
					parent.getIndex(subFolder));

				((IMAPFolder) subFolder).existsOnServer= true;
				subFolder.getFolderItem().set("selectable", "false");

				// this is the final folder
				//subFolder = addIMAPChildFolder(parent, info, subchild);
			} else {
				if (!((IMAPFolder) subFolder).existsOnServer) {
					((IMAPFolder) subFolder).existsOnServer= true;
					subFolder.getFolderItem().set("selectable", "false");
				}
			}

			// recursively go on
			syncFolder(
				subFolder,
				name.substring(name.indexOf(store.getDelimiter()) + 1),
				info);
		} else {
			// no delimiter found
			//  -> this is already the final folder
			// if folder doesn't exist already
			FolderTreeNode subFolder=
				(FolderTreeNode) parent.findChildWithName(name, false);

			if (subFolder == null) {
				subFolder= new IMAPFolder(name, "IMAPFolder");
				parent.add(subFolder);
				parent.getNode().addElement(subFolder.getNode());
				MailInterface.treeModel.insertNodeInto(
					subFolder,
					parent,
					parent.getIndex(subFolder));

				((IMAPFolder) subFolder).existsOnServer= true;
			} else {
				((IMAPFolder) subFolder).existsOnServer= true;
			}

			if (info.getParameter(ListInfo.NOSELECT)) {
				subFolder.getFolderItem().set("selectable", "false");
			} else {
				subFolder.getFolderItem().set("selectable", "true");
			}
		}
	}

	protected void markAllSubfoldersAsExistOnServer(
		FolderTreeNode parent,
		boolean value) {
		FolderTreeNode child;

		for (int i= 0; i < parent.getChildCount(); i++) {
			child= (FolderTreeNode) parent.getChildAt(i);

			if (child instanceof IMAPFolder) {
				((IMAPFolder) child).existsOnServer= value;
				markAllSubfoldersAsExistOnServer(child, value);
			}
		}
	}

	protected void removeNotMarkedSubfolders(FolderTreeNode parent)
		throws Exception {
		FolderTreeNode child;

		// first remove all subfolders recursively
		for (int i= 0; i < parent.getChildCount(); i++) {
			child= (FolderTreeNode) parent.getChildAt(i);

			if (child instanceof IMAPFolder) {
				removeNotMarkedSubfolders(child);
			}
		}

		// maybe remove this folder
		if (parent instanceof IMAPFolder) {
			if (!((IMAPFolder) parent).existsOnServer) {
				MailInterface.treeModel.removeNodeFromParent(parent);
				parent.removeFolder();
			}
		}
	}

	public void findSpecialFolders() {
		SpecialFoldersItem folders= accountItem.getSpecialFoldersItem();

		for (int i= 0; i < specialFolderNames.length; i++) {
			// Find special
			int specialUid= folders.getInteger(specialFolderNames[i]);

			// if have already a suitable folder skip the search
			if (this.findChildWithUID(specialUid, true) == null) {
				// search for a folder thats on the IMAP account
				// first try to find the local translation of special
				FolderTreeNode specialFolder=
					this.findChildWithName(
						MailResourceLoader.getString(
							"tree",
							specialFolderNames[i]),
						true);

				if (specialFolder == null) {
					// fall back to the english version
					specialFolder=
						this.findChildWithName(specialFolderNames[i], true);
				}

				if (specialFolder != null) {
					// we found a suitable folder -> set it
					folders.set(specialFolderNames[i], specialFolder.getUid());
				}
			}
		}
	}

	public void syncSubscribedFolders() {
		// first clear all flags
		markAllSubfoldersAsExistOnServer(this, false);

		IMAPFolder inbox= (IMAPFolder) this.findChildWithName("INBOX", false);
		inbox.existsOnServer= true;

		try {
			// create and tag all subfolders on server
			ListInfo[] listInfo= getStore().lsub("", "*");

			if (listInfo != null) {
				for (int i= 0; i < listInfo.length; i++) {
					ListInfo info= listInfo[i];
					ColumbaLogger.log.info(
						"delimiter=" + getStore().getDelimiter());

					String folderPath= info.getName();

					syncFolder(this, folderPath, info);
				}
			}

			// remove all subfolders that are not marked as existonserver
			removeNotMarkedSubfolders(this);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		// This fixes the strange behaviour of the courier imapserver
		// which sets the \Noselect flag on INBOX
		inbox.getFolderItem().set("selectable", "true");

		findSpecialFolders();
	}

	public IMAPStore getStore() {
		return store;
	}

	public void updateConfiguration() {
		store= new IMAPStore(accountItem.getImapItem(), this);

	}

	/**
	 * @see org.columba.mail.folder.Folder#searchMessages(org.columba.mail.filter.Filter, java.lang.Object, org.columba.core.command.WorkerStatusController)
	 */
	public Object[] searchMessages(Filter filter, Object[] uids)
		throws Exception {
		return null;
	}

	/**
	 * @see org.columba.mail.folder.Folder#searchMessages(org.columba.mail.filter.Filter, org.columba.core.command.WorkerStatusController)
	 */
	public Object[] searchMessages(Filter filter) throws Exception {
		return null;
	}

	/**
	 * @return
	 */
	public AccountItem getAccountItem() {
		return accountItem;
	}

	/* (non-Javadoc)
	 * @see org.columba.mail.folder.FolderTreeNode#addSubfolder(org.columba.mail.folder.FolderTreeNode)
	 */
	public void addSubfolder(FolderTreeNode child) throws Exception {
		String path= child.getName();
		boolean result= getStore().createFolder(path);

		if (result) {
			super.addSubfolder(child);
		}
	}

	/* (non-Javadoc)
	 * @see org.columba.mail.folder.Folder#save()
	 */
	public void save() throws Exception {
		ColumbaLogger.log.info("Logout from IMAPServer " + getName());

		getStore().logout();
	}

	/* (non-Javadoc)
	 * @see org.columba.mail.folder.RootFolder#getTrashFolder()
	 */
	public FolderTreeNode getTrashFolder() {
		FolderTreeNode ret=
			findChildWithUID(
				accountItem.getSpecialFoldersItem().getInteger("trash"),
				true);

		// has the imap account no trash folder using the default trash folder
		if (ret == null) {
			ret= MailInterface.treeModel.getTrashFolder();
		}

		return ret;
	}

	/* (non-Javadoc)
	 * @see org.columba.mail.folder.RootFolder#getInbox()
	 */
	public FolderTreeNode getInboxFolder() {
		return (IMAPFolder) this.findChildWithName("INBOX", false);
	}
}
