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
package org.apache.tomcat.util.test;

import java.net.*;
import java.io.*;
import java.util.*;
import java.net.*;


/**
 *  Part of GTest
 * 
 */
public class Properties {
    Hashtable keys=new Hashtable();
    
    public Properties() {}


    
    /** Replace ${NAME} with the property value
     *  Reproduced from ant, without dependencies on Project. Should be
     *  part of a top-level tool set.
     */
    public static String replaceProperties(String value, Hashtable keys )
    {
        StringBuffer sb=new StringBuffer();
        int i=0;
        int prev=0;
        if( value==null ) return null;
        int pos;
        while( (pos=value.indexOf( "$", prev )) >= 0 ) {
            if(pos>0) {
                sb.append( value.substring( prev, pos ) );
            }
            if( pos == (value.length() - 1)) {
                sb.append('$');
                prev = pos + 1;
            }
            else if (value.charAt( pos + 1 ) != '{' ) {
                sb.append( value.charAt( pos + 1 ) );
                prev=pos+2; 
            } else {
                int endName=value.indexOf( '}', pos );
                if( endName < 0 ) {
		    // it's not a property..
		    sb.append( value.substring( pos ));
		    pos=value.length() -1;
                }
                String n=value.substring( pos+2, endName );
                String v = (keys.containsKey(n)) ?
		    (String) keys.get(n) :
		    "${"+n+"}"; 
                
                sb.append( v );
                prev=endName+1;
            }
        }
        if( prev < value.length() ) sb.append( value.substring( prev ) );
        return sb.toString();
    }

}
