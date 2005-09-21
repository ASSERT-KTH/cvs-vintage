package org.columba.mail.folder.zippedmh;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.columba.core.io.StreamUtils;
import org.columba.mail.folder.IDataStorage;
import org.columba.ristretto.io.Source;
import org.columba.ristretto.io.SourceInputStream;
import org.columba.ristretto.io.TempSourceFactory;

public class ZippedMHDataStorage implements IDataStorage {

	private File directory; 

	private Source actSource;
	private Object actUid;
	
	
	public ZippedMHDataStorage(File directory) {
		this.directory =  directory;
	}

	public void removeMessage(Object uid) throws Exception {
		File messageFile = getMessageFile(uid);
		
		if( !messageFile.delete() ) {
			messageFile.deleteOnExit();
		}
		
		if( actUid != null && actUid.equals(uid)) {
			actUid = null;
		}
	}

	public Source getMessageSource(Object uid) throws Exception {
		return inflateToTempSource(uid);
	}

	public InputStream getMessageStream(Object uid) throws Exception {
		return new SourceInputStream(inflateToTempSource(uid));
	}
	
	private Source inflateToTempSource(Object uid) throws Exception {
		if( actUid == null || !actUid.equals(uid)) {		
			ZipFile messageFile = new ZipFile(getMessageFile(uid));
			ZipEntry entry = messageFile.getEntry(uid.toString());
			
			actUid = uid;
			actSource =TempSourceFactory.createTempSource(messageFile.getInputStream(entry), (int)entry.getSize());
		}
		
		return actSource; 
	}

	public void saveMessage(Object uid, InputStream source) throws Exception {
		File messageFile = getMessageFile(uid);
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(messageFile));
		out.setMethod(ZipOutputStream.DEFLATED);
		
		ZipEntry zipentry = new ZipEntry(uid.toString());
		zipentry.setSize(source.available());
		out.putNextEntry(zipentry);
		
		StreamUtils.streamCopy(source, out);
		
		source.close();
		out.close();		
	}

	private File getMessageFile(Object uid) {
		File messageFile = new File(directory, uid + ".zip");
		return messageFile;
	}

	public int getMessageCount() {
		return directory.listFiles(new FileFilter() {

			public boolean accept(File pathname) {
				return pathname.getName().matches("\\d*\\.zip");
			}
			
		}).length;
	}

	public boolean exists(Object uid) throws Exception {		
		return getMessageFile(uid).exists();
	}

	public Object[] getMessageUids() {
		File[] messageFiles = directory.listFiles(new FileFilter() {

			public boolean accept(File pathname) {
				return pathname.getName().matches("\\d*\\.zip");
			}
			
		});
		
		Matcher matcher = Pattern.compile("(\\d+)").matcher("");
		
		Object[] uids = new Object[messageFiles.length];
		for( int i=0; i<uids.length; i++) {
			matcher.reset(messageFiles[i].getName());
			matcher.find(); 
			uids[i] = new Integer(matcher.group(1));
		}
		
		return uids;
	}

}
