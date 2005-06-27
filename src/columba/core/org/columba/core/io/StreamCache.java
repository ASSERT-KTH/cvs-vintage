package org.columba.core.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class StreamCache {
	
	public static final long DEFAULT_SIZE = 10 * 1024 * 1024;//Byte
	
	private File dir;
	private Map contents;
	private List fifo;
	
	private long actSize;
	
	private long maxSize;
	
	public StreamCache(File directory) {
		this(directory, DEFAULT_SIZE);
	}
	
	public StreamCache(File directory, long maxSize) {
		dir = directory;
		if( !dir.exists() ) {
			if( !dir.mkdirs()) {
				throw new RuntimeException(dir.toString() + " could not be created!");
			}
		}		
		actSize = 0;
		fifo = new ArrayList(100);
		
		this.maxSize = maxSize;
	}
	
	public InputStream passiveAdd(Object key, InputStream in ) throws IOException {
		File streamFile = new File(dir.getAbsoluteFile() + File.separator + key.toString() + ".cache");
		return new StreamCacheCopyStream(in, key, streamFile, this);		
	}

	void add(Object key, File out) {
		fifo.add(new CacheEntry(key, out));
		
		actSize += out.length();
		
		Collections.sort( fifo, new Comparator() {

			public int compare(Object arg0, Object arg1) {
				Date a = ((CacheEntry) arg0).lastAccess;
				Date b = ((CacheEntry) arg1).lastAccess;
				
				if( a.before(b)) return 1;
				if( a.after(b)) return -1;
				return 0;
			}
			
		});
		
		ensureMaxSize();
	}

	private void ensureMaxSize() {
		while( actSize > maxSize ) {
			CacheEntry entry = (CacheEntry) fifo.remove(fifo.size()-1);
			actSize -= entry.file.length();			
			entry.file.delete();
		}
	}
	
	
	public InputStream get(Object key) {
		Iterator it = fifo.iterator();
		
		while(it.hasNext()) {
			CacheEntry c = (CacheEntry) it.next();
			if( c.key.equals(key)) {
				try {
					return c.createStream();
				} catch (FileNotFoundException e) {
					it.remove();
					actSize -= c.file.length();
				}
			}
		}
		
		return null;
	}

	public long getActSize() {
		return actSize;
	}

	
	public void clear() {
		Iterator it = fifo.iterator();
		
		while(it.hasNext()) {
			CacheEntry c = (CacheEntry) it.next();
			if( !c.file.delete() ) {
				// Try again after shutdown
				c.file.deleteOnExit();
			}
			it.remove();
		}
		
		actSize = 0;
	}

	public long getMaxSize() {
		return maxSize;
	}

	public void setMaxSize(long maxSize) {
		this.maxSize = maxSize;
		ensureMaxSize();
	}
}


class CacheEntry {
	Date lastAccess;
	Object key;
	File file;
	
	public CacheEntry(Object key, File file) {
		this.key = key;
		this.file = file;
		this.lastAccess = new Date();
	}
	
	public InputStream createStream() throws FileNotFoundException {
		this.lastAccess = new Date();
		return new FileInputStream(file);
	}
}