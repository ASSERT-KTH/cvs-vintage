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

import org.columba.core.folder.DefaultFolderCommandReference;
import org.columba.core.folder.IFolder;
import org.columba.mail.folder.IMailFolder;
import org.columba.mail.message.IColumbaMessage;

/**
 * This is a reference implemention suitable for folders containing messages.
 * <p>
 * Its main purpose is to store source and/or destination folders and arrays of
 * message UIDs.
 * <p>
 * 
 * 
 * @author fdietz
 */
public class MailFolderCommandReference extends DefaultFolderCommandReference
		implements IMailFolderCommandReference {
	private Integer[] address;

	private IColumbaMessage message;

	private int markVariant;

	private String folderName;
	private String folderType;
	
	private int colorValue;

	private File destFile;

	/**
	 * Constructor for MailFolderCommandReference.
	 * 
	 * @param folder
	 */
	public MailFolderCommandReference(IFolder folder) {
		super(folder);
	}

	public MailFolderCommandReference(IFolder folder, IFolder destinationFolder) {
		super(folder, destinationFolder);
	}

	public MailFolderCommandReference(IFolder folder, IColumbaMessage message) {
		super(folder);

		this.message = message;
	}
	
	/**
	 * Constructor for MailFolderCommandReference.
	 * 
	 * @param folder
	 * @param uids
	 */
	public MailFolderCommandReference(IFolder folder, Object[] uids) {
		super(folder, uids);

	}

	/**
	 * Constructor for MailFolderCommandReference.
	 * 
	 * @param folder
	 * @param uids
	 */
	public MailFolderCommandReference(IFolder sourceFolder,
			IMailFolder destinationFolder, Object[] uids) {
		super(sourceFolder, destinationFolder, uids);
	}

	/**
	 * Constructor for MailFolderCommandReference.
	 * 
	 * @param folder
	 * @param uids
	 * @param address
	 */
	public MailFolderCommandReference(IFolder folder, Object[] uids,
			Integer[] address) {
		super(folder, uids);

		this.address = address;
	}


	public Integer[] getAddress() {
		return address;
	}
	
	public void setAddress(Integer[] address) {
		this.address = address;
	}


	public IColumbaMessage getMessage() {
		return message;
	}

	public void setMessage(IColumbaMessage message) {
		this.message = message;
	}

	public void reduceToFirstUid() {
		Object[] uids = getUids();
		
		if (uids == null) {
			return;
		}

		int size = Array.getLength(uids);

		if (size > 1) {
			Object[] oneUid = new Object[1];
			oneUid[0] = uids[0];
			uids = oneUid;
		}
	}

	/**
	 * Returns the markVariant.
	 * 
	 * @return int
	 */
	public int getMarkVariant() {
		return markVariant;
	}

	/**
	 * Sets the markVariant.
	 * 
	 * @param markVariant
	 *            The markVariant to set
	 */
	public void setMarkVariant(int markVariant) {
		this.markVariant = markVariant;
	}

	/**
	 * Returns the folderName.
	 * 
	 * @return String
	 */
	public String getFolderName() {
		return folderName;
	}

	/**
	 * Sets the folderName.
	 * 
	 * @param folderName
	 *            The folderName to set
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

	/**
	 * @return Returns the colorValue.
	 */
	public int getColorValue() {
		return colorValue;
	}

	/**
	 * @param colorValue
	 *            The colorValue to set.
	 */
	public void setColorValue(int colorValue) {
		this.colorValue = colorValue;
	}

	/**
	 * @return Returns the folderType.
	 */
	public String getFolderType() {
		return folderType;
	}
	/**
	 * @param folderType The folderType to set.
	 */
	public void setFolderType(String folderType) {
		this.folderType = folderType;
	}
}