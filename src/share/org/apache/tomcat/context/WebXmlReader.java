package org.apache.tomcat.context;

import org.apache.tomcat.core.*;
import org.apache.tomcat.util.*;
import org.apache.tomcat.util.xml.*;
import java.beans.*;
import java.io.*;
import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;
import java.util.StringTokenizer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import org.w3c.dom.*;

/**
 * @author costin@dnt.ro
 */
public class WebXmlReader extends BaseContextInterceptor  implements ContextInterceptor {

    private static StringManager sm =StringManager.getManager("org.apache.tomcat.core");
    
    public WebXmlReader() {
    }

    public int contextInit(Context ctx) {
	//	System.out.println("Context(" + ctx.getPath() + "): " + ctx.getDocBase());

	// read default web.xml
	try {
	    processFile(ctx, ctx.getContextManager().getHome() + "/conf/web.xml");
	    processFile(ctx, ctx.getDocBase() + "/WEB-INF/web.xml");
	    XmlMapper xh=new XmlMapper();
	} catch (Exception e) {
	    String msg = sm.getString("context.getConfig.e",ctx.getPath() + " " + ctx.getDocBase());
	    System.out.println(msg);
	}
	return 0;

    }

    void processFile( Context ctx, String file) {
	try {
	    File f=new File(file);

	    XmlMapper xh=new XmlMapper();
	    xh.setDebug( 0 );

	    xh.addRule("web-app/context-param", xh.methodSetter("addInitParameter", 2) );
	    xh.addRule("web-app/context-param/param-name", xh.methodParam(0) ); 
	    xh.addRule("web-app/context-param/param-value", xh.methodParam(1) );

	    xh.addRule("web-app/description", xh.methodSetter("setDescription", 0) );
	    xh.addRule("web-app/icon/small-icon", xh.methodSetter("setIcon", 0) ); 
	    xh.addRule("web-app/distributable", xh.methodSetter("setDistributable", 0) );

	    xh.addRule("web-app/servlet-mapping", xh.methodSetter("addMapping", 2) ); 
	    xh.addRule("web-app/servlet-mapping/servlet-name", xh.methodParam(0) ); 
	    xh.addRule("web-app/servlet-mapping/url-pattern", xh.methodParam(1) );
	    
	    xh.addRule("web-app/mime-mapping", xh.methodSetter("addContentType", 2) ); 
	    xh.addRule("web-app/mime-mapping/extension", xh.methodParam(0) ); 
	    xh.addRule("web-app/mime-mapping/mime-type", xh.methodParam(1) );

	    xh.addRule("web-app/welcome-file-list/welcome-file", xh.methodSetter("addWelcomeFile", 0) );

	    //	    xh.addRule("web-app/taglib", xh.methodSetter("addTagLib", 2) );
	    //	    xh.addRule("web-app/taglib/taglib-uri", xh.methodParam(0) );
	    //	    xh.addRule("web-app/taglib/taglib-location", xh.methodParam(1) ); 

	    xh.addRule("web-app/error-page", xh.methodSetter("addErrorPage",2) );
	    xh.addRule("web-app/error-page/error-code", xh.methodParam(0) );
	    xh.addRule("web-app/error-page/exception-type", xh.methodParam(0) );
	    xh.addRule("web-app/error-page/location", xh.methodParam(1) );
	    
	    xh.addRule("web-app/session-cronfig/session-timeout", xh.methodSetter("setSessionTimeout",0) ); 
	    
	    // Servlet
	    xh.addRule("web-app/servlet", xh.objectCreate("org.apache.tomcat.core.ServletWrapper") ); // servlet-wrapper
	    xh.addRule("web-app/servlet", xh.setParent( "setContext") ); // remove it from stack when done
	    xh.addRule("web-app/servlet", xh.addChild("addServlet", null) ); // remove it from stack when done
	    xh.addRule("web-app/servlet/servlet-name", xh.methodSetter("setServletName",0) ); 
	    xh.addRule("web-app/servlet/servlet-class", xh.methodSetter("setServletClass",0));
	    xh.addRule("web-app/servlet/jsp-file",xh.methodSetter("setPath",0));

	    xh.addRule("web-app/servlet/security-role-ref", xh.methodSetter("addSecurityMapping", 3) ); 
	    xh.addRule("web-app/servlet/security-role-ref/role-name", xh.methodParam(0) );
	    xh.addRule("web-app/servlet/security-role-ref/role-link", xh.methodParam(1) );
	    xh.addRule("web-app/servlet/security-role-ref/description", xh.methodParam(2) ); 
	    
	    xh.addRule("web-app/servlet/init-param", xh.methodSetter("addInitParam", 2) ); // addXXX
	    xh.addRule("web-app/servlet/init-param/param-name", xh.methodParam(0) );
	    xh.addRule("web-app/servlet/init-param/param-value", xh.methodParam(1) );

	    xh.addRule("web-app/servlet/icon/small-icon", xh.methodSetter("setIcon",0) ); // icon, body
	    xh.addRule("web-app/servlet/description", xh.methodSetter("setDescription", 0) ); // description, body
	    xh.addRule("web-app/servlet/load-on-startup", xh.methodSetter("setLoadOnStartUp", 0 ));
	    //	    xh.addRule("web-app/servlet/security-role-ref", new SetProperty() ); // xxx, body
	    
	    // 	    xh.addRule("",
	    // 		       new XmlAction() {
	    // 			       public void end( SaxContext ctx) {
	    // 				   for( int i=0; i<ctx.getTagCount(); i++) System.out.print( ctx.getTag(i)+"/");
	    // 				   System.out.println();
	    // 			       }
	    // 			   });
	    
	    Object ctx1=xh.readXml(f, ctx);
	} catch(Exception ex ) {
	    ex.printStackTrace();
	}
    }

    
}

