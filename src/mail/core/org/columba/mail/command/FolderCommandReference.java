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
package org.columba.mail.command;

import java.io.File;
import java.lang.reflect.Array;

import org.columba.core.command.DefaultCommandReference;
import org.columba.mail.folder.FolderTreeNode;
import org.columba.mail.message.ColumbaMessage;

/**
 * This is a reference implemention suitable for folders containing
 * messages.
 * <p>
 * Its main purpose is to store source and/or destination folders and
 * arrays of message UIDs.
 * <p>
 * 
 *
 * @author fdietz
 */
public class FolderCommandReference extends DefaultCommandReference {
	private FolderTreeNode folder;
	private Object[] uids;
	private Integer[] address;
	private ColumbaMessage message;
	private int markVariant;
	private String folderName;
	
	private File destFile;
	
	/**
	 * Constructor for FolderCommandReference.
	 * @param folder
	 */
	public FolderCommandReference(FolderTreeNode folder) {
		this.folder = folder;
		
	}
	
	public FolderCommandReference(FolderTreeNode folder, ColumbaMessage message) {
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
	
	public ColumbaMessage getMessage()
	{
		return message;
	}
	
	public void setMessage( ColumbaMessage message )
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
		//ColumbaLogger.log.debug("try to get lock on: "+folder.getName() );
		
		//Lock result = folder.tryToGetLock();
		return folder.tryToGetLock(locker);
		
	}

	public void releaseLock(Object locker) {
		
		//ColumbaLogger.log.debug("releasing lock: "+folder.getName() );
		folder.releaseLock(locker);
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

	/**
	 * @return
	 */
	public File getDestFile() {
		return destFile;
	}

	/**
	 * @param destFile
	 */
	public void setDestFile(File destFile) {
		this.destFile = destFile;
	}

}
