// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Library General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

package org.columba.mail.folder.imap;

import java.util.Hashtable;
import java.util.Vector;

import javax.swing.Timer;

import org.columba.core.command.WorkerStatusController;
import org.columba.core.config.AdapterNode;
import org.columba.core.logging.ColumbaLogger;
import org.columba.mail.config.FolderItem;
import org.columba.mail.config.ImapItem;
import org.columba.mail.folder.Folder;
import org.columba.mail.folder.FolderTreeNode;
import org.columba.mail.imap.IMAPStore;
import org.columba.mail.imap.parser.Imap4Parser;
import org.columba.mail.imap.parser.ListInfo;
import org.columba.mail.imap.protocol.IMAPProtocol;

public class IMAPRootFolder extends FolderTreeNode //implements ActionListener 
{
	private IMAPProtocol imap;
	private ImapItem item;
	//private boolean select=false;
	private boolean fetch = false;
	private Imap4Parser parser;
	private StringBuffer cache;
	private int state;
	private int accountUid;
	private Vector lsubList;

	private final static int ONE_SECOND = 1000;
	private Timer timer;

	//    private ImapOperator operator;

	private IMAPStore store;

	public IMAPRootFolder(
		AdapterNode node,
		FolderItem folderItem,
		ImapItem item,
		int accountUid) {
		//super(node, folderItem);
		super(node);

		this.item = item;
		this.accountUid = accountUid;

		//state = 0;

		//lsubList = null;

		//imap = new Imap4();

		//restartTimer();

		store = new IMAPStore(item, this);

	}
	
	public boolean isAlreadyLocked()
	{
		return myLock.isLocked();
	}

	public Hashtable getAttributes() {
		Hashtable attributes = new Hashtable();

		attributes.put("accessrights", "user");
		attributes.put("messagefolder", "true");
		attributes.put("type", "imap");
		attributes.put("subfolder", "true");
		attributes.put("accessrights", "true");
		attributes.put("add", "true");
		attributes.put("remove", "true");
		attributes.put("accountuid", new Integer(accountUid));

		return attributes;
	}
	
	public int getAccountUid()
	{
		return accountUid;
	}

	public Folder instanceNewChildNode(AdapterNode node, FolderItem item) {
		return new IMAPFolder(node, item, this.item, this);
	}

	protected void addSubFolder(FolderTreeNode folder, String name) throws Exception{
		ColumbaLogger.log.debug("addSubFolder=<" + name + ">");

		if (name.indexOf(store.getDelimiter()) != -1) {

			String subchild =
				name.substring(0, name.indexOf(store.getDelimiter()));
			FolderTreeNode subFolder = (FolderTreeNode) folder.getChild(subchild);

			if (subFolder == null) {
				ColumbaLogger.log.debug("creating folder=" + subchild);

				// folder does not exist, create new folder
				
				Hashtable parameter = getAttributes();
				parameter.put("name", subchild);
				parameter.put("messagefolder","false");
				subFolder = (IMAPFolder) folder.addSubFolder(parameter);
				
			}

			addSubFolder(
				subFolder,
				name.substring(name.indexOf(store.getDelimiter()) + 1));

		} else {
			ColumbaLogger.log.debug("no delimiters in mailbox-name found");

			if (folder.getChild(name) == null) {
				Hashtable parameter = getAttributes();
				parameter.put("name", name);
				folder.addSubFolder(parameter);
			}
		}
	}

	public void createChildren( WorkerStatusController  worker ) {
		try {
			ListInfo[] listInfo = getStore().lsub("", "*", worker);

			for (int i = 0; i < listInfo.length; i++) {
				ListInfo info = listInfo[i];
				getStore().setDelimiter(info.getDelimiter());
				
				addSubFolder(this, info.getName().trim() );

			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public IMAPStore getStore() {
		return store;
	}

	public ImapItem getImapItem() {
		return item;
	}

	/*
	public void restartTimer() {
	
		if (item.isMailCheck() == true) {
			int interval = -1;
	
			try {
				interval = Integer.parseInt(item.getInterval());
			} catch (NumberFormatException ex) {
				ex.printStackTrace();
			}
	
			if (interval != -1) {
				timer =
					new Timer(
						ONE_SECOND * interval * 60,
						(ActionListener) this);
				timer.restart();
			}
		} else {
			if (timer != null) {
				timer.stop();
			}
		}
	}
	
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
	
		if (src.equals(timer)) {
	
			System.out.println("timer action");
	
			IMAPFolder inboxFolder = (IMAPFolder) getChild("inbox");
	
			if (inboxFolder != null) {
				
				//MainInterface.treeViewer.setSelected(inboxFolder);
				
				
				//MainInterface.headerTableViewer.setFolder(inboxFolder);
				
				FolderOperation op =
					new FolderOperation(Operation.IMAP_CHECK, 20, inboxFolder);
	
				MainInterface.crossbar.operate(op);
				
			}
	
		}
	}
	
	public String getDelimiter() {
		return delimiter;
	}
	
	public boolean isLogin(SwingWorker worker) throws Exception {
		if (getState() == Imap4.STATE_AUTHENTICATE)
			return true;
		else {
			// we are in Imap4.STATE_NONAUTHENTICATE
	
			login(worker);
			return false;
		}
	
	}
	
	public void clearLSubList() {
		lsubList = null;
	}
	
	public int getAccountUid() {
		return accountUid;
	}
	
	public ImapItem getImapItem() {
		return item;
	}
	
	public Imap4 getImapServerConnection() {
		return imap;
	}
	
	private boolean isSubscribed(IMAPFolder folder, Vector lsubList) {
		String folderPath = folder.getImapPath();
	
		for (int i = 0; i < lsubList.size(); i++) {
	
			String path = (String) lsubList.get(i);
	
			if (folderPath.equalsIgnoreCase(path))
				return true;
		}
	
		return false;
	}
	
	private void removeUnsubscribedFolders(Folder child) {
		if (!(child instanceof IMAPRootFolder)) {
			if (child.getChildCount() == 0) {
				Folder parent = (Folder) child.getParent();
				child.removeFromParent();
				System.out.println("folder removed:" + parent);
	
				removeUnsubscribedFolders(parent);
			}
		}
	}
	
	public void removeUnsubscribedFolders(Folder parent, Vector lsubList) {
		for (int i = 0; i < parent.getChildCount(); i++) {
			if (!(parent.getChildAt(i) instanceof IMAPFolder))
				continue;
	
			IMAPFolder child = (IMAPFolder) parent.getChildAt(i);
	
			if (child.getChildCount() == 0) {
				// if folder is not subscribed: remove folder
				if (!isSubscribed(child, lsubList)) {
					//removeUnsubscribedFolders(child);
					child.removeFromParent();
					i--;
				}
			} else
				removeUnsubscribedFolders(child, lsubList);
		}
	}
	
	private void addFolder(Folder folder, String name, Vector lsubList) {
	
		if (name.indexOf(delimiter) != -1) {
	
			String subchild = name.substring(0, name.indexOf(delimiter));
			IMAPFolder subFolder = (IMAPFolder) folder.getChild(subchild);
	
			if (subFolder == null) {
	
				// folder does not exist, create new folder
				subFolder =
					(IMAPFolder) MainInterface
						.treeModel
						.addImapFolder(
						folder,
						subchild,
						item,
						this,
						accountUid);
	
				//subFolder.getFolderItem().setMessageFolder("false");
			}
	
			addFolder(
				subFolder,
				name.substring(name.indexOf(delimiter) + 1, name.length()),
				lsubList);
		} else {
			if (folder.getChild(name) == null)
				MainInterface.treeModel.addImapFolder(
					folder,
					name,
					item,
					this,
					accountUid);
		}
	
	}
	*/

	/*
	private void addSubscribedFolders(Vector lsubList) throws Exception {
		boolean inbox = false;
		boolean answer;
	
		for (int i = 0; i < lsubList.size(); i++) {
			String name = (String) lsubList.get(i);
	
			if (name.toLowerCase().equalsIgnoreCase("inbox"))
				inbox = true;
	
			addFolder(this, name, lsubList);
	
		}
	
		if (inbox == false) {
			answer = imap.flist("", "");
			//System.out.println("trying to parse flist");
			String str = imap.getResult();
			//System.out.println("str: "+ str );
			str = str.toLowerCase();
			//System.out.println("str: "+ str );
	
			if (str.indexOf("inbox") != -1) {
				Folder childFolder = (Folder) getChild("Inbox");
				if (childFolder == null) {
					MainInterface.treeModel.addImapFolder(
						this,
						"Inbox",
						item,
						this,
						accountUid);
				}
			} else {
				System.out.println("string inbox not found");
			}
		}
	}
	
	protected void lsubsub(Vector lsubList, Vector v) throws Exception {
	
		//lsubList.addAll(v);
	
		for (int i = 0; i < v.size(); i++) {
			String path = (String) v.get(i);
			lsubList.add(path);
	
			System.out.println("path:" + path);
	
			boolean answer = imap.flsub(path + "/*");
	
			if (imap.getResult().length() > 0) {
				Vector temp = Imap4Parser.parseLsub(imap.getResult());
	
				if (temp.size() > 0)
					lsubsub(lsubList, temp);
			}
	
		}
	
	}
	*/

	/**
	 *
	 * this method is called to get the initial folder-list
	 *  ( this happens when doppel-clicking the imap root-folder
	 * */

	/*
	public void lsub(SwingWorker worker) throws Exception {
	
		boolean answer;
		lsubList = new Vector();
		Vector v = new Vector();
	
		getImapServerConnection().setState(Imap4.STATE_NONAUTHENTICATE);
	
		isLogin(worker);
	
		if (worker != null)
			worker.setText("Retrieve folder listing...");
	
		if (worker != null)
			worker.startTimer();
	
		answer = imap.flist("", "");
	
		String result = imap.getResult();
	
		delimiter = Imap4Parser.parseDelimiter(result);
		System.out.println("delimiter=" + delimiter);
	
		answer = imap.flsub("*");
		//System.out.println("trying to parse lsub");
		String result2 = imap.getResult();
		System.out.println("--------->result:\n" + result2);
	
		lsubList = Imap4Parser.parseLsub(result2);
	
		//lsubsub(lsubList, v);
	
		v = new Vector();
		v.add("INBOX");
	
		lsubsub(lsubList, v);
	
		// add subscribed folders
	
		addSubscribedFolders(lsubList);
	
		// remove unsubscribed folders
	
		removeUnsubscribedFolders(this, lsubList);
	
		if (worker != null)
			worker.stopTimer();
	
	}
	*/

	/**
	 *
	 *  this method is called by the subscribe/unsubscribe dialog
	 *
	 *
	 **/
	/*
	public Vector getLSubList() throws Exception {
		if (lsubList != null)
			return lsubList;
	
		boolean answer;
		lsubList = new Vector();
	
		try {
	
			answer = imap.flsub("*");
			//System.out.println("trying to parse lsub");
	
			lsubList = Imap4Parser.parseLsub(imap.getResult());
	
		} catch (Exception ex) {
			//System.out.println("imapfolder->lsub: "+ ex.getMessage() );
			throw new Exception(
				"IMAPRootFolder->getLSubList: " + ex.getMessage());
		}
	
		return lsubList;
	}
	
	public boolean subscribe(String path) throws Exception {
		boolean answer = false;
	
		try {
			answer = imap.subscribe(path);
		} catch (Exception ex) {
			//System.out.println("imapfolder->lsub: "+ ex.getMessage() );
			throw new Exception(
				"IMAPRootFolder->subscribe: " + ex.getMessage());
		}
	
		return answer;
	}
	
	public boolean unsubscribe(String path) throws Exception {
		boolean answer = false;
		try {
			answer = imap.unsubscribe(path);
		} catch (Exception ex) {
			//System.out.println("imapfolder->lsub: "+ ex.getMessage() );
			throw new Exception(
				"IMAPRootFolder->unsubscribe: " + ex.getMessage());
		}
	
		return answer;
	}
	*/
	/**
	 *
	 *  this method is called by the subscribe/unsubscribe dialog
	 *
	 *
	 **/
	/*
	public Vector getList(SubscribeTreeNode treeNode) throws Exception {
		StringBuffer buf = new StringBuffer(treeNode.getName());
		SubscribeTreeNode node = (SubscribeTreeNode) treeNode.getParent();
		while (node != null) {
			if (node.getName().equals("root"))
				break;
	
			buf.insert(0, node.getName() + "/");
			node = (SubscribeTreeNode) node.getParent();
		}
	
		String name = buf.toString();
	
		Vector v = new Vector();
	
		boolean answer;
	
		try {
	
			answer = imap.flist(name.trim() + "/%", "");
			//System.out.println("trying to parse list");
			v = Imap4Parser.parseList(imap.getResult());
	
		} catch (Exception ex) {
			//System.out.println("imapfolder->lsub: "+ ex.getMessage() );
			throw new Exception("IMAPRootFolder->getList: " + ex.getMessage());
		}
	
		return v;
	}
	*/
	/**
	 *
	 *  this method is called by the subscribe/unsubscribe dialog
	 *
	 *
	 **/

	/*
	public Vector getList() throws Exception {
		Vector v = new Vector();
	
		boolean answer;
	
		isLogin(null);
	
		answer = imap.flist("*", "");
		// System.out.println("trying to parse list");
		v = Imap4Parser.parseList(imap.getResult());
	
		return v;
	}
	*/

	/*
	public boolean addFolder(String path) throws Exception {
		isLogin(null);
	
		boolean b = imap.create(path);
	
		if (b == false) {
			throw new ImapException(imap.getResult());
		}
	
		return b;
	}
	*/

}