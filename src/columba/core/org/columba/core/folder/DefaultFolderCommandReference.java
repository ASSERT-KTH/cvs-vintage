// The contents of this file are subject to the Mozilla Public License Version
// 1.1
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
package org.columba.core.folder;

/**
 * @author fdietz
 *
 */
public class DefaultFolderCommandReference implements IFolderCommandReference {

	private IFolder sourceFolder;
	private IFolder destinationFolder;
	private Object[] uids;
	
	/**
	 * 
	 */
	public DefaultFolderCommandReference() {
		super();
	}
	
	/**
	 * 
	 */
	public DefaultFolderCommandReference(IFolder sourceFolder) {
		super();
		
		this.sourceFolder = sourceFolder;
	}
	
	/**
	 * 
	 */
	public DefaultFolderCommandReference(IFolder sourceFolder, IFolder destinationFolder) {
		this(sourceFolder);
		
		this.destinationFolder = destinationFolder;
	}
	
	/**
	 * 
	 */
	public DefaultFolderCommandReference(IFolder sourceFolder, Object[] uids) {
		this(sourceFolder);
		
		this.sourceFolder = sourceFolder;
		this.uids = uids;
	}
	
	/**
	 * 
	 */
	public DefaultFolderCommandReference(IFolder sourceFolder, IFolder destinationFolder, Object[] uids) {
		this(sourceFolder, destinationFolder);
		
		this.uids = uids;
	}

	/**
	 * @see org.columba.core.sourceFolder.IFolderCommandReference#getSourceFolder()
	 */
	public IFolder getSourceFolder() {
		return sourceFolder;
	}

	/**
	 * @see org.columba.core.sourceFolder.IFolderCommandReference#setSourceFolder(org.columba.core.sourceFolder.IFolder)
	 */
	public void setSourceFolder(IFolder folder) {
		this.sourceFolder = folder;
	}

	/**
	 * @see org.columba.core.sourceFolder.IFolderCommandReference#getDestinationFolder()
	 */
	public IFolder getDestinationFolder() {
		return destinationFolder;
	}

	/**
	 * @see org.columba.core.sourceFolder.IFolderCommandReference#setDestinationFolder(org.columba.core.sourceFolder.IFolder)
	 */
	public void setDestinationFolder(IFolder destinationFolder) {
		this.destinationFolder = destinationFolder;
	}

	/**
	 * @see org.columba.api.command.ICommandReference#tryToGetLock(java.lang.Object)
	 */
	public boolean tryToGetLock(Object locker) {
		return sourceFolder.tryToGetLock(locker);
	}

	/**
	 * @see org.columba.api.command.ICommandReference#releaseLock(java.lang.Object)
	 */
	public void releaseLock(Object locker) {
		sourceFolder.releaseLock(locker);
	}

	/**
	 * @see org.columba.core.folder.IFolderCommandReference#getUids()
	 */
	public Object[] getUids() {
		return uids;
	}

	/**
	 * @see org.columba.core.folder.IFolderCommandReference#setUids(java.lang.Object[])
	 */
	public void setUids(Object[] uids) {
		this.uids = uids;
	}

}
