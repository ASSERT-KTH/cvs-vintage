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
package org.columba.mail.folder.mh;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.columba.core.io.DiskIO;
import org.columba.core.io.StreamUtils;
import org.columba.mail.folder.DataStorageInterface;
import org.columba.mail.folder.LocalFolder;
import org.columba.ristretto.message.io.FileSource;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class MHDataStorage implements DataStorageInterface {

	protected LocalFolder folder;

	public MHDataStorage(LocalFolder folder) {
		this.folder = folder;

	}

	public void saveMessage(String source, Object uid) throws Exception {
		File file =
			new File(
				folder.getDirectoryFile() + File.separator + (Integer) uid);

		if( source == null ) {
			System.out.println( source + uid );
		}
		DiskIO.saveStringInFile(file, source);
	}

	public String loadMessage(Object uid) throws Exception {

		File file =
			new File(
				folder.getDirectoryFile()
					+ File.separator
					+ ((Integer) uid).toString());
		
		return DiskIO.readFileInString(file);
	}
	
	public boolean exists( Object uid  ) throws Exception 
	{
		File file =
			new File(
				folder.getDirectoryFile()
					+ File.separator
					+ ((Integer) uid).toString());
			
		return file.exists();
	}
	
	public void removeMessage(Object uid) {
		File file =
			new File(
				folder.getDirectoryFile()
					+ File.separator
					+ ((Integer) uid).toString());
					
		file.delete();
	}

	public int getMessageCount() {
		File[] list = folder.getDirectoryFile().listFiles(MHMessageFileFilter.getInstance());
		
		return list.length;
	}
	
	
	
	/* (non-Javadoc)
	 * @see org.columba.mail.folder.DataStorageInterface#getMessages()
	 */
	public Object[] getMessageUids() {
		
		File[] list = folder.getDirectoryFile().listFiles(MHMessageFileFilter.getInstance());
		// A list of all files that seem to be messages (only numbers in the name)

		List result = new ArrayList(list.length);//new Object[list.length];
		for( int i=0; i< list.length; i++) {
			result.add( i, new Integer(list[i].getName()) );
		}

		Collections.sort( result );

		return result.toArray();
	}

	/* (non-Javadoc)
	 * @see org.columba.mail.folder.DataStorageInterface#getFileSource(java.lang.Object)
	 */
	public FileSource getFileSource(Object uid) throws Exception {
		File file =
			new File(
				folder.getDirectoryFile()
					+ File.separator
					+ ((Integer) uid).toString());
		
		
		return new FileSource(file);
	}

	/* (non-Javadoc)
	 * @see org.columba.mail.folder.DataStorageInterface#saveInputStream(java.lang.Object, java.io.InputStream)
	 */
	public void saveInputStream(Object uid, InputStream source)
		throws Exception {
			File file =
				new File(
					folder.getDirectoryFile() + File.separator + (Integer) uid);


			OutputStream out = new FileOutputStream( file );
			
			StreamUtils.streamCopy(source, out);
			
			out.close();	
	}

}

