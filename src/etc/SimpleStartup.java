/*
 * $Header: /tmp/cvs-vintage/tomcat/src/etc/Attic/SimpleStartup.java,v 1.2 1999/10/12 05:42:09 craigmcc Exp $
 * $Revision: 1.2 $
 * $Date: 1999/10/12 05:42:09 $
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
 * [Additional notices, if required by prior licensing conditions]
 *
 */ 


import org.apache.tomcat.core.*;
import org.apache.tomcat.server.*;
import java.net.*;

/**
 * This is a very simple example of how to start up the HttpServer
 * from any Java based program. Note that this example uses APIs
 * which are not yet finalized and subject to change. Use such
 * information at your own risk.
 */

public class SimpleStartup {

    /**
     * The application main program.
     *
     * @param args The command line arguments submitted to this program
     */
    public static void main(String[] args) {
	int port = 8080;
	InetAddress inet = null; // null uses all inets on the machine
	String hostname = null;
	HttpServer server = new HttpServer(port, inet, hostname);
	try {
	    URL url = resolveURL("webpages");
	    server.setDocumentBase(url);
	    System.out.println("Starting with docbase of: " + url);
	    server.start();
	} catch (MalformedURLException mue) {
	    System.out.println("Malformed URL Exception for doc root");
	    System.out.println(mue.getMessage());
	} catch (HttpServerException hse) {
	    System.out.println("Server threw an exception while running");
	    System.out.println(hse.getMessage());
	}

	// when you want to stop the server, simply call
	// server.stop();
    }
    

    /**
     * Convert the specified String into a URL, according to the following
     * rules:
     * <ul>
     * <li>If the string contains the magic characters <code>:/</code>,
     *     assume that it is an actual URL and do nothing.
     * <li>If the string starts with a <code>/</code> character, assume that
     *     it is an absolute pathname on the local filesystem.
     * <li>Otherwise, assume that the string represents a directory
     *     or file in the current working directory.
     * </ul>
     *
     * @param s The string to be converted
     */
    private static URL resolveURL(String s) throws MalformedURLException {
	// if the string contains the magic :/, then we assume
	// that it's a real URL and do nothing
	
	if (s.indexOf(":/") > -1) {
	    return new URL(s);
	}
	    
	// otherwise, we assume that we've got a file name and
	// need to construct a file url appropriatly.
	
	if (s.startsWith("/")) {
	    return new URL("file", null, s);
	} else {
	    String pwd = System.getProperty("user.dir");
	    return new URL("file", null, pwd + "/" + s);
	}
    }


}








