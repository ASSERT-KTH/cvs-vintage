/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/jasper/compiler/EscapeUnicodeWriter.java,v 1.4 2004/02/23 06:22:36 billbarker Exp $
 * $Revision: 1.4 $
 * $Date: 2004/02/23 06:22:36 $
 *
 *
 *  Copyright 1999-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.jasper.compiler;

import java.io.OutputStream;
import java.io.IOException;
import java.io.Writer;

import org.apache.jasper.Constants;

/**
 * Used to escape unicode characters with \ u's. 
 *
 * @author Anselm Baird-Smith
 */
public class EscapeUnicodeWriter extends Writer {
    private OutputStream out     = null;
    private byte         bytes[] = new byte[6];
    
    public EscapeUnicodeWriter(OutputStream out) {
	this.out = out;
    }
    
    public void write(char buf[], int off, int len) 
	throws IOException
    {
	if (out == null)
	    throw new IOException(Constants.getString("jsp.error.stream.closed"));
        
	int ci = off, end = off + len;
	while ( --len >= 0 ) {
	    int ch = buf[off++] & 0xffff;
	    /*
	     * Write out unicode characters as \u0000
	     */
	    if ((ch < 0x20 || ch > 0x7e) && 
		(ch != '\n' && ch != '\r' && ch != '\t')) {
		bytes[0] = (byte) '\\';
		bytes[1] = (byte) 'u';
		bytes[2] = (byte) Character.forDigit((ch & 0xf000) >> 12, 16);
		bytes[3] = (byte) Character.forDigit((ch & 0x0f00) >>  8, 16);
		bytes[4] = (byte) Character.forDigit((ch & 0x00f0) >>  4, 16);
		bytes[5] = (byte) Character.forDigit((ch & 0x000f) >>  0, 16);
		out.write(bytes);
	    } else {
		out.write((byte) ch);
	    }
	}
    }
    
    public void flush() throws IOException {
	out.flush();
    }
    
    public void close() 
	throws IOException
    {
	if (out == null) 
	    return;
	out.close();
	out = null;
    }
}
