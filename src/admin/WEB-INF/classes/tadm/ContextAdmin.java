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

/**
 * A context administration class. Contexts can be
 * viewed, added, and removed from the context manager.
 *
 */
public class ContextAdmin extends TagSupport {
    private ContextManager cm;
    String ctxPath;
    String docBase;
    String ctxPathParam;
    String docBaseParam;
    String action;
    String host;
    String value;
    //    PageContext pageContext;
    
    public ContextAdmin() {}

    public int doStartTag() throws JspException {
	try {
	    HttpServletRequest req=(HttpServletRequest)pageContext.
		getRequest();

	    cm=(ContextManager)pageContext.getAttribute("cm");
	    if( cm==null )
		throw new JspException( "Can't find context manager" );

	    Context ctx=null;
	    if( ctxPath==null && ctxPathParam!=null ) {
		ctxPath=req.getParameter( ctxPathParam );
	    }

	    if( docBase==null &&  docBaseParam!=null) {
		docBase=req.getParameter( docBaseParam );
	    }
	    
	    if( ctxPath != null ) {
		if( debug>0 ) log("Finding " + ctxPath );
		Enumeration en=cm.getContexts();
		while( en.hasMoreElements() ) {
		    ctx=(Context)en.nextElement();
		    // XXX virtual host
		    if( ctxPath.equals( ctx.getPath())) {
			pageContext.setAttribute("ctx", ctx);
			break;
		    }
		}
	    }
	    if( ctx==null ) {
		throw new JspException("Can't find context " + ctxPath );
	    }
	    if("removeContext".equals( action ) )
		removeContext( cm , ctx);
	    if("setLogger".equals( action ) )
		setLogFile(  ctx, value );
	    if("addContext".equals( action ) )
		addContext( cm, host, ctxPath, docBase );
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

    // -------------------- Properties --------------------
    
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
	if( debug > 0 ) log("Removing " + ctx );
	cm.removeContext( ctx );
    }

    private void setLogFile( Context ctx, String dest )
	throws TomcatException
    {
// 	try {
// 	    QueueLogger logger=new QueueLogger();
// 	    if( debug > 0 ) log ("Setting logger " + dest );
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
	if( debug > 0 )
	    log("Adding " + path + " " + docBase);
	Context context = new Context();
	context.setContextManager(cm);
	context.setPath(path);
	context.setDocBase(docBase);

	cm.addContext(context);
	context.init();
    }

    // --------------------
    private static int debug=0;
    
    private void log(String s ) {
	System.out.println(s );
    }
}
