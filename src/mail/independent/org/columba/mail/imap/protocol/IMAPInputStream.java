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
package org.columba.mail.imap.protocol;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.columba.core.command.StatusObservable;

/**
 * 
 * This InputStream handles all IMAP specific features like literals
 * in an transparent way.
 * <p>
 * 
 * See ArgumentWriter for more information.
 * 
 * Additionally you can register a StatusObserver, to get detailed
 * progress information of longer tasks.
 * 
 * @author fdietz 
 */
public class IMAPInputStream extends BufferedInputStream {

	private static final int increment = 256;

	// buffer for data 
	private byte[] buffer = null;

	// buffer size
	private int sz = 0;

	// position
	private int idx = 0;
	
	private StatusObservable observable;

	/**
	 * @see java.io.FilterInputStream#FilterInputStream(InputStream)
	 */
	/**
	 * Constructor.
	 */
	public IMAPInputStream(InputStream in) {
		super(in, 2 * 1024);
	}

	/**
	 * Method readResponse.
	 * @return String
	 * @throws IOException
	 */
	/**
	 * Read a Response from the InputStream.
	 * @return ByteArray that contains the Response
	 */
	public String readResponse()
		throws IOException {
		buffer = new byte[128];
		idx = 0;
		sz = 128;

		readResponseString();

		return new String(buffer, 0, idx, "ISO8859_1");
	}

	/**
	 * Method read.
	 * 
	 * read data from the inputstream and move it to
	 * global byte[] buffer.
	 * 
	 * 
	 * @throws IOException
	 * 
	 */
	private void readResponseString()
		throws IOException {

		int b = 0;
		boolean lineHasCRLF = false;

		// read line from inputstream which is ended by CRLF
		while (!lineHasCRLF
			&& ((b = (pos >= count) ? read() : (buf[pos++] & 0xff)) != -1)) {

			if (b == '\n') {
				if ((idx > 0) && (buffer[idx - 1] == '\r'))
					lineHasCRLF = true;
			}
			if (idx >= sz)
				growBuffer(increment);
			buffer[idx++] = (byte) b;

		}

		// failure while reading next byte from inputstream
		if (b == -1)
			throw new IOException();

		// see if we find a literal
		//
		// example:
		//
		// SERVER:* 147 FETCH (UID 149 BODY[2] {2750}
		if (idx >= 5 && buffer[idx - 3] == '}') {
			int i;

			// search for '{'
			for (i = idx - 4; i >= 0; i--)
				if (buffer[i] == '{')
					break;

			// no left curl found
			if (i < 0)
				return;

			int count = 0;

			// found a literal {2750}
			// -> read count = 2750
			try {
				count = parseInt(buffer, i + 1, idx - 3);
			} catch (NumberFormatException e) {
				return;
			}

			// read 'count' bytes
			//  in our example this is 2750
			if (count > 0) {
				if (getObservable() != null) {

					getObservable().setMax(count);

					getObservable().setCurrent(0);
				}

				// space left in buffer
				int avail = sz - idx;

				// we need to grow the buffer
				if (count > avail) {
					if (increment > count - avail)
						growBuffer(increment);
					else
						growBuffer(count - avail);
				}

				// read all pending bytes from inputstream
				int actual;
				while (count > 0) {
					actual = read(buffer, idx, count);
					count -= actual;
					idx += actual;

					if (getObservable() != null) {
						getObservable().setCurrent(idx);
					}
				}
			}

			// we don't stop until we find CRLF 
			readResponseString();
		}
		return;
	}

	/**
	 * Method growBuffer.
	 * 
	 * make buffer bigger by increment
	 * 
	 * @param ii	int
	 */
	private void growBuffer(int i) {
		byte[] nbuf = new byte[sz + i];
		if (buffer != null)
			System.arraycopy(buffer, 0, nbuf, 0, idx);
		buffer = nbuf;
		sz += i;
	}

	public static int parseInt(byte[] b, int start, int end)
		throws NumberFormatException {

		int radix = 10;

		if (b == null)
			throw new NumberFormatException("null");

		int result = 0;
		boolean negative = false;
		int i = start;
		int limit;
		int multmin;
		int digit;

		if (end > start) {
			if (b[i] == '-') {
				negative = true;
				limit = Integer.MIN_VALUE;
				i++;
			} else {
				limit = -Integer.MAX_VALUE;
			}
			multmin = limit / radix;
			if (i < end) {
				digit = Character.digit((char) b[i++], radix);
				if (digit < 0) {
					throw new NumberFormatException(
						"illegal number: " + new String(b, start, end));
				} else {
					result = -digit;
				}
			}
			while (i < end) {

				digit = Character.digit((char) b[i++], radix);
				if (digit < 0) {
					throw new NumberFormatException("illegal number");
				}
				if (result < multmin) {
					throw new NumberFormatException("illegal number");
				}
				result *= radix;
				if (result < limit + digit) {
					throw new NumberFormatException("illegal number");
				}
				result -= digit;
			}
		} else {
			throw new NumberFormatException("illegal number");
		}
		if (negative) {
			if (i > start + 1) {
				return result;
			} else { /* Only got "-" */
				throw new NumberFormatException("illegal number");
			}
		} else {
			return -result;
		}
	}

	/**
	 * @return
	 */
	public StatusObservable getObservable() {
		return observable;
	}

	/**
	 * @param observable
	 */
	public void setObservable(StatusObservable observable) {
		this.observable = observable;
	}

}
