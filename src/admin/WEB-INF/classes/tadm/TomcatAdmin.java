package tadm;
import java.util.Vector;
import java.util.Enumeration;
import java.io.*;
import java.net.URL;
import javax.servlet.http.*;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;
import org.apache.tomcat.core.*;
import org.apache.tomcat.util.log.*;
//import org.apache.tomcat.util.qlog.*;

/**
 * A context administration class. Contexts can be
 * viewed, added, and removed from the context manager.
 *
 */
public class TomcatAdmin extends TagSupport {
    private ContextManager cm;
    String ctxHost;
    String ctxPath;
    String docBase;
    String ctxHostParam;
    String ctxPathParam;
    String docBaseParam;
    String action;
    String host;
    String value;
    //    PageContext pageContext;
    
    public TomcatAdmin() {}

    public int doStartTag() throws JspException {
	try {
	    HttpServletRequest req=(HttpServletRequest)pageContext.
		getRequest();
	    init(req);
	    if( cm==null ) {
		return SKIP_BODY;
	    }
	    pageContext.setAttribute("cm", cm);
	    Context ctx=null;
	    if( ctxHost == null && ctxHostParam != null) {
		ctxHost=req.getParameter( ctxHostParam );
		if( "".equals(ctxHost) )
		    ctxHost=null;
	    }
	    if( ctxPath==null && ctxPathParam!=null ) {
		ctxPath=req.getParameter( ctxPathParam );
	    }
	    if( docBase==null &&  docBaseParam!=null) {
		docBase=req.getParameter( docBaseParam );
	    }

	    boolean found = false;
	    if( ctxPath != null ) {
		System.out.println("Finding host: "+ ctxHost + ",path=" + ctxPath );
		if( ! ctxPath.startsWith("/") )
		    ctxPath = "/" + ctxPath;
		if( ctxPath.equals("/") )
		    ctxPath="";
		Enumeration en=cm.getContexts();
		while( en.hasMoreElements() ) {
		    ctx=(Context)en.nextElement();
		    // XXX virtual host
		    if( ctxPath.equals( ctx.getPath())) {
			if( (ctxHost == null && ctx.getHost() == null ) ||
			    (ctxHost != null && ctxHost.equals(ctx.getHost()))){
			    found=true;
			    pageContext.setAttribute("ctx", ctx);
			    System.out.println("Found " + ctx );
			    break;
			} 
		    }
		}
	    }
	    if("removeContext".equals( action ) && found)
		removeContext( cm , ctx);
	    if("setLogger".equals( action ) )
		setLogFile(  ctx, value );
	    if("addContext".equals( action ) )
		addContext( cm, ctxHost, ctxPath, docBase );
	    if("restartContext".equals(action) && found) {
		restartContext(cm, ctx, req);
	    }
	} catch (Exception ex ) {
	    ex.printStackTrace();
	}
	return EVAL_BODY_INCLUDE;
    }

    public int doEndTag() throws JspException {
	return EVAL_PAGE;
    }
    
    public void setParent( Tag parent ) {
	super.setParent( parent);
    }

    public void release() {
    }

    private void init(HttpServletRequest request) {
	Request realRequest = (Request)
	    request.getAttribute( Request.ATTRIB_REAL_REQUEST);
	if( realRequest==null )
	    pageContext.getServletContext().log("Untrusted Application");
	else
	    cm = realRequest.getContext().getContextManager();
    }

    public ContextManager getContextManager(HttpServletRequest request ) {
	if( cm==null ) init( request );
        return cm;
    }

    public void setCtxPath( String ctx ) {
	ctxPath=ctx;
    }

    public void setCtxPathParam( String ctx ) {
	ctxPathParam=ctx;
    }
    
    public void setDocBaseParam( String ctx ) {
	docBaseParam=ctx;
    }

    public void setCtxHost( String host ) {
	this.host=host;
    }

    public void setCtxHostParam( String hostP ) {
	this.ctxHostParam = hostP;
    }
    public void setAction( String action ) {
	this.action=action;
    }

    public void setDocBase( String docBase ) {
	this.docBase=docBase;
    }

    public void setValue( String s ) {
	this.value=s;
    }
    
    private void removeContext( ContextManager cm, Context ctx)
	throws TomcatException
    {
	System.out.println("Removing " + ctx );
	cm.removeContext( ctx );
    }

    private void restartContext( ContextManager cm, Context ctx,HttpServletRequest req ) 
    throws TomcatException {
	Request request = (Request)
	    req.getAttribute( Request.ATTRIB_REAL_REQUEST);
	if( request == null ) {
	    throw new TomcatException("Untrusted Web-App");
	}
	synchronized(ctx) {
	    if(ctx.getState() == Context.STATE_NEW)
		return ; // Already reloaded.
	    Vector sI=new Vector();  // saved local interceptors
	    BaseInterceptor[] eI;    // all exisiting interceptors
	    
	    // save the ones with the same context, they are local
	    eI=ctx.getContainer().getInterceptors();
	    for(int i=0; i < eI.length ; i++)
		if(ctx == eI[i].getContext()) sI.addElement(eI[i]);
		    
	    Enumeration e;
	 
	    Context ctx1=cm.createContext();
	    ctx1.setContextManager( cm );
	    ctx1.setPath(ctx.getPath());
	    ctx1.setDocBase(ctx.getDocBase());
	    ctx1.setReloadable( ctx.getReloadable());
	    ctx1.setDebug( ctx.getDebug());
	    ctx1.setHost( ctx.getHost());
	    ctx1.setTrusted( ctx.isTrusted());
	    e=ctx.getHostAliases();
	    while( e.hasMoreElements())
		ctx1.addHostAlias( (String)e.nextElement());
	    
	    BaseInterceptor ri[] = 
		cm.getContainer().getInterceptors(Container.H_copyContext);
	    int i;
	    for( i=0; i < ri.length; i++) {
		ri[i].copyContext(request, ctx, ctx1);
	    }
	    cm.removeContext( ctx );
		    
	    cm.addContext( ctx1 );
	    
	    // put back saved local interceptors
	    e=sI.elements();
	    while(e.hasMoreElements()){
		BaseInterceptor savedI=(BaseInterceptor)e.nextElement();
		
		ctx1.addInterceptor(savedI);
		savedI.setContext(ctx1);
		savedI.reload(request,ctx1);
	    }

	    ctx1.init();
	}
    }

    private void setLogFile( Context ctx, String dest )
	throws TomcatException
    {
// 	try {
// 	    QueueLogger logger=new QueueLogger();
// 	    System.out.println("Setting logger " + dest );
// 	    logger.setPath( dest );
// 	    logger.open();
// 	    LogManager logManager=(LogManager)ctx.getContextManager().
// 		getNote("tc.LogManager");
	    
// 	    logManager.addChannel("temp.log", logger );
// 	    Log log=Log.getLog( "temp.log", ctx );
// 	    ctx.setLog( log );
// 	    ctx.setServletLog( log );
// 	} catch( Exception ex ) {
// 	    ex.printStackTrace();
// 	}
    }

    private void addContext( ContextManager cm, String host, String path,
			     String docBase)
	throws TomcatException
    {
	if( ! docBase.startsWith("/") ) {
	    docBase=cm.getHome() + "/" + docBase;
	}
	System.out.println("Adding " + path + " " + docBase);
	Context context = new Context();
	context.setContextManager(cm);
	context.setHost(host);
	context.setPath(path);
	context.setDocBase(docBase);

	cm.addContext(context);
	context.init();
    }
}
