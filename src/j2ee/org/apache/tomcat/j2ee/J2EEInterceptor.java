package org.apache.tomcat.j2ee;

import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.tomcat.core.*;
import com.sun.web.server.*;

import com.sun.enterprise.util.JarClassLoader;

import java.net.URL;
import java.net.InetAddress;
import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.net.ssl.*;
import javax.security.cert.X509Certificate;

import com.sun.enterprise.util.*;
import com.sun.enterprise.*;
import com.sun.enterprise.security.*;
import com.sun.enterprise.security.acl.*;
import com.sun.enterprise.log.*;
import com.sun.enterprise.deployment.WebBundleDescriptor;

import org.apache.tomcat.deployment.*;
import org.apache.tomcat.util.*;

// depends on servlet 2.2
import org.apache.tomcat.facade.*;
import org.apache.tomcat.helper.*;

/**
 * Adapter for j2ee.
 * 
 * @author Harish Prabandham
 * @author Costin Manolache
 */
public class J2EEInterceptor extends BaseInterceptor {
    InvocationManagerImpl invM;
    private static LocalStringManagerImpl localStrings;
    private static ServerConfiguration config;
    
    private Hashtable accessLogs = new Hashtable();
    private Hashtable errorLogs = new Hashtable();
    private static final String HTTP_ACCESS_LOG = "web.access.log";
    private static final String HTTP_ERROR_LOG = "web.error.log";
    private static final int BUFFER_SIZE = 1024;

    // auth
    private static int MAX_COUNT = 5;
    private static int SLEEP_TIME = 5000; // milliseconds....

    public J2EEInterceptor() {
	this.invM= (InvocationManagerImpl) Switch.getSwitch().
	    getInvocationManager();
	config= ServerConfiguration.getConfiguration();
	// minimize the changes in j2ee ( temp, until properties are moved)
	localStrings = new
	    LocalStringManagerImpl(com.sun.web.security.DataConstraint.class);
    }

    public void engineInit( ContextManager cm ) throws TomcatException {
	super.engineInit(cm);
	debug=0;
    }
    
    public void contextInit( Context ctx)
	throws TomcatException
    {
    }
    
    public int preService(Request request, Response response) {
	Context ctx = request.getContext();
	Handler sw=request.getWrapper();
	if( ! (sw instanceof ServletWrapper) )
	    return 0;
	try {
	    invM.preServletInvoke( ctx.getFacade(),
				   ((ServletWrapper)sw).getServlet(), 
				   request.getFacade(),
				   response.getFacade() );
	} catch(Exception ex ) {
	    return -1;
	}
	return 0;
    }
    public int postService(Request request, Response response) {
	Context ctx = request.getContext();
	Handler sw=request.getWrapper();
	if( ! (sw instanceof ServletWrapper) )
	    return 0;
	try {
	    invM.postServletInvoke( ctx.getFacade(),
				    ((ServletWrapper)sw).getServlet(), 
				    request.getFacade(),
				    response.getFacade() );
	    intLogRequest( request.getContext().getPath(),
			   request.getRequestURI(),
			   null);
	} catch(Exception ex ) {
	    return -1;
	}
	return 0;
    }
    
    /** Servlet Init  notification
     */
    public void preServletInit( Context ctx, ServletWrapper sw )
	throws TomcatException
    {
	try {
	    invM.preInitInvoke( ctx.getFacade(), sw.getServlet());
	} catch(Exception ex ) {
	    log("XXX " + invM + " " + ctx + " " + sw );
	    throw new TomcatException( "Error in j2ee adapter " , ex );
	}
    }

    
    public void postServletInit( Context ctx, ServletWrapper sw )
	throws TomcatException
    {
	try {
	    invM.postInitInvoke( ctx.getFacade(), sw.getServlet());
	} catch(Exception ex ) {
	    throw new TomcatException( "Error in j2ee adapter " , ex );
	}
    }

    /** Servlet Destroy  notification
     */
    public void preServletDestroy( Context ctx, ServletWrapper sw )
	throws TomcatException
    {
	try {
	    if( sw != null && ctx != null )
		invM.preDestroyInvoke( ctx.getFacade(), sw.getServlet());
	    else
		log("XXX J2EEInterceptor: sw, ctx=" +
				   sw + " " +ctx);
	} catch(Exception ex ) {
	    ex.printStackTrace();
	    throw new TomcatException( "Error in j2ee adapter " , ex );
	}
    }

    
    public void postServletDestroy( Context ctx, ServletWrapper sw )
	throws TomcatException
    {
	try {
	    if( sw != null && ctx != null )
		invM.postDestroyInvoke( ctx.getFacade(), sw.getServlet());
	    else
		log("XXX J2EEInterceptor: sw, ctx=" +
				   sw + " " +ctx);
	} catch(Exception ex ) {
	    throw new TomcatException( "Error in j2ee adapter " , ex );
	}
    }

    // -------------------- Authentication hooks --------------------
    
    public int authenticate( Request req, Response res ) {
	Context ctx=req.getContext();
	
	// Extract the credentials
	Hashtable cred=new Hashtable();
	SecurityTools.credentials( req, cred );

	// This realm will use only username and password callbacks
	String user=(String)cred.get("username");
	String password=(String)cred.get("password");
	if( debug>0 ) log( "Try to auth " + user + " " + password);

	if( user==null || password == null ) {
	    // Need auth, but have no user/pass
	    return 0;
	}
	byte authData[]=password.getBytes();
	
	String realm="default";  //ctx.getRealmName();
	String authM=null;// ctx.getAuthMethod();

	LoginContext lc = initLoginContext(realm);
	if( lc==null ) {
	    log("Authenticate - Can't get LoginContext....");
	    return 0;
	}
	
	if(authM != null) 
	    lc.setAuthenticationMethod(authM);
	
	SecurityContext.setCurrent(null);

	try {
	    lc.login(user,authData);
	} catch (LoginException le) {
	    // Log the information here
	    // le.printStackTrace();
	    Log.err.println(le);
	    //le.printStackTrace();
	    log("Login failed for..: " + user);
	    return 0;
	}

	if(debug>0) {
	    log("Login succeeded for..: " + user);
	}

	req.setRemoteUser( user );
	req.setUserPrincipal( getUserPrincipal() );

	return 0;
    }

    public int authorize( Request req, Response response, String roles[] )
    {
	if( roles==null ) {
	    return 0;
	}
	
	Context ctx=req.getContext();
	WebBundleDescriptor wbd = (WebBundleDescriptor) 
	    Switch.getSwitch().getDescriptorFor(ctx.getFacade());

	String appName="default";
	if(wbd != null)
	    appName=wbd.getApplication().getName();
	if( debug>0) log("appname=" + appName);

	String user=req.getRemoteUser();
	if( user==null ) {
	    // Need auth, but have no user/pass
	    if( debug>0) log("no username");
	    return HttpServletResponse.SC_UNAUTHORIZED;
	}
	String userRoles[]=null;

	String realm="default";  //ctx.getRealmName();

	Handler h=req.getWrapper();
	ServletWrapper sw=(ServletWrapper)h;
	String mappedRole=null;
	String role=null;
	for( int i=0; i< roles.length ; i++ ) {
	    role=roles[i];
	    mappedRole=sw.getSecurityRole( role );
	    if( mappedRole==null) mappedRole=role;
	    
	    if(isUserInRole(appName, mappedRole) ) {
		if( debug>0 ) log("Role match " +
				  roles[i] + " " +  mappedRole);
		return 0;
	    }
	    if( debug>0 ) log("Role match failed " +
			      roles[i] + " " + mappedRole);
	}
	
	if( debug>0  ) log("UnAuthorized " +
					role + " " + mappedRole);
 	return HttpServletResponse.SC_UNAUTHORIZED;
	// XXX check transport
    }

    
    // -------------------- Security --------------------

    /*
      J2EE stores the security information in per/thread data
      ( SecurityContext). The web server will respect web.xml -
      including constraints, roles, auth methods.

      In order to access the J2EE realm it will use those methods.
      
      XXX This is based on how the current code works. It may be a
      good idea to make authenticate return the Principal or null,
      drop getUserPrincipal, and pass the Principal to isUserInRole.
      ( this avoids per/thread data - and it's more generic )
     */
      
    private LoginContext initLoginContext(String realm) {
	for(int i=0; i < MAX_COUNT; ++i) {
	    try {
		if (debug>0) log("Getting auth for:" + i);
		LoginContext lc= new LoginContext();
		lc.setRealmName(realm);
		return lc;
	    } catch(Exception e) {
		e.printStackTrace(Log.err);
		try { 
		    Thread.sleep(SLEEP_TIME); 
		}catch(InterruptedException ie) {
		}
	    }
	}
	return null;
    }

    /** Return the current user, as set by authenticate()
     */
    public Principal getUserPrincipal() {
	SecurityContext sc=SecurityContext.getCurrent();
	if( sc==null ) return null;
	return sc.getCallerPrincipal();
    }
    
    /** Check if the current user has the specified role.
     *  The user is set in the thread data.
     *
     *  It will be called with the role specified in web.xml/role-ref
     */
    public boolean isUserInRole(String appName, String rname) {
	RoleMapper rmap = RoleMapper.getRoleMapper( appName );

	if(debug>0) log("Mapping role " + rname + " " + rmap);
	if(rmap != null) {
	    for(Enumeration e =
		    rmap.getCurrentRoles(); e.hasMoreElements();) {
		Role r = (Role) e.nextElement();
		if( debug>0) log("Try " + r.getName());
		if(r.getName().equals(rname))
		    return true;
	    }
	}
	return false;
    }

    
    // -------------------- Logging implementation --------------------

    /* Very inefficient - we do a hashtable lookup per context.
       We could use context notes to speed up if needed.
    */
    
    private synchronized void intLogRequest(String ctxPath, String uri,
					    String respString ) {   
	Log accessLog = getAccessLog(ctxPath);
	accessLog.println(localStrings.
			  getLocalString( "web.security.request.on.at", 
					  "Request for  {0} on {1} at {2} ", 
					  new Object[] {uri,
							ctxPath,
							new Date() +
							"." + respString }));
	accessLog.flush();
    }


    private synchronized void intLogError(String ctxPath, String uri,
					  String respString)
    {
	Log errorLog = getErrorLog(ctxPath);
	errorLog.println("Error serving Request " + uri +
			 " on " + ctxPath + " at " +
			 new Date() + ". " + respString);
	errorLog.flush();
    }


    private synchronized Log getLog(String contextName, String propName,
				    String defaultValue) {
	try {
	    File dir = LogUtil.getLogDirectory(contextName, LogUtil.WEB_LOG);
	    String fname = config.getProperty(propName, defaultValue);
	    File logFile =  new File(dir, fname);
	    
	    if(!logFile.exists()) {
		logFile.createNewFile();
	    }

	    FileLogWriter fwriter = new FileLogWriter(logFile, BUFFER_SIZE);
	    return Log.getLog(fwriter);
	} catch(Exception e) {
	    e.printStackTrace();
	    Log.err.println(e);
	    return Log.err;
	}
    }

    private Log getAccessLog(String contextName) {
	Log aLog = (Log) accessLogs.get(contextName);
	if(aLog == null) {
	    aLog = getLog(contextName, HTTP_ACCESS_LOG, "access.log");
	    accessLogs.put(contextName, aLog);
	}
	
	return aLog;
    }

    private Log getErrorLog(String contextName) {
	Log aLog = (Log) errorLogs.get(contextName);
	if(aLog == null) {
	    aLog = getLog(contextName, HTTP_ERROR_LOG, "error.log");
	    errorLogs.put(contextName, aLog);
	}
	
	return aLog;
    }
}

