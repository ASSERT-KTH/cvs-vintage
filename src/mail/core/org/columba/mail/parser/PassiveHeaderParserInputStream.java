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

package org.columba.mail.parser;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.columba.core.util.Barrier;
import org.columba.core.util.Mutex;
import org.columba.ristretto.io.CharSequenceSource;
import org.columba.ristretto.message.Header;
import org.columba.ristretto.parser.HeaderParser;
import org.columba.ristretto.parser.ParserException;

/**
 * FilterInputStream that passively reads and parses 
 * a Header from the stream. 
 * 
 * @author Timo Stich <tstich@users.sourceforge.net>
 */
public class PassiveHeaderParserInputStream extends FilterInputStream {

	private static final int READING = 0;
	private static final int DONE = 1;
	private static final String HEADER_END = "\r\n\r\n";
	
	
	private StringBuffer buffer;
	private int mode;
	private Header header;
	
	private Mutex mutex;
	private Barrier barrier;
	
	/**
	 * Constructs the PassiveHeaderParserInputStream.
	 * 
	 * @param arg0 the message stream
	 */
	public PassiveHeaderParserInputStream(InputStream arg0) {
		super(arg0);
		
		buffer = new StringBuffer();
		
		mutex= new Mutex();
		barrier = new Barrier();
	}

	public int read() throws IOException {
		int character = super.read();
		
		if( mode == READING ) {
			buffer.append((char)character);
			checkHeaderReadComplete();
		}
		
		return character;
	}

	private void checkHeaderReadComplete() {
		mutex.lock();
		if( buffer.indexOf(HEADER_END) != -1) {
			mode = DONE;
			try {
				// do the parsing
				header = HeaderParser.parse(new CharSequenceSource(buffer));
			} catch (ParserException e) {
				//TODO do something
			}
			barrier.open();
			buffer = null;
		}
		mutex.release();
	}

	public int read(byte[] arg0, int arg1, int arg2) throws IOException {
		int read = super.read(arg0, arg1, arg2);
		
		if( mode == READING ) {
			buffer.append(new String(arg0,"US-ASCII").toCharArray(), 0, read);
			checkHeaderReadComplete();
		}
		
		return read;
	}
	
	
	/**
	 * Checks if the Header is already available.
	 * 
	 * @return true if the Header is parsed
	 */
	public boolean isHeaderAvailable() {
		return mode == DONE;
	}

	/**
	 * This call blocks until the Header is parsed.
	 * 
	 * @return Returns the header.
	 */
	public Header getHeader() {
		barrier.join();
		
		return header;
	}
}
