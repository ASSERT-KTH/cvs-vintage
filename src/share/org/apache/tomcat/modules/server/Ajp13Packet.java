/*   
 *  Copyright 1999-2004 The Apache Sofware Foundation.
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

package org.apache.tomcat.modules.server;

import java.io.IOException;

import org.apache.tomcat.core.OutputBuffer;
import org.apache.tomcat.util.buf.MessageBytes;
import org.apache.tomcat.util.http.MimeHeaders;

/* Frozen, bug fixes only: all active development goes in
     jakarta-tomcat-connectors/jk/org/apache/ajp/Ajp14*
*/

/**
 * A single packet for communication between the web server and the
 * container.  Designed to be reused many times with no creation of
 * garbage.  Understands the format of data types for these packets.
 * Can be used (somewhat confusingly) for both incoming and outgoing
 * packets.  
 *
 * @author Dan Milstein [danmil@shore.net]
 * @author Keith Wannamaker [Keith@Wannamaker.org]
 */
public class Ajp13Packet {
    byte buff[]; // Holds the bytes of the packet
    int pos;     // The current read or write position in the buffer
    OutputBuffer ob;
    
    int len; 
    // This actually means different things depending on whether the
    // packet is read or write.  For read, it's the length of the
    // payload (excluding the header).  For write, it's the length of
    // the packet as a whole (counting the header).  Oh, well.
    
    /**
     * Create a new packet with an internal buffer of given size.
     */
    public Ajp13Packet( int size ) {
	buff = new byte[size];
    }
    
    public Ajp13Packet( byte b[] ) {
	buff = b;
    }

    public Ajp13Packet( OutputBuffer ob ) {
	this.ob=ob;
	buff=ob.getBuffer();
    }
	
    public byte[] getBuff() {
	return buff;
    }
    
    public int getLen() {
	return len;
    }
    
    public int getByteOff() {
	return pos;
    }
    
    public void setByteOff(int c) {
	pos=c;
    }

    /** 
     * Parse the packet header for a packet sent from the web server to
     * the container.  Set the read position to immediately after
     * the header.
     *
     * @return The length of the packet payload, as encoded in the
     * header, or -1 if the packet doesn't have a valid header.  
     */
    public int checkIn() {
	pos = 0;
	int mark = getInt();
	
	if( mark != 0x1234 ) {
	    // XXX Logging
        System.err.println( "Ajp13Packet: invalid packet header : " + mark);
        // We only get 4 bytes, not necessary to dump this stuff with dump
	    // dump( "In: " );
        // Bug #2927
	    return -1;
	}

	len = getInt();

	return len;
    }

    /**
     * Prepare this packet for accumulating a message from the container to
     * the web server.  Set the write position to just after the header
     * (but leave the length unwritten, because it is as yet unknown).  
     */
    public void reset() {
	len = 4;
	pos = 4;
	buff[0] = (byte)'A';
	buff[1] = (byte)'B';
    }

        	
    /**
     * For a packet to be sent to the web server, finish the process of
     * accumulating data and write the length of the data payload into
     * the header.  
     */
    public void end() {
	len = pos;
	setInt( 2, len-4 );
    }
    
    // ============ Data Writing Methods ===================
    
    /**
     * Write an integer at an arbitrary position in the packet, but don't
     * change the write position.
     *
     * @param bpos The 0-indexed position within the buffer at which to
     * write the integer (where 0 is the beginning of the header).
     * @param val The integer to write.
     */
    private void setInt( int bPos, int val ) {
	buff[bPos]   = (byte) ((val >>>  8) & 0xFF);
	buff[bPos+1] = (byte) (val & 0xFF);
    }
    
    public void appendInt( int val ) {
	setInt( pos, val );
	pos += 2;
    }
    
    public void appendByte( byte val ) {
	buff[pos++] = val;
    }
    
    public void appendBool( boolean val) {
	buff[pos++] = (byte) (val ? 1 : 0);
    }
    
    /**
     * Write a String out at the current write position.  Strings are
     * encoded with the length in two bytes first, then the string, and
     * then a terminating \0 (which is <B>not</B> included in the
     * encoded length).  The terminator is for the convenience of the C
     * code, where it saves a round of copying.  A null string is
     * encoded as a string with length 0.  
     */
    public void appendString( String str ) {
	// Dual use of the buffer - as Ajp13Packet and as OutputBuffer
	// The idea is simple - fewer buffers, smaller footprint and less
	// memcpy. The code is a bit tricky, but only local to this
	// function.
	if(str == null) {
	    setInt( pos, 0);
	    buff[pos + 2] = 0;
	    pos += 3;
	    return;
	}
	
	int strStart=pos;
	
	// This replaces the old ( buggy and slow ) str.length()
	// and str.getBytes(). str.length() is chars, may be != bytes
	// and getBytes is _very_ slow.
	// XXX setEncoding !!!
	ob.setByteOff( pos+2 ); 
	try {
	    ob.write( str );
	    ob.flushChars();
	} catch( IOException ex ) {
	    ex.printStackTrace();
	}
	int strEnd=ob.getByteOff();
	
	buff[strEnd]=0; // The \0 terminator
	int strLen=strEnd-strStart;
	setInt( pos, strEnd - strStart );
	pos += strLen + 3; 
    }
    
    /** 
     * Copy a chunk of bytes into the packet, starting at the current
     * write position.  The chunk of bytes is encoded with the length
     * in two bytes first, then the data itself, and finally a
     * terminating \0 (which is <B>not</B> included in the encoded
     * length).
     *
     * @param b The array from which to copy bytes.
     * @param off The offset into the array at which to start copying
     * @param len The number of bytes to copy.  
     */
    public void appendBytes( byte b[], int off, int numBytes ) {
	appendInt( numBytes );
	if( pos + numBytes >= buff.length ) {
	    System.out.println("Buffer overflow " + buff.length + " " + pos + " " + numBytes );
	    // XXX Log
	}
	System.arraycopy( b, off, buff, pos, numBytes);
	buff[pos + numBytes] = 0; // Terminating \0
	pos += numBytes + 1;
    }
    
    
    // ============ Data Reading Methods ===================
    
    /**
     * Read an integer from packet, and advance the read position past
     * it.  Integers are encoded as two unsigned bytes with the
     * high-order byte first, and, as far as I can tell, in
     * little-endian order within each byte.  
     */
    public int getInt() {
	int result = peekInt();
	pos += 2;
	return result;
    }
    
    /**
     * Read an integer from the packet, but don't advance the read
     * position past it.  
     */
    public int peekInt() {
	int b1 = buff[pos] & 0xFF;  // No swap, Java order
	int b2 = buff[pos + 1] & 0xFF;
	
	return  (b1<<8) + b2;
    }
    
    public byte getByte() {
	byte res = buff[pos];
	pos++;
	return res;
    }
    
    public byte peekByte() {
	return buff[pos];
    }
    
    public boolean getBool() {
	return (getByte() == (byte) 1);
    }
    
    public static final String DEFAULT_CHAR_ENCODING = "ISO-8859-1";
    
    public void getMessageBytes( MessageBytes mb ) {
	int length = getInt();
	if( (length == 0xFFFF) || (length == -1) ) {
	    mb.setString( null );
	    return;
	}
	mb.setBytes( buff, pos, length );
	pos += length;
	pos++; // Skip the terminating \0
    }
    
    public MessageBytes addHeader( MimeHeaders headers ) {
	int length = getInt();
	if( (length == 0xFFFF) || (length == -1) ) {
	    return null;
	}
	MessageBytes vMB=headers.addValue( buff, pos, length );
	pos += length;
	pos++; // Skip the terminating \0
	
	return vMB;
    }
    
    /**
     * Read a String from the packet, and advance the read position
     * past it.  See appendString for details on string encoding.
     **/
    public String getString() throws java.io.UnsupportedEncodingException {
	int length = getInt();
	if( (length == 0xFFFF) || (length == -1) ) {
	    return null;
	}
	String s = new String( buff, pos, length, DEFAULT_CHAR_ENCODING );
	
	pos += length;
	pos++; // Skip the terminating \0
	return s;
    }
    
    /**
     * Copy a chunk of bytes from the packet into an array and advance
     * the read position past the chunk.  See appendBytes() for details
     * on the encoding.
     *
     * @return The number of bytes copied.
     */
    public int getBytes(byte dest[]) {
	int length = getInt();
	
	if( length > buff.length ) {
	    // XXX Should be if(pos + length > buff.legth)?
	    System.out.println("XXX Assert failed, buff too small ");
	}
	
	if( (length == 0xFFFF) || (length == -1) ) {
	    System.out.println("null string " + length);
	    return 0;
	}

	System.arraycopy( buff, pos,  dest, 0, length );
	pos += length; 
	pos++; // Skip terminating \0  XXX I believe this is wrong but harmless
	return length;
    }
    
    // ============== Debugging code =========================
    private String hex( int x ) {
	//	    if( x < 0) x=256 + x;
	String h=Integer.toHexString( x );
	if( h.length() == 1 ) h = "0" + h;
	return h.substring( h.length() - 2 );
    }
    
    private void hexLine( int start , StringBuffer sb) {
	int pkgEnd = len + 4;
	if( pkgEnd > buff.length )
	    pkgEnd = buff.length;
	for( int i=start; i< start+16 ; i++ ) {
	    if( i < pkgEnd)
		sb.append( hex( buff[i] ) + " ");
	    else 
		sb.append( "   " );
	}
	sb.append(" | ");
	for( int i=start; i < start+16 && i < pkgEnd; i++ ) {
	    char c=(char)buff[i];
	    if( ! Character.isISOControl(c) &&
		Character.isDefined(c) )
		sb.append( c );
	    else if( c==(char)0x20 )
		sb.append( c );
	    else
		sb.append( "." );
	}
	sb.append("\n");
    }
    
    public void dump(String msg) {
	StringBuffer sb=new StringBuffer();
	sb.append( this ).append("/").append(Thread.currentThread()).append("\n");
	sb.append( msg + ": " + buff + " " + pos +"/" + (len + 4) + "\n");
	
	for( int j=0; j < len + 4; j+=16 )
	    hexLine( j, sb );
	
	System.out.println(sb);
    }

    private static final int dL=0;
    private void d(String s ) {
	System.err.println( "Ajp13Packet: " + s );
    }

}
