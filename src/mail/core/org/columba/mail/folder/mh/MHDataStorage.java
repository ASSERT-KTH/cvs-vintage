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
import java.lang.reflect.Array;
import java.util.Vector;

import org.columba.core.command.WorkerStatusController;
import org.columba.core.io.DiskIO;
import org.columba.mail.folder.DataStorageInterface;
import org.columba.mail.folder.LocalFolder;
import org.columba.mail.message.AbstractMessage;
import org.columba.mail.message.ColumbaHeader;
import org.columba.mail.message.HeaderList;
import org.columba.mail.message.Message;
import org.columba.mail.parser.Rfc822Parser;

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

	public HeaderList recreateHeaderList( WorkerStatusController worker ) throws Exception {
		System.out.println("mh-datastorage->recreateheaderList");

		HeaderList headerList = new HeaderList();

		if (folder == null) {
			return null;
        }
        if ( worker != null ) {
            worker.setDisplayText("Recreating Header-Cache");
        }
		if (folder.getDirectoryFile() == null) {
			System.out.println("directory-file == null");
			return null;
		}

		//System.out.println("dir-file=" + folder.getDirectoryFile());

		folder.getSearchEngineInstance().reset();

		File[] list = folder.getDirectoryFile().listFiles(MHMessageFileFilter.getInstance());
		Vector v = new Vector();
		//System.out.println("message-count=" + list.length);

        // This "rename all mh files in folder" operation amounts to a disk-based mutex
        //   and could obviously leave the disk in a bad state.

		for (int i = 0; i < Array.getLength(list); i++) {
			File file = list[i];
			File renamedFile;
			String name = file.getName();
			//System.out.println("name="+name);
			
			if ( name.equals(".") || name.equals("..") ) continue;
			if ( name.startsWith(".") ) continue;
			

			if ((file.exists()) && (file.length() > 0)) {
				/*
                if (file.getName().indexOf('~') >= 0) {
                    // "rename all mh files in folder" either recursing or previously corrupted
                    throw new RuntimeException("\"rename all mh files in folder\" either recursing or separate process or previously corrupted, thread =" + Thread.currentThread().getName());
                }
                */
				renamedFile =
					new File(file.getParentFile(), file.getName() + '~');
				file.renameTo(renamedFile);
				v.add(renamedFile);
			}

		}

		// parse all message files to recreate the header cache
		Rfc822Parser parser = new Rfc822Parser();
		ColumbaHeader header;
		
		if ( worker != null )
		worker.setProgressBarMaximum(v.size());
		
		for (int i = 0; i < v.size(); i++) {
			File file = (File) v.get(i);
            // rename to sequence number (so all mewssages in this folder are renumbered)
            Integer iFileName = new Integer(i);
			file.renameTo(
				new File(file.getParentFile(), iFileName.toString()));

			try {
				String source = loadMessage(iFileName);
				
				//System.out.println("--------------->\nsource-message=" + source);

				header = parser.parseHeader(source);

				AbstractMessage m = new Message(header);
				ColumbaHeader h = (ColumbaHeader) m.getHeader();

				parser.addColumbaHeaderFields(h);

				Integer sizeInt = new Integer(source.length());
				int size = Math.round(sizeInt.intValue() / 1024);
				h.set("columba.size", new Integer(size));
				
				h.set("columba.uid", iFileName);
				
				if ( h.get("columba.flags.recent").equals(Boolean.TRUE) ) folder.getMessageFolderInfo().incRecent();
				if ( h.get("columba.flags.seen").equals(Boolean.FALSE)  ) folder.getMessageFolderInfo().incUnseen();
				
				m.setSource(source);
				
				folder.getMessageFolderInfo().incExists();
			
				headerList.add(header, iFileName);
				folder.getSearchEngineInstance().messageAdded(m);
				
				m.freeMemory();
				
				if ( worker != null ) {
				worker.incProgressBarValue();
                }
			} catch (Exception ex) {
				System.out.println(
					"recreateIndex, working on item " + iFileName + " in " + file.getParentFile() + "  exception: " + ex.getMessage());
				ex.printStackTrace();
			}

		}

		return headerList;
	}
	
	
	
	public int getMessageCount() {
		File[] list = folder.getDirectoryFile().listFiles(MHMessageFileFilter.getInstance());
		
		return list.length;
	}
	
	
	
}

