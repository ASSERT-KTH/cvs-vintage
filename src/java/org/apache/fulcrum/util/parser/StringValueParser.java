package org.apache.fulcrum.util.parser;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
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
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Turbine" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Turbine", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
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
 */

import java.util.StringTokenizer;
import java.net.URLDecoder;

/**
 * An extension that parses a String for name/value pairs.
 *
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @version $Id: StringValueParser.java,v 1.1 2004/10/24 22:12:30 dep4b Exp $
 */
public class StringValueParser
    extends BaseValueParser
{
    public StringValueParser() {}

    /**
     * Parses a String using a single delimiter.
     *
     * @param s a <code>String</code> value
     * @param delim a <code>char</code> value
     * @param urlDecode a <code>boolean</code> value
     * @exception Exception Error decoding name/value pairs.
     */
    public void parse(String s, char delim, boolean urlDecode) 
        throws Exception
    {
        String delimChar = String.valueOf(delim);
        StringTokenizer st = new StringTokenizer(s, delimChar);
        boolean isNameTok = true;
        String pathPart = null;
        String key = null;
        while (st.hasMoreTokens())
        {
            String tok = st.nextToken();
            if ( urlDecode ) 
            {
                tok = URLDecoder.decode(tok);
            }
            
            if (isNameTok)
            {
                key = tok;
                isNameTok = false;
            }
            else
            {
                pathPart = tok;
                if (key.length() > 0)
                {
                    add (convert(key), pathPart);
                }
                isNameTok = true;
            }
        }
    }

    public void parse(String s, char paramDelim, char pairDelim, 
                      boolean urlDecode)
        throws Exception
    {
        if ( paramDelim == pairDelim ) 
        {
            parse(s, paramDelim, urlDecode);
        }
        else 
        {
            String delimChar = String.valueOf(paramDelim);
            StringTokenizer st = new StringTokenizer(s, delimChar);
            boolean isNameTok = true;
            String pathPart = null;
            String key = null;
            while (st.hasMoreTokens())
            {
                String pair = st.nextToken();
                int pos = pair.indexOf(pairDelim);
                String name = pair.substring(0, pos);
                String value = pair.substring(pos+1);
                
                if ( urlDecode ) 
                {
                    name = URLDecoder.decode(name);
                    value = URLDecoder.decode(value);
                }
            
                if (name.length() > 0)
                {
                    add (convert(name), value);
                }
            }
        }
    }
}
