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

package org.apache.tomcat.modules.aaa;

import org.apache.tomcat.core.*;
import org.apache.tomcat.util.*;
import org.apache.tomcat.util.log.*;
import org.apache.tomcat.util.xml.*;
import org.apache.tomcat.util.aaa.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.security.Principal;
import org.xml.sax.*;

/**
 *  Memory based realm - will authenticate an user and password against
 *  an xml file. The file is fully read in memory when the context is
 *  initialized.
 *
 *  The default file is TOMCAT_HOME/conf/users/tomcat-users.xml. You can
 *  change it, and you can also set this module as a per context
 *  interceptor, so that each module have it's own realm.
 *
 *  The module will use "credentials.user" and "credentials.password"
 *  request notes. It's role is to verify those notes, other module is
 *  specialized in extracting the information from the request.
 *
 */
public class SimpleRealm extends  RealmBase {

    MemoryRealm memoryRealm;
    String filename="/conf/users/tomcat-users.xml";


    public SimpleRealm() {
    }

    // -------------------- Properties --------------------
    public String getFilename() {
        return filename;
    }

    public void setFilename(String newFilename) {
        filename = newFilename;
    }

    // -------------------- Hooks --------------------

    public void contextInit(Context ctx)
	throws TomcatException
    {
	ContextManager cm=ctx.getContextManager();
	if( memoryRealm==null) {
	    memoryRealm = new MemoryRealm(filename,
					  cm.getHome());
	    try {
		memoryRealm.readMemoryRealm();
	    } catch(Exception ex ) {
		log("Error loading realm file " + cm.getHome() + "/"  +
		    filename, ex);
		memoryRealm=null;
	    }
	}
    }

    class MemoryRealm {
        // String user -> password
	//        Hashtable passwords=new Hashtable();
        // String role -> Vector users
	//        Hashtable roles=new Hashtable();
        // user -> roles
        // Hashtable userRoles= new Hashtable();

	Hashtable principals=new Hashtable();
	String filename;
        String home;

        MemoryRealm(String fn,String home) {
            this.home=home;
            filename=fn;
        }

	public SimpleRealmPrincipal getPrincipal( String user ) {
	    return (SimpleRealmPrincipal)principals.get(user);
	}

	public void addPrincipal( String name, Principal p ) {
	    principals.put( name, p );
	}

        public void addUser(String name, String pass, String groups ) {
            if( getDebug() > 0 )  log( "Add user " + name + " " +
				       pass + " " + groups );
	    SimpleRealmPrincipal sp=new SimpleRealmPrincipal( name, pass );
	    sp.addRoles( groups );
	    principals.put( name, sp );
        }

        void readMemoryRealm() throws Exception {
            File f;
            if (filename != null)
                f=new File( home + File.separator + filename );
            else
                f=new File( home + "/conf/users/tomcat-users.xml");

            if( ! f.exists() ) {
                log( "File not found  " + f );
                return;
            }
            XmlMapper xh=new XmlMapper();
            if( getDebug() > 5 ) xh.setDebug( 2 );

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

    public static class SimpleRealmPrincipal extends SimplePrincipal {
	private String pass;
	private Vector roles=new Vector();

	SimpleRealmPrincipal(String name, String pass) {
	    super( name );
	    this.pass=pass;
	}

	// local methods

	private void addRole(String role ) {
	    roles.addElement( role );
	}

	String getCredentials() {
	    return pass;
	}

	// backward compat - bad XML format !!!
	void addRoles( String groups ) {
	    groups += ",";
            while (true) {
                int comma = groups.indexOf(",");
                if (comma < 0)
                    break;
                addRole( groups.substring(0, comma).trim() );
                groups = groups.substring(comma + 1);
            }
	}

	String[] getUserRoles( ) {
            String rolesA[]=new String[roles.size()];
            for( int i=0; i<roles.size(); i++ ) {
                rolesA[i]=(String)roles.elementAt( i );
            }
            return rolesA;
        }

	// 	public boolean userInRole( String role ) {
	//             return roles.indexOf( role ) >=0 ;
	//         }

    }

    /**
     * getPrincipal
     * @param username
     * @return java.security.Principal
     */
    protected Principal getPrincipal(String username) {
        return memoryRealm.getPrincipal( username );
    }

    /**
     * getCredentials
     * @param username
     * @return java.lang.String
     */
    protected String getCredentials(String username) {
        SimpleRealmPrincipal sp=memoryRealm.getPrincipal( username );
        if( sp!=null ) {
            return sp.getCredentials();
        }
        return null;
    }

    /**
     * getUserRoles
     * @param username
     * @return java.lang.String
     */
    protected String[] getUserRoles(String username) {
        SimpleRealmPrincipal sp=memoryRealm.getPrincipal( username );
        if( sp!=null ) {
            return sp.getUserRoles();
        }
        return null;
    }


}
