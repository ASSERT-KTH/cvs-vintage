/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/tomcat/service/connector/Attic/DoorConnectionHandler.java,v 1.3 2000/01/13 18:20:35 costin Exp $
 * $Revision: 1.3 $
 * $Date: 2000/01/13 18:20:35 $
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


package org.apache.tomcat.service.connector;

import java.io.*;
import java.net.*;
import java.util.*;
import org.apache.tomcat.core.*;
import org.apache.tomcat.util.*;
//import org.apache.tomcat.server.*;
import javax.servlet.*;
import javax.servlet.http.*;


interface DoorFunction {
    void call(MsgBuffer buf);
}

class Door {

    static {
	System.out.println("Loading libdoor");
	System.loadLibrary("doorJNI");
	System.out.println("Loaded ok");
    }

    public static native long open(String name);
    public static native void close( long doorid );

    /** buff is used for both input/output
	@return size of result
    */
    public static native int call(long doorid, byte buff[], int argsize);
    public static native int info(long doorid);


    public static final int DOOR_PRIVATE=2; /* from sys/door.h */
    public static final int DOOR_UNREF=1;

    /** cookie can't be used - it is used by java wrapper to point to proc
     */
    public static native long create(String name, DoorFunction proc, int attr);
    public static native long destroy(long id, String name);

}


public class DoorConnectionHandler implements DoorFunction {
    ContextManager contextM;
    
    public DoorConnectionHandler() {
	super();
    }

    public void init( ) {
	Door.create("/tmp/apache.door", this, 0);
    }

    public void setContextManager( ContextManager contextM ) {
	this.contextM=contextM;
    }

    /* Incoming packet */
    public void call( MsgBuffer buf ) {
	try {
	    MsgConnector con=new DoorConnector();
	    RequestImpl rrequest=new RequestImpl();
	    ConnectorResponse rresponse=new ConnectorResponse(con);
	    ConnectorRequest  reqA=new ConnectorRequest(con);
	    rrequest.setRequestAdapter( reqA );


	    contextM.service( rrequest, rresponse );
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

}

class DoorConnector implements MsgConnector {
    public static final int SEND_BODY_CHUNK=3;
    public static final int MAX_PACKET_SIZE=4096;
    public static final int SEND_HEADERS=2;
    
    static final int H_SIZE=4;
    static final int CONTEXT='C';
    static final int SERVLET='S';
    static final int HOSTNAME='N';
    static final int ENV='E';
    static final int HEADER='H';
    static final int END_REQUEST='Z';
    
    
    static final int MAX_REQUEST_SIZE=4096;

    MsgBuffer msg;
    
    public DoorConnector () throws IOException {
	msg=new MsgBuffer( MAX_PACKET_SIZE );
    }    

    public MsgBuffer getMsgBuffer() {
	return msg;
    }
    
    public void send(MsgBuffer msg ) throws IOException {

    }
}
