/*
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


package org.apache.tomcat.request;

import org.apache.tomcat.core.*;
import org.apache.tomcat.util.*;
import org.apache.tomcat.util.log.*;
import org.apache.tomcat.helper.*;
import org.apache.tomcat.util.xml.*;
import java.io.*;
import java.net.*;
import java.util.*;
import org.xml.sax.*;

/**
 *  Memory based realm - will authenticate and check the permissions
 *  for a request using a simple, in-memory list of users.
 *  This is for "demo" purpose only, to allow auth in standalone tomcat
 *  for developers.
 *
 *  There are no restrictions or rules on how to authenticate - you have
 *  full control over the process.
 *
 */
public class SimpleRealm extends  BaseInterceptor {

    MemoryRealm memoryRealm;
    int reqRolesNote=-1;
    int reqRealmSignNote=-1;
    String filename;
    public SimpleRealm() {
    }

    public void contextInit(Context ctx)
	throws TomcatException
    {
        super.contextInit(ctx);
        init(cm,ctx);
        try {
            // XXX make the name a "global" static -
            reqRolesNote = cm.getNoteId( ContextManager.REQUEST_NOTE,
                         "required.roles");
                reqRealmSignNote = cm.getNoteId( ContextManager.REQUEST_NOTE
                                       , "realm.sign");
        } catch( TomcatException ex ) {
            log("getting note for " + cm, ex);
            throw new RuntimeException( "Invalid state ");
        }
    }

    public int authenticate( Request req, Response response )
    {
	// Extract the credentials
	Hashtable cred=new Hashtable();
	SecurityTools.credentials( req, cred );

	// This realm will use only username and password callbacks
	String user=(String)cred.get("username");
	String password=(String)cred.get("password");

	if( debug > 0 ) log( "Verify user=" + user + " pass=" + password );
	if( memoryRealm.checkPassword( user, password ) ) {
	    if( debug > 0 ) log( "Auth ok, user=" + user );
            Context ctx = req.getContext();
            if (ctx != null)
                req.setAuthType(ctx.getAuthMethod());
	    req.setRemoteUser( user );
            req.setNote(reqRealmSignNote,this);
	}
	return 0;
    }

    public int authorize( Request req, Response response, String roles[] )
    {
        if( roles==null || roles.length==0 ) {
            // request doesn't need authentication
            return 0;
        }

        Context ctx=req.getContext();

        String userRoles[]=null;
        String user=req.getRemoteUser();
        if( user==null )
	    return 401;

        if( ! this.equals(req.getNote(reqRealmSignNote)) ){
                return 0;
        }



        if( debug > 0 ) log( "Controled access for " + user + " " +
                     req + " " + req.getContainer() );

        userRoles = memoryRealm.getUserRoles( user );
            if ( userRoles == null )
                return 0;
        req.setUserRoles( userRoles );

        if( SecurityTools.haveRole( userRoles, roles ))
            return 0;

        if( debug > 0 ) log( "UnAuthorized " + roles[0] );
        return 401;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String newFilename) {
        filename = newFilename;
    }

    /** Called when the ContextManger is started
     */
    public void engineInit(ContextManager cm) throws TomcatException {
//        super.engineInit(cm);
        init(cm,null);
    }

    void init(ContextManager cm,Context ctx) {
	if( memoryRealm==null) {
	    memoryRealm = new MemoryRealm(filename,cm.getHome());
	    try {
		memoryRealm.readMemoryRealm();
	    } catch(Exception ex ) {
		log("initting " + cm, ex);
		memoryRealm=null;
	    }
	}
    }

    class MemoryRealm {
        // String user -> password
        Hashtable passwords=new Hashtable();
        // String role -> Vector users
        Hashtable roles=new Hashtable();
        // user -> roles
        Hashtable userRoles= new Hashtable();
        String filename;
        String home;

        MemoryRealm(String fn,String home) {
            this.home=home;
            filename=fn;
        }

        public Hashtable getRoles() {
            return roles;
        }

        public void addUser(String name, String pass, String groups ) {
            if( debug > 0 )  log( "Add user " + name + " " + pass + " " + groups );
            passwords.put( name, pass );
            groups += ",";
            while (true) {
                int comma = groups.indexOf(",");
                if (comma < 0)
                    break;
                addRole( groups.substring(0, comma).trim(), name);
                groups = groups.substring(comma + 1);
            }
        }

        public void addRole( String role, String user ) {
            Vector users=(Vector)roles.get(role);
            if(users==null) {
                users=new Vector();
                roles.put(role, users );
            }
            users.addElement( user );

            Vector thisUserRoles=(Vector)userRoles.get( user );
            if( thisUserRoles == null ) {
                thisUserRoles = new Vector();
                userRoles.put( user, thisUserRoles );
            }
            thisUserRoles.addElement( role );
        }

        public boolean checkPassword( String user, String pass ) {
            if( user==null ) return false;
            if( debug > 0 ) log( "check " + user+ " " + pass + " " + passwords.get( user ));
            return pass.equals( (String)passwords.get( user ) );
        }

        public String[] getUserRoles( String user ) {
            Vector v=(Vector)userRoles.get( user );
            if( v==null) return null;
            String roles[]=new String[v.size()];
            for( int i=0; i<roles.length; i++ ) {
                roles[i]=(String)v.elementAt( i );
            }
            return roles;
        }

        public boolean userInRole( String user, String role ) {
            Vector users=(Vector)roles.get(role);
            if( debug > 0 ) log( "check role " + user+ " " + role + " "  );
            if(users==null) return false;
            return users.indexOf( user ) >=0 ;
        }
        void readMemoryRealm() throws Exception {
            File f;
            if (filename != null)
                f=new File( home + File.separator + filename );
            else
                f=new File( home + "/conf/tomcat-users.xml");

            if( ! f.exists() ) {
                log( "File not found  " + f );
                return;
            }
            XmlMapper xh=new XmlMapper();
            if( debug > 5 ) xh.setDebug( 2 );

            // call addUser using attributes as parameters
            xh.addRule("tomcat-users/user",
                       new XmlAction() {
                               public void start(SaxContext sctx) throws Exception {
                                   int top=sctx.getTagCount()-1;
                                   MemoryRealm mr=(MemoryRealm)sctx.getRoot();
                                   AttributeList attributes = sctx.getAttributeList( top );
                                   String user=attributes.getValue("name");
                                   String pass=attributes.getValue("password");
                                   String group=attributes.getValue("roles");
                                   mr.addUser( user, pass, group );
                               }
                           }
                       );

            xh.readXml( f, this );
        }
    }

}

