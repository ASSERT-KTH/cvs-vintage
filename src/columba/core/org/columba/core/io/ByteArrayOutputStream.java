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

import java.io.IOException;
import java.io.OutputStream;

/**
 * Writes to a fixed size byte[].
 * 
 * @author Timo Stich <tstich@users.sourceforge.net>
 */
public class ByteArrayOutputStream extends OutputStream {

	private byte[] buffer;
	private int pos;

	/**
	 * Constructs a ByteArrayOutputStream.
	 * 
	 * @param buffer the buffer to write to
	 */
	public ByteArrayOutputStream(byte[] buffer) {
		this.buffer = buffer;
		
		pos = 0;
	}
	
	/**
	 * Constructs a ByteArrayOutputStrem and allocates a byte[].
	 * You can access the byte[] with {@link #getBuffer()}.
	 * 
	 * @param size of the byte[]
	 */
	public ByteArrayOutputStream(int size) {
		this( new byte[size]);
	}

	/** 
	 * @see java.io.OutputStream#write(int)
	 */
	public void write(int b) throws IOException {
		buffer[pos++] = (byte)b;
	}

	/**
	 * @see java.io.OutputStream#close()
	 */
	public void close() throws IOException {
		buffer = null;
	}

	/**
	 * @see java.io.OutputStream#write(byte[], int, int)
	 */
	public void write(byte[] b, int off, int len) throws IOException {
		System.arraycopy(b,off,buffer,pos,len);
		pos += len;
	}

	/**
	 * @return
	 */
	public byte[] getBuffer() {
		return buffer;
	}

}
