/* $Id: CacheDefaults.java,v 1.1 2001/04/18 10:39:38 melaquias Exp $
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
    Simple class used to establish defaults for the Caches used
    by Jasper as established by settings either in a resource file
    or via System property (-D at the command line).
*/
public class CacheDefaults {
    /**
     *      name of property or init-param to set to tell Jasper how
     * 		large of a cache of compiled JSP servlets to maintain.
     * 		This number should roughly correlate with the total number
     * 		of distinct *.jsp URI's that the application will be servicing.
     * 		<pre><code>
	    * org.apache.jasper.jspservlet.cache_size = 64
	    * 		</code></pre>
     * 		The default value is 512.
     */
    public static final String JSP_CACHE_SIZE_PROPERTY =
    "com.g1440.naf.j2ee.jsp.cache_size";

    /** size of JSP servlet cache */
    public static int JSP_CACHE_SIZE;
    private static final int DEFAULT_JSP_CACHE_SIZE;

    /** set defaults, allowing overrides with -D startup properties */
    static {
        String s = null;
        s = JasperToolkit.getProperty(JSP_CACHE_SIZE_PROPERTY);
        int sz = -1;
        try {
            sz = Integer.parseInt(s);
        } catch(NumberFormatException nfe) {
        }
        if(sz > 0) {
            DEFAULT_JSP_CACHE_SIZE = sz;
        } else {
            DEFAULT_JSP_CACHE_SIZE = 512;
        }
        JSP_CACHE_SIZE = DEFAULT_JSP_CACHE_SIZE;
    }
}




