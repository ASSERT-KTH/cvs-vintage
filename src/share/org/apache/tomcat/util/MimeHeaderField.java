/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/tomcat/util/Attic/MimeHeaderField.java,v 1.1 1999/10/09 00:20:56 duncan Exp $
 * $Revision: 1.1 $
 * $Date: 1999/10/09 00:20:56 $
 *
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


package org.apache.tomcat.util;

import javax.servlet.*;
import java.io.IOException;
import java.io.OutputStream;

/**
 * This class is used to represent a MIME header field.
 *
 * @author dac@eng.sun.com
 * @author James Todd [gonzo@eng.sun.com]
 */

public class MimeHeaderField {

    private StringManager sm =
        StringManager.getManager(Constants.Package);

    /**
     * The header field name.
     */

    protected final MessageString name = new MessageString();

    /**
     * The header field value.
     */

    protected final MessageString value = new MessageString();

    /**
     * The header field integer value.
     */

    protected int intValue;

    /**
     * The header field Date value.
     */

    protected final HttpDate dateValue = new HttpDate(0);

    /**
     * The header field value type.
     */

    protected int type = T_NULL;

    protected static final int T_NULL = 0;
    protected static final int T_STR  = 1;
    protected static final int T_INT  = 2;
    protected static final int T_DATE = 3;

    private static final byte[] charval = { 
	(byte)'0', (byte)'1', (byte)'2', (byte)'3', (byte)'4',
	(byte)'5', (byte)'6', (byte)'7', (byte)'8', (byte)'9' 
    };

    /**
     * Creates a new, uninitialized header field.
     */

    public MimeHeaderField() {
    }

    /**
     * Resets the header field to an uninitialized state.
     */

    public void reset() {
	name.reset();
	value.reset();
	type = T_NULL;
    }

    /**
     * Sets the header field name to the specified string.
     * @param s the header field name String
     */

    public void setName(String s) {
	name.setString(s);
    }

    /**
     * Sets the header field name to the specified subarray of bytes.
     * @param b the header field name bytes
     * @param off the start offset of the bytes
     * @param len the length of the bytes
     */

    public void setName(byte[] b, int off, int len) {
	name.setBytes(b, off, len);
    }

    /**
     * Sets the header field value to the specified string.
     * @param s the header field value String
     */

    public void setValue(String s) {
	value.setString(s);
	type = T_STR;
    }

    /**
     * Sets the header field value to the specified subarray of bytes.
     * @param b the header field value bytes
     * @param off the start offset of the bytes
     * @param len the length of the bytes
     */

    public void setValue(byte[] b, int off, int len) {
	value.setBytes(b, off, len);
	type = T_STR;
    }

    /**
     * Sets the header field to the specified integer value.
     * @param i the header field integer value
     */

    public void setIntValue(int i) {
	intValue = i;
	type = T_INT;
    }

    /**
     * Sets the header field date value to the specified time.
     * @param t the time in milliseconds since the epoch
     */

    public void setDateValue(long t) {
	dateValue.setTime(t);
	type = T_DATE;
    }

    /**
     * Sets the header field date value to the current time.
     */

    public void setDateValue() {
	dateValue.setTime();
	type = T_DATE;
    }

    /**
     * Returns the header field name as a String.
     */

    public String getName() {
	return name.toString();
    }

    /**
     * Returns the header field value as a String, or null if not set.
     */

    public String getValue() {
	switch (type) {
	case T_STR:
	    return value.toString();
	case T_INT:
	    return String.valueOf(intValue);
	case T_DATE:
	    return dateValue.toString();
	default:
	    return null;
	}
    }

    /**
     * Returns the integer value of the header field.
     * @exception NumberFormatException if the integer format was invalid
     */

    public int getIntValue()
    throws NumberFormatException {
	switch (type) {
	case T_INT:
	    return intValue;
	case T_STR:
	    return value.toInteger();
	default:
            String msg = sm.getString("mimeHeaderField.int.nfe");

	    throw new NumberFormatException(msg);
	}
    }

    /**
     * Returns the date value of the header field.
     * @return the header date value in number of milliseconds since the epoch
     * @exception IllegalArgumentException if the date format was invalid
     */

    public long getDateValue()
    throws IllegalArgumentException {
	switch (type) {
	case T_DATE:
	    return dateValue.getTime();
	case T_STR:
	    return value.toDate(dateValue);
	default:
            String msg = sm.getString("mimeHeaderField.date.iae");

	    throw new IllegalArgumentException(msg);
	}
    }

    /** 
     * Place a byte representation of target into the byte array buf.
     * @param target - the integer to convert.  Must be in the range [0..2^31].
     * @return the number of bytes added to buf
     */

    private int intGetBytes(int target, byte buf[], int offset) {
	int power = 1000000000;  // magnitude of highest digit we can handle
	int this_digit;
	int bytes_emitted = 0;

	// special case target is zero
	if (target == 0) {
	    buf[offset] = charval[0];

	    return (1);
	}

	// iterate until there are no more digits to emit
	while (power > 0) {
	    this_digit = target / power;

	    if (this_digit != 0 || bytes_emitted > 0) {
		// emit this digit
		buf[offset + bytes_emitted++] = charval[this_digit];
	    }

	    target = target % power;
	    power = power / 10;
	}

	return bytes_emitted;
    }

    /**
     * Put the bytes for this header into buf starting at offset buf_offset.
     * @return the length of what was added
     */

    public int getBytes(byte buf[], int buf_offset) {
	int len;
	int start_pt = buf_offset;

	buf_offset += name.getBytes(buf, buf_offset);
	buf[buf_offset++] = (byte) ':';
	buf[buf_offset++] = (byte) ' ';

	switch (type) {
	case T_STR:
	    buf_offset += value.getBytes(buf, buf_offset);
	    break;
	case T_INT:
	    buf_offset += intGetBytes(intValue, buf, buf_offset);
	    break;
	case T_DATE:
	    buf_offset += dateValue.getBytes(buf, buf_offset,
	        HttpDate.DATELEN);
	    break;
	}

	buf[buf_offset++] = (byte) '\r';
	buf[buf_offset++] = (byte) '\n';

	return buf_offset - start_pt;
    }

    /**
     * Parses a header field from a subarray of bytes.
     * @param b the bytes to parse
     * @param off the start offset of the bytes
     * @param len the length of the bytes
     * @exception IllegalArgumentException if the header format was invalid
     */

    public void parse(byte[] b, int off, int len)
    throws IllegalArgumentException {
	int start = off;
	byte c;

	while ((c = b[off++]) != ':' && c != ' ') {
	    if (c == '\n') {
                String msg = sm.getString("mimeHeaderField.header.iae");

		throw new IllegalArgumentException(msg);
	    }
	}

	setName(b, start, off - start - 1);

	while (c == ' ') {
	    c = b[off++];
	}

	if (c != ':') {
            String msg = sm.getString("mimeHeaderField.header.iae");

	    throw new IllegalArgumentException(msg);
	}

	while ((c = b[off++]) == ' ');

	setValue(b, off - 1, len - (off - start - 1));
    }

    /**
     * Writes this header field to the specified servlet output stream.
     */

    public void write(ServletOutputStream out)
    throws IOException {
	name.write(out);
	out.print(": ");

	switch (type) {
	case T_STR:
	    value.write(out);
	    out.println();
	    break;
	case T_INT:
	    out.println(intValue);
	    break;
	case T_DATE:
	    dateValue.write(out);
	    out.println();
	    break;
	default:
	    out.println();
	}
    }

    /**
     * Returns true if the header field has the specified name. Character
     * case is ignored in the comparison.
     * @param s the string to compare
     */

    public boolean nameEquals(String s) {
	return name.equalsIgnoreCase(s);
    }

    /**
     * Returns true if the header field has the specified name. Character
     * case is ignored in the comparison.
     * @param b the bytes to compare
     * @param off the start offset of the bytes
     * @param len the length of the bytes
     */

    public boolean nameEquals(byte[] b, int off, int len) {
	return name.equalsIgnoreCase(b, off, len);
    }

    /**
     * Returns a string representation of the header field.
     */

    public String toString() {
	StringBuffer sb = new StringBuffer();

	sb.append(name.toString());
	sb.append(": ");

	switch (type) {
	case T_STR:
	    sb.append(value.toString());
	    break;
	case T_INT:
	    sb.append(intValue);
	    break;
	case T_DATE:
	    sb.append(dateValue.toString());
	    break;
	}
	return sb.toString();
    }
}
