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

package org.apache.tomcat.startup;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Hashtable;

import org.apache.tomcat.util.IntrospectionUtils;

/**
 * Simple task for enabling the admin interface. Can be used as
 * an ant task or from command line.
 * 
 * @author Costin Manolache
 */
public class EnableAdmin {

    Hashtable attributes=new Hashtable();
    String args[];
    
    public EnableAdmin() {
    }
    
    //-------------------- Properties --------------------
    
    public void setHome(String home) {
	attributes.put( "home", home );
    }

    public void setInstall(String install) {
	attributes.put( "install", install );
    }
    
    public void setArgs(String args[]) {
	attributes.put("args", args);
	this.args=args;
    }

    public void setConfig( String s ) {
	attributes.put( "config" , s );
    }

    public void setAction(String s ) {
	attributes.put("action",s);
	attributes.put(s, "true" );
    }

    public void setSandbox( boolean b ) {
	if( b ) attributes.put( "sandbox", "true" );
    }
    
    public void setStop( boolean b ) {
	if( b ) attributes.put( "stop", "true" );
    }
    
    public void setEnableAdmin( boolean b ) {
	if( b ) attributes.put( "enableAdmin", "true" );
    }
    
    public void setParentClassLoader( ClassLoader cl ) {
	attributes.put( "parentClassLoader", cl );
    }

    public void setCommonClassLoader( ClassLoader cl ) {
	attributes.put( "commonClassLoader", cl );
    }

    public void setAppsClassLoader( ClassLoader cl ) {
	attributes.put( "appsClassLoader", cl );
    }

    public void setContainerClassLoader( ClassLoader cl ) {
    	attributes.put( "containerClassLoader", cl );
    }
    
    // -------------------- execute --------------------
    
    public void execute() throws Exception
    {
	if( args!=null ) {
	    boolean ok=processArgs( args );
	    if ( ! ok ) {
		printUsage();
		return;
	    }
	}

	System.out.println("Overriding apps-admin settings ");
	String home=(String)attributes.get("home");
	if( home==null) home=(String)attributes.get("install");
	FileWriter fw=new FileWriter( home + File.separator +
				      "conf" + File.separator +
				      "apps-admin.xml" );
	PrintWriter pw=new PrintWriter( fw );
        pw.println( "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
	pw.println( "<webapps>" );
        pw.println( "    <!-- Special rules for the admin webapplication -->");
	pw.println( "    <Context path=\"/admin\"");
	pw.println( "             docBase=\"webapps/admin\"");
	pw.println( "             trusted=\"true\">");
	pw.println( "            <SimpleRealm filename=\"conf/users/admin-users.xml\" />");
	pw.println( "    </Context>");
	pw.println( "</webapps>" );
	pw.close();
    }
	
    // -------------------- Command-line args processing --------------------

    public static void printUsage() {
	//System.out.println(sm.getString("tomcat.usage"));
	System.out.println("Usage: java org.apache.tomcat.startup.EnableAdmin {options}");
	System.out.println("  Options are:");
        System.out.println("    -home                      Tomcat home directory");
        System.out.println();
    }


    static String options1[]= { };

    public String[] getOptions1() {
	return options1;
    }
    public Hashtable getOptionAliases() {
	return null;
    }
    
    /** Process arguments - set object properties from the list of args.
     */
    public  boolean processArgs(String[] args) {
	try {
	    return IntrospectionUtils.processArgs( this, args,getOptions1(),
						   null, getOptionAliases());
	} catch( Exception ex ) {
	    ex.printStackTrace();
	    return false;
	}
    }

    /** Callback from argument processing
     */
    public void setProperty(String s,Object v) {
	if ( dL > 0 ) debug( "Generic property " + s );
	attributes.put(s,v);
    }

    /** Called by Main to set non-string properties
     */
    public void setAttribute(String s,Object o) {
        if ( "args".equals(s) ) {
	    String args[]=(String[])o;
	}
        if( o != null )
            attributes.put(s,o);
    }

    // -------------------- Main --------------------

    public static void main(String args[] ) {
	try {
	    EnableAdmin task=new EnableAdmin();
	    task.setArgs(args);
            task.execute();
	} catch(Exception ex ) {
	    ex.printStackTrace();
	    System.exit(1);
	}
    }

    private static int dL=10;
    private void debug( String s ) {
	System.out.println("EnableAdmin: " + s );
    }
}
