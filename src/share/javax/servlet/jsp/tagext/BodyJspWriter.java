/*
 * @(#)BodyJspWriter.java	1.7 99/08/08
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
import java.io.PrintWriter;

import javax.servlet.jsp.JspWriter;

/**
 * A JspWriter subclass that can be used to process body evaluations
 * so they can re-extracted later on.
 */

public abstract class BodyJspWriter extends JspWriter {
    
    /**
     * Construct a BodyJspWriter.
     *
     * Only to be used by a subclass.
     * TODO -- buffering issues to be revisited after Monday.
     */

    protected BodyJspWriter(int buffersize, boolean autoflush) {
	super(buffersize, autoflush);
    }

    /**
     * Clear the body.
     * TODO -- need to clarify this
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
     * Return the value of this BodyJspWriter as a Reader.
     * Note: this is after evaluation!!  There are no scriptlets,
     * etc in this stream.
     *
     * @returns the value of this BodyJspWriter as a Reader
     */
    public abstract Reader getReader();

    /**
     * Return the value of the BodyJspWriter as a String.
     * Note: this is after evaluation!!  There are no scriptlets,
     * etc in this stream.
     *
     * @returns the value of the BodyJspWriter as a String
     */
    public abstract String getString();
	
    /**
     * Write the contents of this BodyJspWriter into a Writer.
     * Subclasses are likely to do interesting things with the
     * implementation so some things are extra efficient.
     *
     * @param out The writer into which to place the contents of
     * this body evaluation
     */
    public abstract void writeOut(Writer out);
}
