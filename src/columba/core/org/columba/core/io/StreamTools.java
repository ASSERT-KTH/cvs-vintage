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

/*
 * Created on 04.09.2003
 *
 */
package org.columba.core.io;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;

/**
 * @author waffel
 *
 */
public class StreamTools {
	
	/**
	 * Copied all bytes from an InputStream to an OutputStream. The Bufsize should be 8000 bytes or 16000 bytes. This
	 * is platform dependend. A higher number of bytes to read on block, blocks the operation for a greater time. 
	 * @param from InputStream from wihch the bytes are to copied.
	 * @param to OutputStream in which the bytes are copied.
	 * @param _iBufSize The Buffer size. How many bytes on block are should be copied.
	 * @return Number of bytes which are copied.
	 * @throws Exception If the Streams are unavailable.
	 */
	public static long streamCopy(InputStream from, OutputStream to, int _iBufSize) throws Exception {
		byte[] _aBuffer = new byte[_iBufSize];
		int _iBytesRead;
		long _lBytesCopied = 0;
		while ((_iBytesRead = from.read(_aBuffer)) > 0 ) {
			to.write(_aBuffer, 0, _iBytesRead);
			_lBytesCopied += _iBytesRead;
		}
		return _lBytesCopied;
	}
	
}
