package org.columba.mail.folder.mh;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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

		StringBuffer strbuf = new StringBuffer();

		BufferedReader in = new BufferedReader(new FileReader(file));
		String str;
		strbuf = new StringBuffer();

		while ((str = in.readLine()) != null) {
			strbuf.append(str + "\n");
		}

		in.close();

		return strbuf.toString();
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

		if (folder == null)
			return null;

		if (folder.getDirectoryFile() == null) {
			System.out.println("directory-file == null");
			return null;
		}

		//System.out.println("dir-file=" + folder.getDirectoryFile());

		File[] list = folder.getDirectoryFile().listFiles();
		Vector v = new Vector();
		//System.out.println("message-count=" + list.length);

		for (int i = 0; i < Array.getLength(list); i++) {
			File file = list[i];
			File renamedFile;
			String name = file.getName();
			//System.out.println("name="+name);
			
			if ( name.equals(".") || name.equals("..") ) continue;
			if ( name.startsWith(".") ) continue;
			

			if ((file.exists()) && (file.length() > 0)) {
				renamedFile =
					new File(file.getParentFile(), file.getName() + '~');
				file.renameTo(renamedFile);
				v.add(renamedFile);
			}

		}

		// parse all message files to recreate the header cache
		Rfc822Parser parser = new Rfc822Parser();
		ColumbaHeader header;
		
		worker.setProgressBarMaximum(v.size());
		
		for (int i = 0; i < v.size(); i++) {
			File file = (File) v.get(i);
			file.renameTo(
				new File(file.getParentFile(), (new Integer(i)).toString()));

			try {
				String source = loadMessage(new Integer(i));
				
				//System.out.println("--------------->\nsource-message=" + source);

				header = parser.parseHeader(source);

				AbstractMessage m = new Message(header);
				ColumbaHeader h = (ColumbaHeader) m.getHeader();

				parser.addColumbaHeaderFields(h);

				Integer sizeInt = new Integer(source.length());
				int size = Math.round(sizeInt.intValue() / 1024);
				h.set("columba.size", new Integer(size));
				
				h.set("columba.uid", new Integer(i) );
				
				if ( h.get("columba.flags.recent").equals(Boolean.TRUE) ) folder.getMessageFolderInfo().incRecent();
				if ( h.get("columba.flags.seen").equals(Boolean.FALSE)  ) folder.getMessageFolderInfo().incUnseen();
				
				m.setSource(source);
				
				folder.getMessageFolderInfo().incExists();
			
				headerList.add(header, new Integer(i));
				folder.getSearchEngineInstance().messageAdded(m);
				
				m.freeMemory();
				
				worker.incProgressBarValue();

			} catch (Exception ex) {
				System.out.println(
					"recreateIndex exception: " + ex.getMessage());
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

