package org.apache.tomcat.startup;

import java.net.*;
import java.io.*;

import org.apache.tomcat.core.*;
import org.apache.tomcat.request.*;
import org.apache.tomcat.modules.server.*;
import org.apache.tomcat.modules.session.*;
import org.apache.tomcat.context.*;
import org.apache.tomcat.util.log.*;
import java.security.*;
import java.util.*;

/**
 * Simple example of tomcat embeding.
 * 
 * @author Costin Manolache
 */
public class SimpleTomcat { 
    
    public SimpleTomcat() {
    }

    /** Sample - you can use it to tomcat
     */
    public static void main( String args[] ) {
	try {
	    File pwdF=new File(".");
	    String pwd=pwdF.getCanonicalPath();

	    EmbededTomcat tc=new EmbededTomcat();
	    // relative to pwd
	    tc.getContextManager().setWorkDir( pwd + "/work");
	    
	    Context sctx=tc.addContext("", new URL
				       ( "file", null, pwd + "/webapps/ROOT"),
				       null);
	    sctx.init();

	    sctx=tc.addContext("/examples", new URL
			       ("file", null, pwd + "/webapps/examples"),
			       null);
	    sctx.init();

	    tc.addEndpoint( 8080, null, null);
	    tc.getContextManager().start();
	} catch (Throwable t ) {
	    // this stack trace is ok, i guess, since it's just a
	    // sample main
	    t.printStackTrace();
	}
    }
}
