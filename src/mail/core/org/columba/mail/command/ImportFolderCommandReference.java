/*
 * Created on 24.03.2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.command;

import java.io.File;

import org.columba.mail.folder.FolderTreeNode;
import org.columba.mail.folder.mailboximport.DefaultMailboxImporter;

/**
 * @author frd
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ImportFolderCommandReference extends FolderCommandReference {

	File[] sourceFiles;
	DefaultMailboxImporter importer;

	/**
	 * @param folder
	 */
	public ImportFolderCommandReference(FolderTreeNode folder) {
		super(folder);
		
	}

	/**
	 * @param folder
	 * @param message
	 */
	public ImportFolderCommandReference(
		FolderTreeNode folder,
		File[] sourceFiles,
		DefaultMailboxImporter importer) {
		super(folder);
		this.sourceFiles = sourceFiles;
		this.importer = importer;
		
	}

	

	/**
	 * @return DefaultMailboxImporter
	 */
	public DefaultMailboxImporter getImporter() {
		return importer;
	}

}
