/*   
 *  Copyright 1999-2004 The Apache Sofware Foundation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.tomcat.modules.aaa;

import java.io.File;
import java.security.Principal;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.tomcat.core.Context;
import org.apache.tomcat.core.ContextManager;
import org.apache.tomcat.core.TomcatException;
import org.apache.tomcat.util.aaa.SimplePrincipal;
import org.apache.tomcat.util.xml.SaxContext;
import org.apache.tomcat.util.xml.XmlAction;
import org.apache.tomcat.util.xml.XmlMapper;
import org.xml.sax.AttributeList;

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
