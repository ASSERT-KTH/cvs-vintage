/*
 * @(#)BodyContent.java	1.13 99/10/14
 * 
 * Copyright (c) 1999 Sun Microsystems, Inc. All Rights Reserved.
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
 
package javax.servlet.jsp.tagext;

import java.io.Reader;
import java.io.Writer;
import java.io.IOException;
import javax.servlet.jsp.*;

/**
 * A JspWriter subclass that can be used to process body evaluations
 * so they can re-extracted later on.
 */

public abstract class BodyContent extends JspWriter {
    
    /**
     * Protected constructor.
     *
     * Unbounded buffer,  no autoflushing.
     */

    protected BodyContent(JspWriter e) {
	super(UNBOUNDED_BUFFER , false);
	this.enclosingWriter = e;
    }

    /**
     * Redefine flush().
     * It is not valid to flush.
     */

    public void flush() throws IOException {
	throw new IOException("Illegal to flush within a custom tag");
    }

    /**
     * Clear the body.
     */
    
    public void clearBody() {
	try {
	    this.clear();
	} catch (IOException ex) {
	    // TODO -- clean this one up.
	    throw new Error("internal error!;");
	}
    }

    /**
     * Return the value of this BodyContent as a Reader.
     * Note: this is after evaluation!!  There are no scriptlets,
     * etc in this stream.
     *
     * @returns the value of this BodyContent as a Reader
     */
    public abstract Reader getReader();

    /**
     * Return the value of the BodyContent as a String.
     * Note: this is after evaluation!!  There are no scriptlets,
     * etc in this stream.
     *
     * @returns the value of the BodyContent as a String
     */
    public abstract String getString();
	
    /**
     * Write the contents of this BodyContent into a Writer.
     * Subclasses are likely to do interesting things with the
     * implementation so some things are extra efficient.
     *
     * @param out The writer into which to place the contents of
     * this body evaluation
     */

    public abstract void writeOut(Writer out) throws IOException;

    /**
     * Get the enclosing JspWriter
     *
     * @returns the enclosing JspWriter passed at construction time
     */

    public JspWriter getEnclosingWriter() {
	return enclosingWriter;
    }

    /**
     * private fields
     */
    
    private JspWriter enclosingWriter;
 }
