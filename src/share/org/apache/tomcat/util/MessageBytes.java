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

package org.apache.tomcat.util;

import java.text.*;
import java.util.*;

/**
 * This class is used to represent a subarray of bytes in an HTTP message.
 *
 * @author dac@eng.sun.com
 * @author James Todd [gonzo@eng.sun.com]
 * @author Costin Manolache
 */
public final class MessageBytes implements Cloneable {
    public static final String DEFAULT_CHAR_ENCODING="8859_1";
    
    // primary type ( whatever is set as original value )
    private int type = T_NULL;
    
    public static final int T_NULL = 0;
    public static final int T_STR  = 1;
    public static final int T_BYTES = 2;
    public static final int T_CHARS = 3;

    // support for efficient int and date parsing/formating
    public static final int T_INT = 4;
    public static final int T_DATE = 5;

    private int hashCode=0;
    private boolean hasHashCode=false;

    private boolean caseSensitive=true;
    
    // byte[]
    private byte[] bytes;
    private int bytesOff;
    private int bytesLen;
    private String enc;
    private boolean hasByteValue=false;
    
    // Caching the result of a conversion

    // char[]
    private char chars[];
    private int charsOff;
    private int charsLen;
    private boolean hasCharValue=false;
    
    // String
    private String strValue;
    private boolean hasStrValue=false;

    // efficient int and date
    private int intValue;
    private boolean hasIntValue=false;
    private Date dateValue;
    private boolean hasDateValue=false;
    
    /**
     * Creates a new, uninitialized MessageBytes object.
     */
    public MessageBytes() {
    }

    public void setCaseSenitive( boolean b ) {
	caseSensitive=b;
    }

    public MessageBytes getClone() {
	try {
	    return (MessageBytes)this.clone();
	} catch( Exception ex) {
	    return null;
	}
    }

    public void reset() {
	recycle();
    }
    /**
     * Resets the message bytes to an uninitialized state.
     */
    public void recycle() {
	bytes = null;
	strValue=null;
	//	chars=null;
	caseSensitive=true;

	enc=null;
	hasByteValue=false;
	hasStrValue=false;
	hasCharValue=false;
	hasHashCode=false;
	hasIntValue=false;
	hasDateValue=false;	
    }


    /**
     * Sets the message bytes to the specified subarray of bytes.
     * @param b the ascii bytes
     * @param off the start offset of the bytes
     * @param len the length of the bytes
     */
    public void setBytes(byte[] b, int off, int len) {
	bytes = b;
	bytesOff = off;
	bytesLen = len;
	type=T_BYTES;
	hasByteValue=true;
    }

    public void setEncoding( String enc ) {
	this.enc=enc;
    }
    
    public void setChars( char[] c, int off, int len ) {
	chars=c;
	charsOff=off;
	charsLen=len;
	type=T_CHARS;
	hasCharValue=true;
    }

    public void setString( String s ) {
	strValue=s;
	hasStrValue=true;
	type=T_STR;
    }

    public void setTime(long t) {
	if( dateValue==null)
	    dateValue=new Date(t);
	else
	    dateValue.setTime(t);
	type = T_DATE;
	hasDateValue=true;
    }

    public void setInt(int i) {
	intValue = i;
	type = T_INT;
	hasIntValue=true;
    }

    // -------------------- Conversion and getters --------------------
    public String toString() {
	if( hasStrValue ) return strValue;
	hasStrValue=true;

	switch (type) {
	case T_CHARS:
	    strValue=new String( chars, charsOff, charsLen);
	    return strValue;
	case T_BYTES:
	    try {
		if( enc==null )
		    strValue=toStringUTF8();
		else
		    strValue=new String(bytes, bytesOff, bytesLen, enc);
		return strValue;
	    } catch (java.io.UnsupportedEncodingException e) {
		return null;  // can't happen
	    }
	case T_DATE:
	    strValue=DateTool.rfc1123Format.format(dateValue);
	    return strValue;
	case T_INT:
	    strValue=String.valueOf(intValue);
	    return strValue;
	}
	return null;
    }

    private String toStringUTF8() {
        if (null == bytes) {
            return null;
        }
	if( chars==null || bytesLen > chars.length ) {
	    chars=new char[bytesLen];
	}

	int j=bytesOff;
	for( int i=0; i< bytesLen; i++ ) {
	    chars[i]=(char)bytes[j++];
	}
	charsLen=bytesLen;
	charsOff=0;
	hasCharValue=true;
	return new String( chars, 0, bytesLen);
    }

    public long getTime()
    {
	if( hasDateValue ) {
	    if( dateValue==null) return -1;
	    return dateValue.getTime();
	}

	long l=DateTool.parseDate( this );
	if( dateValue==null)
	    dateValue=new Date(l);
	else
	    dateValue.setTime(l);
	hasDateValue=true;
	return l;
    }

    public int getInt()
    {
	if( hasIntValue )
	    return intValue;
	
	switch (type) {
	case T_BYTES:
	    intValue=Ascii.parseInt(bytes, bytesOff,
				    bytesLen);
	    break;
	default:
	    intValue=Integer.parseInt(toString());
	}
	hasIntValue=true;
	return intValue;
    }
    
    //----------------------------------------
    public int getType() {
	return type;
    }
    
    /**
     * Returns the message bytes.
     */
    public byte[] getBytes() {
	return bytes;
    }

    /**
     * Returns the start offset of the bytes.
     */
    public int getOffset() {
	if(type==T_BYTES)
	    return bytesOff;
	if(type==T_CHARS)
	    return charsOff;
	return 0;
    }

    /**
     * Returns the length of the bytes.
     */
    public int getLength() {
	if(type==T_BYTES)
	    return bytesLen;
	if(type==T_CHARS)
	    return charsLen;
	if(type==T_STR)
	    return strValue.length();
	toString();
	return strValue.length();
    }

    // -------------------- equals --------------------

    /**
     * Compares the message bytes to the specified String object.
     * @param s the String to compare
     * @return true if the comparison succeeded, false otherwise
     */
    public boolean equals(String s) {
	if( ! caseSensitive )
	    return equalsIgnoreCase( s );
	switch (type) {
	case T_INT:
	case T_DATE:
	    toString();
	    // now strValue is valid
	case T_STR:
	    if( strValue==null && s!=null) return false;
	    return strValue.equals( s );
	case T_CHARS:
	    char[] c = chars;
	    int len = charsLen;
	    if (c == null || len != s.length()) {
		return false;
	    }
	    int off = charsOff;
	    for (int i = 0; i < len; i++) {
		if (c[off++] != s.charAt(i)) {
		    return false;
		}
	    }
	    return true;
	case T_BYTES:
	    byte[] b = bytes;
	    int blen = bytesLen;
	    if (b == null || blen != s.length()) {
		return false;
	    }
	    int boff = bytesOff;
	    for (int i = 0; i < blen; i++) {
		if (b[boff++] != s.charAt(i)) {
		    return false;
		}
	    }
	    return true;
	default:
	    return false;
	}
    }

    /**
     * Compares the message bytes to the specified String object.
     * @param s the String to compare
     * @return true if the comparison succeeded, false otherwise
     */
    public boolean equalsIgnoreCase(String s) {
	switch (type) {
	case T_INT:
	case T_DATE:
	    toString(); // now strValue is set
	case T_STR:
	    if( strValue==null && s!=null) return false;
	    return strValue.equalsIgnoreCase( s );
	case T_CHARS:
	    char[] c = chars;
	    int len = charsLen;
	    if (c == null || len != s.length()) {
		return false;
	    }
	    int off = charsOff;
	    for (int i = 0; i < len; i++) {
		if (Ascii.toLower( c[off++] ) != Ascii.toLower( s.charAt(i))) {
		    return false;
		}
	    }
	    return true;
	case T_BYTES:
	    byte[] b = bytes;
	    int blen = bytesLen;
	    if (b == null || blen != s.length()) {
		return false;
	    }
	    int boff = bytesOff;
	    for (int i = 0; i < blen; i++) {
		if (Ascii.toLower(b[boff++]) != Ascii.toLower(s.charAt(i))) {
		    return false;
		}
	    }
	    return true;
	default:
	    return false;
	}
    }

    public boolean equals(MessageBytes mb) {
	switch (type) {
	case T_INT:
	case T_DATE:
	    toString(); // now strValue is set
	case T_STR:
	    return mb.equals( strValue );
	}

	if( mb.type != T_CHARS &&
	    mb.type!= T_BYTES ) {
	    // it's a string or int/date string value
	    return equals( mb.toString() );
	}

	// mb is either CHARS or BYTES.
	// this is either CHARS or BYTES
	// Deal with the 4 cases ( in fact 3, one is simetric)
	
	if( mb.type == T_CHARS && type==T_CHARS ) {
	    char b1[]=chars;
	    char b2[]=mb.chars;
	    if (b1== null || b2==null || mb.charsLen != charsLen) {
		return false;
	    }
	    int off1 = charsOff;
	    int off2 = mb.charsOff;
	    int len=charsLen;
	    while ( len-- > 0) {
		if (b1[off1++] != b2[off2++]) {
		    return false;
		}
	    }
	    return true;
	}
	if( mb.type==T_BYTES && type== T_BYTES ) {
	    byte b1[]=bytes;
	    byte b2[]=mb.bytes;
	    if (b1== null || b2==null || mb.bytesLen != bytesLen) {
		return false;
	    }
	    int off1 = bytesOff;
	    int off2 = mb.bytesOff;
	    int len=bytesLen;
	    while ( len-- > 0) {
		if (b1[off1++] != b2[off2++]) {
		    return false;
		}
	    }
	    return true;
	}

	// char/byte or byte/char
	MessageBytes mbB=this;
	MessageBytes mbC=mb;
	
	if( type == T_CHARS && mb.type==T_BYTES  ) {
	    mbB=mb;
	    mbC=this;
	}

	byte b1[]=mbB.bytes;
	char b2[]=mbC.chars;
	if (b1== null || b2==null || mbB.bytesLen != mbC.charsLen) {
	    return false;
	}
	int off1 = mbB.bytesOff;
	int off2 = mbC.charsOff;
	int len=mbB.bytesLen;
	
	while ( len-- > 0) {
	    if ( (char)b1[off1++] != b2[off2++]) {
		return false;
	    }
	}
	return true;
    }

    
    /**
     * Returns true if the message bytes starts with the specified string.
     * @param s the string
     */
    public boolean startsWith(String s) {
	switch (type) {
	case T_STR:
	    return strValue.startsWith( s );
	case T_CHARS:
	    char[] c = chars;
	    int len = s.length();
	    if (c == null || len > charsLen) {
		return false;
	    }
	    int off = charsOff;
	    for (int i = 0; i < len; i++) {
		if (c[off++] != s.charAt(i)) {
		    return false;
		}
	    }
	    return true;
	case T_BYTES:
	    byte[] b = bytes;
	    int blen = s.length();
	    if (b == null || blen > bytesLen) {
		return false;
	    }
	    int boff = bytesOff;
	    for (int i = 0; i < blen; i++) {
		if (b[boff++] != s.charAt(i)) {
		    return false;
		}
	    }
	    return true;
	case T_INT:
	case T_DATE:
	    String s1=toString();
	    if( s1==null && s!=null) return false;
	    return s1.startsWith( s );
	default:
	    return false;
	}
    }

    

    // -------------------- Hash code  --------------------
    public  int hashCode() {
	if( hasHashCode ) return hashCode;
	int code = 0;

	if( caseSensitive ) 
	    code=hash(); 
	else
	    code=hashIgnoreCase();
	hashCode=code;
	hasHashCode=true;
	return code;
    }

    // normal hash. 
    private int hash() {
	int code=0;
	switch (type) {
	case T_INT:
	case T_DATE:
	    String s1=toString();
	    // continue with T_STR - it now have a strValue
	case T_STR:
	    for (int i = 0; i < strValue.length(); i++) {
		code = code * 37 + strValue.charAt( i );
	    }
	    return code;
	case T_CHARS:
	    for (int i = charsOff; i < charsOff + charsLen; i++) {
		code = code * 37 + chars[i];
	    }
	    return code;
	case T_BYTES:
	    return hashBytes( bytes, bytesOff, bytesLen);
	default:
	    return 0;
	}
    }

    // hash ignoring case
    private int hashIgnoreCase() {
	int code=0;
	switch (type) {
	case T_INT:
	case T_DATE:
	    String s1=toString();
	    // continue with T_STR - it now have a strValue
	case T_STR:
	    for (int i = 0; i < strValue.length(); i++) {
		code = code * 37 + Ascii.toLower(strValue.charAt( i ));
	    }
	    return code;
	case T_CHARS:
	    for (int i = charsOff; i < charsOff + charsLen; i++) {
		code = code * 37 + Ascii.toLower(chars[i]);
	    }
	    return code;
	case T_BYTES:
	    return hashBytesIC( bytes, bytesOff, bytesLen );
	default:
	    return 0;
	}
    }

    private static int hashBytes( byte bytes[], int bytesOff, int bytesLen ) {
	int max=bytesOff+bytesLen;
	byte bb[]=bytes;
	int code=0;
	for (int i = bytesOff; i < max ; i++) {
	    code = code * 37 + bb[i];
	}
	return code;
    }

    private static int hashBytesIC( byte bytes[], int bytesOff,
				    int bytesLen )
    {
	int max=bytesOff+bytesLen;
	byte bb[]=bytes;
	int code=0;
	for (int i = bytesOff; i < max ; i++) {
	    code = code * 37 + Ascii.toLower(bb[i]);
	}
	return code;
    }

    /**
     * Returns true if the message bytes starts with the specified string.
     * @param s the string
     */
    public int indexOf(char c) {
	switch (type) {
	case T_INT:
	case T_DATE:
	    String s1=toString();
	    // continue with T_STR - it now have a strValue
	case T_STR:
	    return strValue.indexOf( c );
	case T_CHARS:
	    for (int i = charsOff; i < charsOff + charsLen; i++) {
		if( c == chars[i] ) return i;
	    }
	    return -1;
	case T_BYTES:
	    int max=bytesOff+bytesLen;
	    byte bb[]=bytes;
	    for (int i = bytesOff; i < max ; i++) {
		if( (byte)c == bb[i]) return i;
	    }
	    return -1;
	default:
	    return -1;
	}
    }


}
