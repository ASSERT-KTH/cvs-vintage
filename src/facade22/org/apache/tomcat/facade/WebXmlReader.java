package org.apache.tomcat.facade;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Stack;
import java.util.Vector;

import org.apache.tomcat.core.BaseInterceptor;
import org.apache.tomcat.core.Context;
import org.apache.tomcat.core.ContextManager;
import org.apache.tomcat.core.TomcatException;
import org.apache.tomcat.util.compat.Action;
import org.apache.tomcat.util.compat.Jdk11Compat;
import org.apache.tomcat.util.io.FileUtil;
import org.apache.tomcat.util.res.StringManager;
import org.apache.tomcat.util.xml.SaxContext;
import org.apache.tomcat.util.xml.XmlAction;
import org.apache.tomcat.util.xml.XmlMapper;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

// XXX XXX Specific to servlet 2.2 

/**
 * Read a servlet2.2 web.xml file and call the apropriate internal API
 * to set up the configuration.
 * 
 * @author costin@dnt.ro
 */
public class WebXmlReader extends BaseInterceptor {
    public static final String defaultWelcomeList[]={"index.jsp", "index.html", "index.htm"};
    public static final int DEFAULT_SESSION_TIMEOUT=30;

    private static StringManager sm =StringManager.getManager("org.apache.tomcat.resources");
    boolean validate=true;
    static Jdk11Compat jdk11Compat=Jdk11Compat.getJdkCompat();
    

    public WebXmlReader() {
    }

    public void setValidate( boolean b ) {
	validate=b;
    }

    private void readDefaultWebXml( Context ctx ) throws TomcatException {
	ContextManager cm=ctx.getContextManager();
	String home = cm.getHome();
	
	File default_xml=new File( home + "/conf/web.xml" );
	
	// try the default ( installation )
	if( ! default_xml.exists() ) {
	    String tchome=ctx.getContextManager().getInstallDir();
	    if( tchome != null )
		default_xml=new File( tchome + "/conf/web.xml");
	}
	
	if( ! default_xml.exists() )
	    return;
	
	processWebXmlFile(ctx , default_xml.getPath());
    }
    
    public void contextInit(Context ctx) throws TomcatException {
	if( debug > 0 )
	    log("contextInit  " + ctx.getPath() + " " +ctx.getDocBase() );
	ContextManager cm=ctx.getContextManager();
	
	try {
	    // Defaults 
	    ctx.setSessionTimeOut( 30 );

	    // We may read a "default" web.xml from INSTALL/conf/web.xml -
	    // the code is commented out right now because we want to
	    // consolidate the config in server.xml ( or API calls ),
	    // we may put it back for 3.2 if needed.
	    // note that web.xml have to be cleaned up - only diff from
	    // default should be inside
	    // readDefaultWebXml( ctx );
	    
	    File inf_xml = new File(ctx.getAbsolutePath() +
				    "/WEB-INF/web.xml");
	    if( inf_xml.exists() )
		processWebXmlFile(ctx, inf_xml.getPath() );

	    // If the user haven't set any welcome file, use the
	    // defaults.
	    // If the user specifies welcome files, it's assumed he
	    // doesn't want extra
	    String newWF[]=ctx.getWelcomeFiles();
	    if( newWF==null || newWF.length==0 ) {
		for( int i=0; i< defaultWelcomeList.length; i++ )
		    ctx.addWelcomeFile( defaultWelcomeList[i]);
	    }
	} catch (Exception e) {
	    String msg = sm.getString("context.getConfig.e",ctx.getPath() + " " + ctx.getDocBase());
	    log(msg, e);
	}

    }

    static class WebXmlErrorHandler implements ErrorHandler{
	Context ctx;
	XmlMapper xm;
	boolean ok=true;
	WebXmlErrorHandler( XmlMapper xm,Context ctx ) {
	    this.ctx=ctx;
	    this.xm=xm;
	}

	public void warning (SAXParseException exception)
	    throws SAXException {
	    ok=false;
	    ctx.log("web.xml: Warning " + exception );
	    ctx.log(xm.positionToString());
	}
	public void error (SAXParseException exception)
	    throws SAXException
	{
	    ok=false;
	    ctx.log("web.xml: Error " + exception );
	    ctx.log(xm.positionToString());
	}
	public void fatalError (SAXParseException exception)
	    throws SAXException
	{
	    ok=false;
	    ctx.log("web.xml: Fatal error " + exception );
	    ctx.log(xm.positionToString());
	    throw new SAXException( "Fatal error " + exception );
	}
	public boolean isOk() {
	    return ok;
	}
    }
    
    void processWebXmlFile( Context ctx, String file) {
	try {
	    File f=new File(FileUtil.patch(file));
	    if( ! f.exists() ) {
		ctx.log( "File not found " + f + ", using only defaults" );
		return;
	    }
	    if( ctx.getDebug() > 0 ) ctx.log("Reading " + file );
	    XmlMapper xh=new XmlMapper();
	    WebXmlErrorHandler xeh=null;
	    File v=new File( ctx.getWorkDir(), "webxmlval.txt" );
	    if( validate ) {
		if( ! v.exists() || 
		    v.lastModified() < f.lastModified() ) {
		    ctx.log("Validating web.xml");
		    xh.setValidating(true);
		    xeh=new WebXmlErrorHandler( xh, ctx ); 
		    xh.setErrorHandler( xeh );
		}
	    }

	    // By using dtdURL you brake most parsers ( at least xerces )
	    xh.registerDTDRes("-//Sun Microsystems, Inc.//DTD Web Application 2.2//EN",
			      "org/apache/tomcat/resources/web.dtd");

	    xh.addRule("web-app/context-param", xh.methodSetter("addInitParameter", 2) );
	    xh.addRule("web-app/context-param/param-name", xh.methodParam(0) );
	    xh.addRule("web-app/context-param/param-value", xh.methodParam(1) );

	    xh.addRule("web-app/description", xh.methodSetter("setDescription", 0) );
	    xh.addRule("web-app/icon/small-icon", xh.methodSetter("setIcon", 0) );
	    xh.addRule("web-app/distributable", xh.methodSetter("setDistributable", 0) );

	    xh.addRule("web-app/servlet-mapping", xh.methodSetter("addServletMapping", 2) );
	    xh.addRule("web-app/servlet-mapping/servlet-name", xh.methodParam(1) );
	    xh.addRule("web-app/servlet-mapping/url-pattern", xh.methodParam(0) );

	    xh.addRule("web-app/taglib", xh.methodSetter("addTaglib", 2) );
	    xh.addRule("web-app/taglib/taglib-uri", xh.methodParam(0) );
	    xh.addRule("web-app/taglib/taglib-location", xh.methodParam(1) );

	    xh.addRule("web-app/env-entry", xh.methodSetter("addEnvEntry", 4) );
	    xh.addRule("web-app/env-entry/env-entry-name", xh.methodParam(0) );
	    xh.addRule("web-app/env-entry/env-entry-type", xh.methodParam(1) );
	    xh.addRule("web-app/env-entry/env-entry-value", xh.methodParam(2) );
	    xh.addRule("web-app/env-entry/description", xh.methodParam(3) );

	    xh.addRule("web-app/login-config", xh.methodSetter("setLoginConfig", 4) );
	    xh.addRule("web-app/login-config/auth-method", xh.methodParam(0) );
	    xh.addRule("web-app/login-config/realm-name", xh.methodParam(1) );
	    xh.addRule("web-app/login-config/form-login-config/form-login-page", xh.methodParam(2) );
	    xh.addRule("web-app/login-config/form-login-config/form-error-page", xh.methodParam(3) );

	    xh.addRule("web-app/mime-mapping", xh.methodSetter("addContentType", 2) );
	    xh.addRule("web-app/mime-mapping/extension", xh.methodParam(0) );
	    xh.addRule("web-app/mime-mapping/mime-type", xh.methodParam(1) );

	    xh.addRule("web-app/welcome-file-list/welcome-file", xh.methodSetter("addWelcomeFile", 0) );

	    xh.addRule("web-app/error-page", xh.methodSetter("addErrorPage",2) );
	    xh.addRule("web-app/error-page/error-code", xh.methodParam(0) );
	    xh.addRule("web-app/error-page/exception-type", xh.methodParam(0) );
	    xh.addRule("web-app/error-page/location", xh.methodParam(1) );

	    xh.addRule("web-app/session-config", xh.methodSetter("setSessionTimeOut", 1, new String[]{"int"}));
	    xh.addRule("web-app/session-config/session-timeout", xh.methodParam(0));

	    // Servlet
	    xh.addRule("web-app/servlet", xh.objectCreate("org.apache.tomcat.facade.ServletInfo") ); // servlet-wrapper
	    xh.addRule("web-app/servlet", xh.setParent( "setContext") ); // remove it from stack when done
	    //	    xh.addRule("web-app/servlet", xh.addChild("addServlet", "org.apache.tomcat.core.Handler") );

	    final WebXmlReader wxr=this;
	    xh.addRule("web-app/servlet", new XmlAction() {
			   public void end( SaxContext xctx)
			       throws Exception {
			       ServletInfo sw=(ServletInfo)
				   xctx.currentObject();
			       Context cctx=(Context)xctx.previousObject();
			       sw.addServlet(cctx, wxr);
			   }
		       }
		   );
	    // remove it from stack when done
	    xh.addRule("web-app/servlet/servlet-name", xh.methodSetter("setServletName",0) );
	    xh.addRule("web-app/servlet/servlet-class", xh.methodSetter("setServletClassName",0));
	    xh.addRule("web-app/servlet/jsp-file",
		       xh.methodSetter("setJspFile",0));

	    xh.addRule("web-app/servlet/security-role-ref", xh.methodSetter("addSecurityMapping", 3) );
	    xh.addRule("web-app/servlet/security-role-ref/role-name", xh.methodParam(0) );
	    xh.addRule("web-app/servlet/security-role-ref/role-link", xh.methodParam(1) );
	    xh.addRule("web-app/servlet/security-role-ref/description", xh.methodParam(2) );

	    xh.addRule("web-app/servlet/init-param", xh.methodSetter("addInitParam", 2) ); // addXXX
	    xh.addRule("web-app/servlet/init-param/param-name", xh.methodParam(0) );
	    xh.addRule("web-app/servlet/init-param/param-value", xh.methodParam(1) );

	    xh.addRule("web-app/servlet/icon/small-icon", xh.methodSetter("setIcon",0 )); // icon, body
	    xh.addRule("web-app/servlet/description", xh.methodSetter("setDescription", 0) ); // description, body
	    xh.addRule("web-app/servlet/load-on-startup", xh.methodSetter("setLoadOnStartUp", 0 ));


	    addSecurity( xh );

            Object ctx1=null;

            xh.useLocalLoader( false ); // we'll use our own parser for web.xml
            
            // Perform the reading with the context privs
            Object pd=ctx.getAttribute( Context.ATTRIB_PROTECTION_DOMAIN);
            //            System.out.println("Protection domain " + pd);

            if( pd!=null ) {
                // Do the action in a sandbox, with context privs
                PriviledgedAction di = new PriviledgedAction(xh, f, ctx);
                try {
                    ctx1=jdk11Compat.doPrivileged(di, pd);
                } catch( TomcatException ex1 ) {
                    throw ex1;
                } catch( Exception ex ) {
                    throw new TomcatException( ex );
                }
            } else {
                ctx1=xh.readXml(f, ctx);
            }

	    if( validate && xeh != null && xeh.isOk() ) {
		// don't create the validation mark if an error was detected
		try {
		    FileOutputStream fos=new FileOutputStream( v );
		    fos.write( 1 );
		    fos.close();
		} catch(IOException ex ) {
		    ctx.log( "Error creating validation mark ", ex );
		}
	    }
	} catch(Exception ex ) {
	    log("ERROR initializing " + file, ex);
	    try {
		ctx.setState( Context.STATE_DISABLED );
	    } catch(Exception ex1 ) {
		ex1.printStackTrace();
	    }
	    // XXX we should invalidate the context and un-load it !!!
	}
    }


    // Sandbox support
    static class PriviledgedAction extends Action {
        XmlMapper xh;
        File f;
        Context ctx;
        
	public PriviledgedAction(XmlMapper xh, File f, Context ctx ) {
	    this.xh=xh;
	    this.ctx=ctx;
            this.f=f;
	}           
	public Object run() throws Exception {
            return xh.readXml(f, ctx);
	}           
    }    



    
    // Add security rules - complex code
    void addSecurity( XmlMapper xh ) {
	xh.addRule("web-app/security-constraint",
		   new SCAction() );

	xh.addRule("web-app/security-constraint/user-data-constraint/transport-guarantee",
		   new XmlAction() {
			   public void end( SaxContext ctx) throws Exception {
			       Stack st=ctx.getObjectStack();
			       SecurityConstraint rc=(SecurityConstraint)st.peek();
			       String  body=ctx.getBody().trim();
			       rc.setTransport( body );
			   }
		       }
		   );
	xh.addRule("web-app/security-constraint/auth-constraint/role-name",
		   new XmlAction() {
			   public void end( SaxContext ctx) throws Exception {
			       Stack st=ctx.getObjectStack();
			       SecurityConstraint rc=(SecurityConstraint)st.peek();
			       String  body=ctx.getBody().trim();
			       rc.addRole( body );
			   }
		       }
		   );

	xh.addRule("web-app/security-constraint/web-resource-collection",
		   new XmlAction() {
			   public void start( SaxContext ctx) throws Exception {
			       Stack st=ctx.getObjectStack();
			       st.push(new ResourceCollection());
			   }
			   public void end( SaxContext ctx) throws Exception {
			       Stack st=ctx.getObjectStack();
			       ResourceCollection rc=(ResourceCollection)st.pop();
			       SecurityConstraint sc=(SecurityConstraint)st.peek();
			       st.push( rc );
			       sc.addResourceCollection( rc );
			   }
			   public void cleanup( SaxContext ctx) {
			       Stack st=ctx.getObjectStack();
			       Object o=st.pop();
			   }
		       }
		   );

	xh.addRule("web-app/security-constraint/web-resource-collection/url-pattern",
		   new XmlAction() {
			   public void end( SaxContext ctx) throws Exception {
			       Stack st=ctx.getObjectStack();
			       ResourceCollection rc=(ResourceCollection)st.peek();
			       String  body=ctx.getBody().trim();
			       rc.addUrlPattern( body );
			   }
		       }
		   );
	xh.addRule("web-app/security-constraint/web-resource-collection/http-method",
		   new XmlAction() {
			   public void end( SaxContext ctx) throws Exception {
			       Stack st=ctx.getObjectStack();
			       ResourceCollection rc=(ResourceCollection)st.peek();
			       String  body=ctx.getBody().trim();
			       rc.addHttpMethod( body );
			   }
		       }
		   );
    }

}

/** Specific action for Security-constraint
 */
class SCAction extends XmlAction {
    public void start( SaxContext ctx) throws Exception {
	Stack st=ctx.getObjectStack();
	st.push(new SecurityConstraint());
    }
    public void end( SaxContext ctx) throws Exception {
	Stack st=ctx.getObjectStack();
	String tag=ctx.getTag(ctx.getTagCount()-1);
	SecurityConstraint sc=(SecurityConstraint)st.pop();
	Context context=(Context)st.peek();

	st.push( sc ); // restore stack
	// add all patterns that will need security

	String roles[]=sc.getRoles();
	String transport=sc.getTransport();
	Enumeration en=sc.getResourceCollections();
	while( en.hasMoreElements()) {
	    ResourceCollection rc=(ResourceCollection)en.nextElement();
	    String paths[]=rc.getPatterns();
	    String meths[]=rc.getMethods();
	    context.addSecurityConstraint(  paths, meths ,
					    roles, transport);
	}
    }
    public void cleanup( SaxContext ctx) {
	Stack st=ctx.getObjectStack();
	Object o=st.pop();
    }
}

class SecurityConstraint {
    Vector roles=new Vector();
    String transport;
    Vector resourceC=new Vector();

    public SecurityConstraint() {
    }

    public void setTransport( String transport ) {
	this.transport=transport;
    }

    public String getTransport() {
	return this.transport;
    }

    public void addRole(String role ) {
	roles.addElement( role );
    }

    public void addResourceCollection( ResourceCollection rc ) {
	resourceC.addElement( rc );
    }

    public String []getRoles() {
	String rolesA[]=new String[roles.size()];
	for( int i=0; i< rolesA.length; i++ ) {
	    rolesA[i]=(String)roles.elementAt( i );
	}
	return rolesA;
    }
    public Enumeration getResourceCollections() {
	return resourceC.elements();
    }
}

class ResourceCollection {
    Vector urlP=new Vector();
    Vector methods=new Vector();

    public ResourceCollection() {
    }

    public void addUrlPattern( String pattern ) {
	urlP.addElement( pattern );
    }

    public void addHttpMethod( String method ) {
	methods.addElement( method );
    }

    public String []getMethods() {
	String methodsA[]=new String[methods.size()];
	for( int i=0; i< methodsA.length; i++ ) {
	    methodsA[i]=(String)methods.elementAt( i );
	}
	return methodsA;
    }

    public String []getPatterns() {
	String patternsA[]=new String[urlP.size()];
	for( int i=0; i< patternsA.length; i++ ) {
	    patternsA[i]=(String)urlP.elementAt( i );
	}
	return patternsA;
    }


}

