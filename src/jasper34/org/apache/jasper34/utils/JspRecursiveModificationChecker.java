/* $Id: JspRecursiveModificationChecker.java,v 1.1 2001/04/18 10:39:38 melaquias Exp $
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

package org.apache.jasper34.utils;

/**
    implementation of Modification Checker that will recurse through all
    static includes in a Java Server Pages <i>source</i> document (up to a depth
    of INCLUDE_RECURSION_MAX), comparing
    file modification dates until it finds one newer than the <i>target</i>
    file.  Otherwise, returns false.
*/
public class JspRecursiveModificationChecker extends SimpleModificationChecker {
    /**
        name of property to set to control the depth of recursion
	    explored when the JSP Compiler checks modification dates
	    on Jsp files with static includes of other jsp files.
	    <pre><code>
	    * org.apache.jasper.jsp_include_recursion_max = 10
	    </code></pre>
	    The default value is 5.
	*/
    public static final String INCLUDE_RECURSION_MAX_PROPERTY =
    "org.apache.jasper.jsp_include_recursion_max";

    /** recursion depth for mofification check */
    public static final int INCLUDE_RECURSION_MAX;

    static {
        String s = null;
        s = JasperToolkit.getProperty(INCLUDE_RECURSION_MAX_PROPERTY);
        int sz = 5;
        try {
            sz = Integer.parseInt(s);
        } catch(NumberFormatException nfe) {
            sz = 5;
        }
        INCLUDE_RECURSION_MAX = sz;
    }

    public boolean isModified(){
        return super.isModified();
    }
 }





