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
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class StreamUtils {
	
	private static final int BUFFERSIZE = 8000;
	private static final Charset _8BitCharset = Charset.forName("ISO-8859-1");

	/**
	 * Copied all bytes from an InputStream to an OutputStream. The Bufsize should be 8000 bytes or 16000 bytes. This
	 * is platform dependend. A higher number of bytes to read on block, blocks the operation for a greater time. 
	 * @param _isInput InputStream from wihch the bytes are to copied.
	 * @param _osOutput OutputStream in which the bytes are copied.
	 * @param _iBufSize The Buffer size. How many bytes on block are should be copied.
	 * @return Number of bytes which are copied.
	 * @throws Exception If the Streams are unavailable.
	 */
	public static long streamCopy(InputStream _isInput, OutputStream _osOutput, int _iBufSize) throws IOException {
		byte[] _aBuffer = new byte[_iBufSize];
		int _iBytesRead;
		long _lBytesCopied = 0;
		while ((_iBytesRead = _isInput.read(_aBuffer)) > 0 ) {
			_osOutput.write(_aBuffer, 0, _iBytesRead);
			_lBytesCopied += _iBytesRead;
		}
		return _lBytesCopied;
	}

	/**
	 * Copied all bytes from an InputStream to an OutputStream. The Bufsize is set to 8000 bytes.  
	 * @param _isInput InputStream from wihch the bytes are to copied.
	 * @param _osOutput OutputStream in which the bytes are copied.
	 * @return Number of bytes which are copied.
	 * @throws Exception If the Streams are unavailable.
	 */
	public static long streamCopy(InputStream _isInput, OutputStream _osOutput ) throws IOException {
		return streamCopy( _isInput, _osOutput, BUFFERSIZE );
	}

	
	/**
	 * Reads a InputStream into a StringBuffer.
	 * This method is 8bit safe.
	 * 
	 * @param in the InputStream to read from
	 * @return the interpreted InputStream
	 * @throws IOException
	 */
	public static StringBuffer readInString( InputStream in ) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(BUFFERSIZE);
		StringBuffer result = new StringBuffer();
		int read = in.read( buffer.array() );
		
		while( read > 0 ) {
			buffer.limit( read );
			result.append( _8BitCharset.decode(buffer));

			buffer.clear();
			read = in.read( buffer.array() );
		}
		
		return result;		
	}

}
