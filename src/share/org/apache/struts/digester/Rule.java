/*
 * $Header: /tmp/cvs-vintage/struts/src/share/org/apache/struts/digester/Attic/Rule.java,v 1.2 2001/04/14 02:11:02 craigmcc Exp $
 * $Revision: 1.2 $
 * $Date: 2001/04/14 02:11:02 $
 *
 * ====================================================================
 * 
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2001 The Apache Software Foundation.  All rights 
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
 * 4. The names "The Jakarta Project", "Struts", and "Apache Software
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


package org.apache.struts.digester;


import org.xml.sax.AttributeList;


/**
 * Concrete implementations of this class implement actions to be taken when
 * a corresponding nested pattern of XML elements has been matched.
 *
 * @author Craig McClanahan
 * @version $Revision: 1.2 $ $Date: 2001/04/14 02:11:02 $
 */

public abstract class Rule {


    // ----------------------------------------------------------- Constructors


    /**
     * Default constructor sets only the the associated Digester.
     *
     * @param digester The digester with which this rule is associated
     */
    public Rule(Digester digester) {

	super();
	this.digester = digester;

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * The Digester with which this Rule is associated.
     */
    protected Digester digester = null;


    // --------------------------------------------------------- Public Methods


    /**
     * This method is called when the beginning of a matching XML element
     * is encountered.
     *
     * @param attributes The attribute list of this element
     */
    public void begin(AttributeList attributes) throws Exception {

	;	// The default implementation does nothing

    }


    /**
     * This method is called when the body of a matching XML element
     * is encountered.  If the element has no body, this method is
     * not called at all.
     *
     * @param text The text of the body of this element
     */
    public void body(String text) throws Exception {

	;	// The default implementation does nothing

    }


    /**
     * This method is called when the end of a matching XML element
     * is encountered.
     */
    public void end() throws Exception {

	;	// The default implementation does nothing

    }



    /**
     * This method is called after all parsing methods have been
     * called, to allow Rules to remove temporary data.
     */
    public void finish() throws Exception {

	;	// The default implementation does nothing

    }


}
