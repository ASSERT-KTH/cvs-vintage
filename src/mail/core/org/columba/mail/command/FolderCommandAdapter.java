/*
 * Created on 12.02.2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package org.columba.mail.command;

import org.columba.mail.folder.Folder;

/**
 * @author frd
 */
public class FolderCommandAdapter {

	protected FolderCommandReference[] c;
	protected int length;
	
	public FolderCommandAdapter( FolderCommandReference[] c)
	{
		this.c = c;
		if ( c.length<3 ) length= 3;
		else length = c.length;
	}
	
	public FolderCommandReference[] getSourceFolderReferences()
	{
		FolderCommandReference[] result = new FolderCommandReference[length-2];
		
		System.arraycopy(c,0, result, 0, length-2);
		
		return result;
		
	}
	
	public Folder getDestinationFolder()
	{
		return (Folder) c[ length-2].getFolder();
	}
	
	public FolderCommandReference getUpdateReferences()
	{
		
		// shouldn't this be <=2 ?
		// -> this fixes the VirtualFolder doesn't update bug for me
		if ( c.length<=2 ) return null;
		//if ( c.length==2 ) return null;
		
		return c[ c.length-1 ];
	}
	
	
}
