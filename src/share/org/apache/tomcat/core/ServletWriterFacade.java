/*
 * ====================================================================
 * 
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights 
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:  
 *       "This product includes software developed by the 
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written 
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * [Additional notices, if required by prior licensing conditions]
 *
 */ 


package org.apache.tomcat.core;

import org.apache.tomcat.util.StringManager;
import java.io.*;
import javax.servlet.ServletOutputStream;

/**
 *  Facade to the PrintWriter returned by Response.
 *  This will grow to include more support for JSPs ( and other templating
 *  systems ) buffering requirements, provide support for accounting
 *  and will allow more control over char-to-byte conversion ( if it proves
 *  that we spend too much time in that area ).
 *
 *  This will also help us control the multi-buffering ( since all writers have
 *  8k or more of un-recyclable buffers). 
 *
 * @author Costin Manolache [costin@eng.sun.com]
 */
public class ServletWriterFacade extends PrintWriter {
    Response resA;
    RequestImpl req;
    
    protected ServletWriterFacade( Writer w, Response resp ) {
	super( w );
	this.resA=resp;
	req=(RequestImpl)resA.getRequest();
    }

    // -------------------- Write methods --------------------

    public void flush() {
	in();
	super.flush();
	out();
    }

    public void print( String str ) {
	in();
	super.print( str );
	out(); 
   }

    public void println( String str ) {
	in();
	super.print( str );
	out(); 
   }

    public void write( char buf[], int offset, int count ) {
	in();
	super.write( buf, offset, count );
	out();
    }

    public void write( String str ) {
	in();
	super.write( str );
	out();
    }

    private void in() {
	req.setAccount( RequestImpl.ACC_IN_OUT, System.currentTimeMillis() );
    }

    private void out() {
	long l=System.currentTimeMillis();
	long l1=req.getAccount( RequestImpl.ACC_IN_OUT);
	long l3=req.getAccount( RequestImpl.ACC_OUT_COUNT);
	req.setAccount( RequestImpl.ACC_OUT_COUNT, l - l1 + l3 );
    }

    /** Reuse the object instance, avoid GC
     *  Called from BSOS
     */
    void recycle() {
    }

}

// -------------------- From Crimson !

//
// Delegating to a converter module will always be slower than
// direct conversion.  Use a similar approach for any other
// readers that need to be particularly fast; only block I/O
// speed matters to this package.  For UTF-16, separate readers
// for big and little endian streams make a difference, too;
// fewer conditionals in the critical path!
//
abstract class BaseReader extends Reader
{
    protected InputStream	instream;
    protected byte		buffer [];
    protected int		start, finish;
    
    BaseReader (InputStream stream)
    {
	super (stream);
	
	instream = stream;
	buffer = new byte [8192];
    }

    public boolean ready () throws IOException
    {
	return instream == null
	    || (finish - start) > 0
	    ||  instream.available () != 0;
	}

    // caller shouldn't read again
    public void close () throws IOException
    {
	if (instream != null) {
	    instream.close ();
	    start = finish = 0;
	    buffer = null;
	    instream = null;
	}
    }
}

//
// We want this reader, to make the default encoding be as fast
// as we can make it.  JDK's "UTF8" (not "UTF-8" till JDK 1.2)
// InputStreamReader works, but 20+% slower speed isn't OK for
// the default/primary encoding.
//
final class Utf8Reader extends BaseReader
{
    // 2nd half of UTF-8 surrogate pair
    private char		nextChar;
    
    Utf8Reader (InputStream stream)
    {
	super (stream);
    }
    
    public int read (char buf [], int offset, int len) throws IOException
    {
	int i = 0, c = 0;
	
	if (len <= 0)
	    return 0;
	
	// avoid many runtime bounds checks ... a good optimizer
	// (static or JIT) will now remove checks from the loop.
	if ((offset + len) > buf.length || offset < 0)
	    throw new ArrayIndexOutOfBoundsException ();
	
	// Consume remaining half of any surrogate pair immediately
	if (nextChar != 0) {
	    buf [offset + i++] = nextChar;
	    nextChar = 0;
	}
	
	while (i < len) {
	    // stop or read data if needed
	    if (finish <= start) {
		if (instream == null) {
		    c = -1;
		    break;
		}
		start = 0;
		finish = instream.read (buffer, 0, buffer.length);
		if (finish <= 0) {
		    this.close ();
		    c = -1;
		    break;
		}
	    }
	    
	    //
	    // RFC 2279 describes UTF-8; there are six encodings.
	    // Each encoding takes a fixed number of characters
	    // (1-6 bytes) and is flagged by a bit pattern in the
	    // first byte.  The five and six byte-per-character
	    // encodings address characters which are disallowed
	    // in XML documents, as do some four byte ones.
	    // 
	    
	    //
	    // Single byte == ASCII.  Common; optimize.
	    //
	    c = buffer [start] & 0x0ff;
	    if ((c & 0x80) == 0x00) {
		// 0x0000 <= c <= 0x007f
		start++;
		buf [offset + i++] = (char) c;
		continue;
	    }
		
	    //
	    // Multibyte chars -- check offsets optimistically,
	    // ditto the "10xx xxxx" format for subsequent bytes
	    //
	    int		off = start;
		
	    try {
		// 2 bytes
		if ((buffer [off] & 0x0E0) == 0x0C0) {
		    c  = (buffer [off++] & 0x1f) << 6;
		    c +=  buffer [off++] & 0x3f;

		    // 0x0080 <= c <= 0x07ff

		    // 3 bytes
		} else if ((buffer [off] & 0x0F0) == 0x0E0) {
		    c  = (buffer [off++] & 0x0f) << 12;
		    c += (buffer [off++] & 0x3f) << 6;
		    c +=  buffer [off++] & 0x3f;

		    // 0x0800 <= c <= 0xffff

		    // 4 bytes
		} else if ((buffer [off] & 0x0f8) == 0x0F0) {
		    c  = (buffer [off++] & 0x07) << 18;
		    c += (buffer [off++] & 0x3f) << 12;
		    c += (buffer [off++] & 0x3f) << 6;
		    c +=  buffer [off++] & 0x3f;

		    // 0x0001 0000  <= c  <= 0x001f ffff

		    // Unicode supports c <= 0x0010 ffff ...
		    if (c > 0x0010ffff)
			throw new CharConversionException (
							   "UTF-8 encoding of character 0x00"
							   + Integer.toHexString (c)
							   + " can't be converted to Unicode."
							   );

		    else if (c > 0xffff) {
			// Convert UCS-4 char to surrogate pair (UTF-16)
			c -= 0x10000;
			nextChar = (char) (0xDC00 + (c & 0x03ff));
			c = 0xD800 + (c >> 10);
		    }
		    // 5 and 6 byte versions are XML WF errors, but
		    // typically come from mislabeled encodings
		} else
		    throw new CharConversionException (
						       "Unconvertible UTF-8 character"
						       + " beginning with 0x"
						       + Integer.toHexString (
									      buffer [start] & 0xff)
						       );

	    } catch (ArrayIndexOutOfBoundsException e) {
		// off > length && length >= buffer.length
		c = 0;
	    }

	    //
	    // if the buffer held only a partial character,
	    // compact it and try to read the rest of the
	    // character.  worst case involves three
	    // single-byte reads -- quite rare.
	    //
	    if (off > finish) {
		System.arraycopy (buffer, start,
				  buffer, 0, finish - start);
		finish -= start;
		start = 0;
		off = instream.read (buffer, finish,
				     buffer.length - finish);
		if (off < 0) {
		    this.close ();
		    throw new CharConversionException (
						       "Partial UTF-8 char");
		}
		finish += off;
		continue;
	    }

	    //
	    // check the format of the non-initial bytes
	    //
	    for (start++; start < off; start++) {
		if ((buffer [start] & 0xC0) != 0x80) {
		    this.close ();
		    throw new CharConversionException (
						       "Malformed UTF-8 char -- "
						       + "is an XML encoding declaration missing?"
						       );
		}
	    }

	    //
	    // If this needed a surrogate pair, consume ASAP
	    //
	    buf [offset + i++] = (char) c;
	    if (nextChar != 0 && i < len) {
		buf [offset + i++] = nextChar;
		nextChar = 0;
	    }
	}
	if (i > 0)
	    return i;
	return (c == -1) ? -1 : 0;
    }
}

//
// We want ASCII and ISO-8859 Readers since they're the most common
// encodings in the US and Europe, and we don't want performance
// regressions for them.  They're also easy to implement efficiently,
// since they're bitmask subsets of UNICODE.
//
// XXX haven't benchmarked these readers vs what we get out of JDK.
//
final class AsciiReader extends BaseReader
{
    AsciiReader (InputStream in) { super (in); }

    public int read (char buf [], int offset, int len) throws IOException
    {
	int		i, c;

	if (instream == null)
	    return -1;

	// avoid many runtime bounds checks ... a good optimizer
	// (static or JIT) will now remove checks from the loop.
	if ((offset + len) > buf.length || offset < 0)
	    throw new ArrayIndexOutOfBoundsException ();

	for (i = 0; i < len; i++) {
	    if (start >= finish) {
		start = 0;
		finish = instream.read (buffer, 0, buffer.length);
		if (finish <= 0) {
		    if (finish <= 0)
			this.close ();
		    break;
		}
	    }
	    c = buffer [start++];
	    if ((c & 0x80) != 0)
		throw new CharConversionException (
						   "Illegal ASCII character, 0x"
						   + Integer.toHexString (c & 0xff)
						   );
	    buf [offset + i] = (char) c;
	}
	if (i == 0 && finish <= 0)
	    return -1;
	return i;
    }
}

final class Iso8859_1Reader extends BaseReader
{
    Iso8859_1Reader (InputStream in) { super (in); }
    
    public int read (char buf [], int offset, int len) throws IOException
    {
	int		i;
	
	if (instream == null)
	    return -1;
	
	// avoid many runtime bounds checks ... a good optimizer
	// (static or JIT) will now remove checks from the loop.
	if ((offset + len) > buf.length || offset < 0)
	    throw new ArrayIndexOutOfBoundsException ();
	    
	for (i = 0; i < len; i++) {
	    if (start >= finish) {
		start = 0;
		finish = instream.read (buffer, 0, buffer.length);
		if (finish <= 0) {
		    if (finish <= 0)
			this.close ();
		    break;
		}
	    }
	    buf [offset + i] = (char) (0x0ff & buffer [start++]);
	}
	if (i == 0 && finish <= 0)
	    return -1;
	return i;
    }
}
 
