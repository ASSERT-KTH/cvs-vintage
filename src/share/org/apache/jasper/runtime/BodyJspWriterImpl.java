/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/jasper/runtime/Attic/BodyJspWriterImpl.java,v 1.1 1999/10/09 00:20:39 duncan Exp $
 * $Revision: 1.1 $
 * $Date: 1999/10/09 00:20:39 $
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
 */ 

package org.apache.jasper.runtime;

import java.io.IOException;
import java.io.Writer;
import java.io.Reader;
import java.io.CharArrayReader;
import java.io.PrintWriter;

import javax.servlet.ServletResponse;
import javax.servlet.jsp.tagext.BodyJspWriter;

import org.apache.jasper.Constants;

/**
 * Write text to a character-output stream, buffering characters so as
 * to provide for the efficient writing of single characters, arrays,
 * and strings. 
 *
 * Provide support for discarding for the output that has been 
 * buffered. 
 *
 * TODO: this needs some serious revisiting!! -akv
 *
 * @author Harish Prabandham
 * @author Rajiv Mordani
 */
public class BodyJspWriterImpl extends BodyJspWriter {

    private DelegateWriter delegate = null;

    public BodyJspWriterImpl(int sz) {
        super(sz, false);
        if (sz <= 0)
            throw new IllegalArgumentException(Constants.getString (
	    			"jsp.buffer.size.zero"));
        delegate = new DelegateWriter(sz, false); 
    }

    /**
     * Write a line separator.  The line separator string is defined by the
     * system property <tt>line.separator</tt>, and is not necessarily a single
     * newline ('\n') character.
     *
     * @exception  IOException  If an I/O error occurs
     */

    public void newLine() throws IOException {
	delegate.newLine();
    }

    /**
     * Print a boolean value.  The string produced by <code>{@link
     * java.lang.String#valueOf(boolean)}</code> is translated into bytes
     * according to the platform's default character encoding, and these bytes
     * are written in exactly the manner of the <code>{@link
     * #write(int)}</code> method.
     *
     * @param      b   The <code>boolean</code> to be printed
     * @throws	   java.io.IOException
     */

    public void print(boolean b) throws IOException {
   	delegate.print (b); 
    }

    /**
     * Print a character.  The character is translated into one or more bytes
     * according to the platform's default character encoding, and these bytes
     * are written in exactly the manner of the <code>{@link
     * #write(int)}</code> method.
     *
     * @param      c   The <code>char</code> to be printed
     * @throws	   java.io.IOException
     */

    public void print(char c) throws IOException {
   	delegate.print (c); 
    }

    /**
     * Print an integer.  The string produced by <code>{@link
     * java.lang.String#valueOf(int)}</code> is translated into bytes according
     * to the platform's default character encoding, and these bytes are
     * written in exactly the manner of the <code>{@link #write(int)}</code>
     * method.
     *
     * @param      i   The <code>int</code> to be printed
     * @see        java.lang.Integer#toString(int)
     * @throws	   java.io.IOException
     */

    public void print(int i) throws IOException {
   	delegate.print (i); 
    }

    /**
     * Print a long integer.  The string produced by <code>{@link
     * java.lang.String#valueOf(long)}</code> is translated into bytes
     * according to the platform's default character encoding, and these bytes
     * are written in exactly the manner of the <code>{@link #write(int)}</code>
     * method.
     *
     * @param      l   The <code>long</code> to be printed
     * @see        java.lang.Long#toString(long)
     * @throws	   java.io.IOException
     */

    public void print(long l) throws IOException {
   	delegate.print (l); 
    }

    /**
     * Print a floating-point number.  The string produced by <code>{@link
     * java.lang.String#valueOf(float)}</code> is translated into bytes
     * according to the platform's default character encoding, and these bytes
     * are written in exactly the manner of the <code>{@link #write(int)}</code>
     * method.
     *
     * @param      f   The <code>float</code> to be printed
     * @see        java.lang.Float#toString(float)
     * @throws	   java.io.IOException
     */

    public void print(float f) throws IOException {
   	delegate.print (f); 
    }

    /**
     * Print a double-precision floating-point number.  The string produced by
     * <code>{@link java.lang.String#valueOf(double)}</code> is translated into
     * bytes according to the platform's default character encoding, and these
     * bytes are written in exactly the manner of the <code>{@link
     * #write(int)}</code> method.
     *
     * @param      d   The <code>double</code> to be printed
     * @see        java.lang.Double#toString(double)
     * @throws	   java.io.IOException
     */

    public void print(double d) throws IOException {
   	delegate.print (d); 
    }

    /**
     * Print an array of characters.  The characters are converted into bytes
     * according to the platform's default character encoding, and these bytes
     * are written in exactly the manner of the <code>{@link #write(int)}</code>
     * method.
     *
     * @param      s   The array of chars to be printed
     *
     * @throws  NullPointerException  If <code>s</code> is <code>null</code>
     * @throws	   java.io.IOException
     */

    public void print(char s[]) throws IOException {
   	delegate.print (s); 
    }

    /**
     * Print a string.  If the argument is <code>null</code> then the string
     * <code>"null"</code> is printed.  Otherwise, the string's characters are
     * converted into bytes according to the platform's default character
     * encoding, and these bytes are written in exactly the manner of the
     * <code>{@link #write(int)}</code> method.
     *
     * @param      s   The <code>String</code> to be printed
     * @throws	   java.io.IOException
     */

    public void print(String s) throws IOException {
   	delegate.print (s); 
    }

    /**
     * Print an object.  The string produced by the <code>{@link
     * java.lang.String#valueOf(Object)}</code> method is translated into bytes
     * according to the platform's default character encoding, and these bytes
     * are written in exactly the manner of the <code>{@link #write(int)}</code>
     * method.
     *
     * @param      obj   The <code>Object</code> to be printed
     * @see        java.lang.Object#toString()
     * @throws	   java.io.IOException
     */

    public void print(Object obj) throws IOException {
   	delegate.print (obj); 
    }

    /**
     * Terminate the current line by writing the line separator string.  The
     * line separator string is defined by the system property
     * <code>line.separator</code>, and is not necessarily a single newline
     * character (<code>'\n'</code>).
     * @throws	   java.io.IOException
     */

    public void println() throws IOException {
   	delegate.println(); 
    }

    /**
     * Print a boolean value and then terminate the line.  This method behaves
     * as though it invokes <code>{@link #print(boolean)}</code> and then
     * <code>{@link #println()}</code>.
     * @throws	   java.io.IOException
     */

    public void println(boolean x) throws IOException {
   	delegate.println (x); 
    }

    /**
     * Print a character and then terminate the line.  This method behaves as
     * though it invokes <code>{@link #print(char)}</code> and then <code>{@link
     * #println()}</code>.
     * @throws	   java.io.IOException
     */

    public void println(char x) throws IOException {
   	delegate.println (x); 
    }

    /**
     * Print an integer and then terminate the line.  This method behaves as
     * though it invokes <code>{@link #print(int)}</code> and then <code>{@link
     * #println()}</code>.
     * @throws	   java.io.IOException
     */

    public void println(int x) throws IOException {
   	delegate.println (x); 
    }

    /**
     * Print a long integer and then terminate the line.  This method behaves
     * as though it invokes <code>{@link #print(long)}</code> and then
     * <code>{@link #println()}</code>.
     * @throws	   java.io.IOException
     */

    public void println(long x) throws IOException {
   	delegate.println (x); 
    }

    /**
     * Print a floating-point number and then terminate the line.  This method
     * behaves as though it invokes <code>{@link #print(float)}</code> and then
     * <code>{@link #println()}</code>.
     * @throws	   java.io.IOException
     */

    public void println(float x) throws IOException {
   	delegate.println (x); 
    }

    /**
     * Print a double-precision floating-point number and then terminate the
     * line.  This method behaves as though it invokes <code>{@link
     * #print(double)}</code> and then <code>{@link #println()}</code>.
     * @throws	   java.io.IOException
     */

    public void println(double x) throws IOException{
   	delegate.println (x); 
    }

    /**
     * Print an array of characters and then terminate the line.  This method
     * behaves as though it invokes <code>{@link #print(char[])}</code> and then
     * <code>{@link #println()}</code>.
     * @throws	   java.io.IOException
     */

    public void println(char x[]) throws IOException {
   	delegate.println (x); 
    }

    /**
     * Print a String and then terminate the line.  This method behaves as
     * though it invokes <code>{@link #print(String)}</code> and then
     * <code>{@link #println()}</code>.
     * @throws	   java.io.IOException
     */

    public void println(String x) throws IOException {
   	delegate.println (x); 
    }

    /**
     * Print an Object and then terminate the line.  This method behaves as
     * though it invokes <code>{@link #print(Object)}</code> and then
     * <code>{@link #println()}</code>.
     * @throws	   java.io.IOException
     */

    public void println(Object x) throws IOException {
   	delegate.println (x); 
    }

    /**
     * Clear the contents of the buffer. If the buffer has been already
     * been flushed then the clear operation shall throw an IOException
     * to signal the fact that some data has already been irrevocably 
     * written to the client response stream.
     *
     * @throws IOException		If an I/O error occurs
     */

    public void clear() throws IOException {
   	delegate.clear(); 
    }

    /**
     * Clears the current contents of the buffer. Unlike clear(), this
     * mehtod will not throw an IOException if the buffer has already been
     * flushed. It merely clears the current content of the buffer and
     * returns.
     *
     * @throws IOException		If an I/O error occurs
     */

    public void clearBuffer() throws IOException {
   	delegate.clearBuffer(); 
    }

    /**
     * Flush the stream.  If the stream has saved any characters from the
     * various write() methods in a buffer, write them immediately to their
     * intended destination.  Then, if that destination is another character or
     * byte stream, flush it.  Thus one flush() invocation will flush all the
     * buffers in a chain of Writers and OutputStreams.
     *
     * @exception  IOException  If an I/O error occurs
     */

    public void flush()  throws IOException {
        delegate.flush ();
    }

    /**
     * Close the stream, flushing it first.  Once a stream has been closed,
     * further write() or flush() invocations will cause an IOException to be
     * thrown.  Closing a previously-closed stream, however, has no effect.
     *
     * @exception  IOException  If an I/O error occurs
     */

    public void close() throws IOException {
   	delegate.close (); 
    }

    /**
     * @return the number of bytes unused in the buffer
     */

    public int getRemaining() {
    	return delegate.getRemaining ();
    }

    /**
     * @return if this JspWriter is auto flushing or throwing IOExceptions on 
     * buffer overflow conditions
     */

    /**
     * Return the value of this BodyJspWriter as a Reader.
     * Note: this is after evaluation!!  There are no scriptlets,
     * etc in this stream.
     *
     * @returns the value of this BodyJspWriter as a Reader
     */
    public Reader getReader() {
  	return delegate.getReader(); 
    }

    /**
     * Return the value of the BodyJspWriter as a String.
     * Note: this is after evaluation!!  There are no scriptlets,
     * etc in this stream.
     *
     * @returns the value of the BodyJspWriter as a String
     */
    public String getString() {
   	return delegate.getString (); 
    }
	
    /**
     * Write the contents of this BodyJspWriter into a Writer.
     * Subclasses are likely to do interesting things with the
     * implementation so some things are extra efficient.
     *
     * @param out The writer into which to place the contents of
     * this body evaluation
     */
    public void writeOut(Writer out) {
   	delegate.writeOut (out); 
    }

    public void write(char cbuf[], int off, int len) 
              throws IOException
    {
   	delegate.write(cbuf, off, len); 
    }
    public void write(int c) throws IOException {
   	delegate.write (c); 
    }

    class DelegateWriter extends JspWriterImpl {

        public DelegateWriter(int sz, boolean autoflush) {
            super(null, sz, autoflush);
        }

        protected void initOut() {
        }

        protected void ensureOpen () {
        }
        /**
         * Flush the stream.
         *
         */
        public void flush()  throws IOException {
            synchronized (lock) {
                flushBuffer();
                if (out != null) {
                    if (!(out instanceof BodyJspWriter))
                        out.flush();
	        }
            }
        }

        public void clearBody() {
            try{
            super.clear();
            }catch(IOException ioe){}
        }

        public Reader getReader() {
	//XXX need to optimize this
	    char[] tmp = new char [ nextChar - 1];
	    for (int i=0; i < tmp.length; i++) 
	        tmp[i] = cb[i];	
	    return new CharArrayReader (tmp);
        }

        public String getString() {
	//XXX need to optimize this
	    char[] tmp = new char [ nextChar - 1];
	    for (int i=0; i < tmp.length; i++) 
	        tmp[i] = cb[i];	
	    return new String (tmp);
        }

        public void writeOut(Writer out) {
            super.out = out;
            try {
                this.flush();
            }catch(IOException ioe) {
	    }
        }
    }
    public static void main (String[] args) throws Exception {
	char[] buff = {'f','o','o','b','a','r','b','a','z','y'};
   	BodyJspWriterImpl bodyWriter = new BodyJspWriterImpl(100);
	bodyWriter.println (buff);
	System.out.println (bodyWriter.getString ());
	bodyWriter.writeOut (new PrintWriter (System.out));
    }
}
