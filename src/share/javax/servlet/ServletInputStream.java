
/*
 * $Id: ServletInputStream.java,v 1.1 1999/10/09 00:20:29 duncan Exp $
 * 
 * Copyright (c) 1995-1999 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the confidential and proprietary information of Sun
 * Microsystems, Inc. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Sun.
 * 
 * SUN MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. SUN SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 * 
 * CopyrightVersion 1.0
 */

package javax.servlet;

import java.io.InputStream;
import java.io.IOException;

/**
 * 
 * Provides an input stream for reading binary data from a client
 * request, including an efficient <code>readLine</code> method
 * for reading data one line at a time. With some protocols, such
 * as HTTP POST and PUT, a <code>ServletInputStream</code>
 * object can be used to read data sent from the client.
 *
 * <p>A <code>ServletInputStream</code> object is normally retrieved via
 * the {@link ServletRequest#getInputStream} method.
 *
 *
 * <p>This is an abstract class that a servlet container implements.
 * Subclasses of this class
 * must implement the <code>java.io.InputStream.read()</code> method.
 *
 *
 * @author 	Various
 * @version 	$Version$
 *
 * @see		ServletRequest 
 *
 */

public abstract class ServletInputStream extends InputStream {



    /**
     * Does nothing, because this is an abstract class.
     *
     */

    protected ServletInputStream() { }

  
  
    
    /**
     *
     * Reads the input stream, one line at a time. Starting at an
     * offset, reads bytes into an array, until it reads a certain number
     * of bytes or reaches a newline character, which it reads into the
     * array as well.
     *
     * <p>This method returns -1 if it reaches the end of the input
     * stream before reading the maximum number of bytes.
     *
     *
     *
     * @param b 		an array of bytes into which data is read
     *
     * @param off 		an integer specifying the character at which
     *				this method begins reading
     *
     * @param len		an integer specifying the maximum number of 
     *				bytes to read
     *
     * @return			an integer specifying the actual number of bytes 
     *				read, or -1 if the end of the stream is reached
     *
     * @exception IOException	if an input or output exception has occurred
     *
     */
     
    public int readLine(byte[] b, int off, int len) throws IOException {

	if (len <= 0) {
	    return 0;
	}
	int count = 0, c;

	while ((c = read()) != -1) {
	    b[off++] = (byte)c;
	    count++;
	    if (c == '\n' || count == len) {
		break;
	    }
	}
	return count > 0 ? count : -1;
    }
}



