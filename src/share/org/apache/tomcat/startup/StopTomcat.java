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
package org.apache.tomcat.startup;

import org.apache.tomcat.util.StringManager;
import java.io.*;
import java.net.*;
import java.util.*;

// Depends: StringManager, resources


/**
 * This task will stop tomcat
 *
 * @author Costin Manolache
 */
public class StopTomcat { 
    static final String DEFAULT_CONFIG="conf/server.xml";
    private static StringManager sm =
	StringManager.getManager("org.apache.tomcat.resources");

    String tomcatHome;
    
    public StopTomcat() 
    {
    }

    // -------------------- Parameters --------------------

    public void setH( String s ) {
	tomcatHome=s;
	System.getProperties().put("tomcat.home", s);
    }

    public void setHome( String s ) {
	tomcatHome=s;
	System.getProperties().put("tomcat.home", s);
    }

    // -------------------- Ant execute --------------------

    public void execute() throws Exception {
	System.out.println(sm.getString("tomcat.stop"));
	try {
	    stopTomcat(); // stop serving
	}
	catch (java.net.ConnectException ex) {
	    System.out.println(sm.getString("tomcat.connectexception"));
	} catch (Exception te ) {
		throw te;
	}
	return;
    }

    // -------------------- Implementation --------------------
    
    void stopTomcat() throws Exception {
	String tchome=getTomcatInstall();
	int port=8007;
	InetAddress address=null;
	
	try {
	    BufferedReader rd=new BufferedReader
		( new FileReader( tchome + "/conf/ajp12.id"));
	    String portLine=rd.readLine();
	    
	    try {
		port=Integer.parseInt( portLine );
	    } catch(NumberFormatException ex ) {
		ex.printStackTrace();
	    }
	    String addLine=rd.readLine();
	    if( addLine!=null && !"".equals( addLine )) {
		try {
		    address=InetAddress.getByName( addLine );
		} catch( UnknownHostException ex ) {
		    ex.printStackTrace();
		}
	    }
	    String secret=rd.readLine();
	    if( "".equals( secret ) )
		secret=null;

	    System.out.println("Stoping tomcat on " + address + ":" +port +" "
			       + secret);
	    stopTomcat( address,port, secret );
	    
	} catch( IOException ex ) {
	    ex.printStackTrace();
	}

    }
    
    public String getTomcatInstall() {
	// Use the "tomcat.home" property to resolve the default filename
	String tchome = System.getProperty("tomcat.home");
	if (tchome == null) {
	    System.out.println(sm.getString("tomcat.nohome"));
	    tchome = ".";
	    // Assume current working directory
	}
	return tchome;
    }
    
    /**
     *  This particular implementation will search for an AJP12
     * 	connector ( that have a special stop command ).
     */
    public void stopTomcat(InetAddress address, int portInt, String secret )
	throws IOException 
    {
	// use Ajp12 to stop the server...
	try {
	    if (address == null)
		address = InetAddress.getLocalHost();
	    Socket socket = new Socket(address, portInt);
	    OutputStream os=socket.getOutputStream();
	    sendAjp12Stop( os, secret );
	    os.flush();
	    os.close();
	    //	    socket.close();
	} catch(IOException ex ) {
	    System.out.println("Error stopping Tomcat with Ajp12 on " +
				      address + ":" + portInt + " " + ex);
	    throw ex;
	}
    }

    /** Small AJP12 client util
     */
    public void sendAjp12Stop( OutputStream os, String secret )
	throws IOException
    {
	byte stopMessage[]=new byte[2];
	stopMessage[0]=(byte)254;
	stopMessage[1]=(byte)15;
	os.write( stopMessage );
	if(secret!=null ) 
	    sendAjp12String( os, secret );
    }

    /** Small AJP12 client util
     */
    public void sendAjp12String( OutputStream os, String s )
	throws IOException
    {
	int len=s.length();
	os.write( len/256 );
	os.write( len%256 );
	os.write( s.getBytes() );// works only for ascii
    }
    
    /** Process arguments - set object properties from the list of args.
     */
    public  boolean processArgs(String[] args) {
	for (int i = 0; i < args.length; i++) {
	    String arg = args[i];
	    
	    if (arg.equals("-?")) {
		return false;
	    }
	    if (arg.equals("-h") || arg.equals("-home")) {
		i++;
		if (i < args.length)
		    System.getProperties().put("tomcat.home", args[i]);
		else
		    return false;
	    }
	}
	return true;
    }

    public static void main(String args[] ) {
	try {
	    StopTomcat tomcat=new StopTomcat();
	    if( ! tomcat.processArgs( args ) ) {
		// XXX use sm, i18n
		System.out.println("Usage: java org.apache.tomcat.startup.StopTomcat [ -home TOMCAT_HOME ] ");
		return;
	    }
	    tomcat.execute();
	} catch(Exception ex ) {
	    System.out.println(sm.getString("tomcat.fatal"));
	    ex.printStackTrace();
	    System.exit(1);
	}
    }


}
