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

package org.columba.core.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * The model that needs to be instanciated if you want to create CloneInputStreams from a master.
 * 
 * @author Timo Stich <tstich@users.sourceforge.net>
 */
public class CloneStreamMaster {

	private InputStream master;
	private int[] streampos;
	private int masterpos;
	private int nextId;
	private List streamList;
	private File tempFile;
	private FileOutputStream tempOut;
	private static int uid = 0;
	private byte[] copyBuffer;

	
	/**
	 * Constructs a CloneStreamMaster. Note that ehe master must NOT be read from after
	 * the construction!
	 * 
	 * @param master
	 */
	public CloneStreamMaster(InputStream master) throws IOException {
		super();
		this.master = master;
		streampos = new int[2];
		
		streamList = new ArrayList(2);
		
		tempFile = File.createTempFile("columba-stream-clone" + (uid++), ".tmp");
		// make sure file is deleted automatically when closing VM
		tempFile.deleteOnExit();
		tempOut = new FileOutputStream( tempFile );
		
		copyBuffer = new byte[8000];

	}
	
	/**
	 * Gets a new clone of the master.
	 * 
	 * @return Clone of the master
	 */
	public CloneInputStream getClone() {
		// Ensure that there are enough pos counters 
		if( streampos.length <= nextId ) {
			int[] oldpos = streampos;
			streampos = new int[oldpos.length + 2];
			System.arraycopy(oldpos,0,streampos,0,oldpos.length);
		}
		
		try {
			// add a new inputstream to read from
			streamList.add(new FileInputStream(tempFile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			// only if tempfile was corrupted
		}
		
		return new CloneInputStream(this, nextId++);
	}

	public int read(int id) throws IOException {
		if( streampos[id] >= masterpos ) {
			// read next block in tempfile
			masterpos += bufferNextBlock();			
		}
		
		streampos[id]++;
		return ((FileInputStream)streamList.get(id)).read();
	}
	
	public int read(int id, byte[] out, int offset, int length ) throws IOException {
		while( streampos[id] + length >= masterpos ) {
			// read next block in tempfile
			int read = bufferNextBlock();
			if( read == 0) break;
			masterpos += read;		
		}
		
		streampos[id] += length;
		return ((FileInputStream)streamList.get(id)).read(out,offset,length);
	}
	
	private int bufferNextBlock() throws IOException {
		int length = master.read(copyBuffer);
		if (length == -1 ) return 0;
		tempOut.write(copyBuffer,0,length);
		return length;
	}

	/**
	 * @return
	 */
	public int available() throws IOException {
		return master.available();
	}
}
