package org.columba.mail.command;

import java.lang.reflect.Array;

import org.columba.core.command.DefaultCommandReference;
import org.columba.core.logging.ColumbaLogger;
import org.columba.mail.folder.FolderTreeNode;
import org.columba.mail.message.AbstractMessage;
import org.columba.mail.message.Message;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class FolderCommandReference extends DefaultCommandReference {
	private FolderTreeNode folder;
	private Object[] uids;
	private Integer[] address;
	private AbstractMessage message;
	private int markVariant;
	private String folderName;
	
	/**
	 * Constructor for FolderCommandReference.
	 * @param folder
	 */
	public FolderCommandReference(FolderTreeNode folder) {
		this.folder = folder;
		
	}
	
	public FolderCommandReference(FolderTreeNode folder, Message message) {
		this.folder = folder;
		this.message = message;
		
	}

	/**
	 * Constructor for FolderCommandReference.
	 * @param folder
	 * @param uids
	 */
	public FolderCommandReference(FolderTreeNode folder, Object[] uids) {
		
		this.folder = folder;
		this.uids = uids;
	}

	/**
	 * Constructor for FolderCommandReference.
	 * @param folder
	 * @param uids
	 * @param address
	 */
	public FolderCommandReference(
		FolderTreeNode folder,
		Object[] uids,
		Integer[] address) {
		
		this.folder = folder;
		this.uids = uids;
		this.address = address;
	}

	public FolderTreeNode getFolder() {
		return folder;
	}

	public void setFolder(FolderTreeNode folder) {
		this.folder = folder;
	}

	public Object[] getUids() {
		return uids;
	}

	public Integer[] getAddress() {
		return address;
	}

	public void setUids(Object[] uids) {
		this.uids = uids;
	}
	
	public AbstractMessage getMessage()
	{
		return message;
	}
	
	public void setMessage( AbstractMessage message )
	{
		this.message = message;
	}

	public void reduceToFirstUid() {
		if (uids == null)
			return;

		int size = Array.getLength(uids);

		if (size > 1) {
			Object[] oneUid = new Object[1];
			oneUid[0] = uids[0];
			uids = oneUid;
		}
	}

	public boolean tryToGetLock(Object locker) {
		ColumbaLogger.log.debug("try to get lock on: "+folder.getName() );
		
		//Lock result = folder.tryToGetLock();
		return folder.tryToGetLock(locker);
		/*
		if (result != null) {
			lock = result;
			ColumbaLogger.log.debug("locked successfully: "+folder.getName() );
			return true;
		} else {
			ColumbaLogger.log.debug("lock failed: "+folder.getName() );
			return false;
		}
		*/
	}

	public void releaseLock() {
		/*
		if (lock != null) {
			lock.release();
			lock = null;
			ColumbaLogger.log.debug("released lock for: "+folder.getName() );
		}
		*/
		ColumbaLogger.log.debug("releasing lock: "+folder.getName() );
		folder.releaseLock();
	}
	/**
	 * Returns the markVariant.
	 * @return int
	 */
	public int getMarkVariant() {
		return markVariant;
	}

	/**
	 * Sets the markVariant.
	 * @param markVariant The markVariant to set
	 */
	public void setMarkVariant(int markVariant) {
		this.markVariant = markVariant;
	}

	/**
	 * Returns the folderName.
	 * @return String
	 */
	public String getFolderName() {
		return folderName;
	}

	/**
	 * Sets the folderName.
	 * @param folderName The folderName to set
	 */
	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}

}
