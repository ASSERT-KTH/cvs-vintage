package tadm;
import java.util.Vector;
import java.util.Enumeration;
import java.io.File;
import java.net.URL;
import javax.servlet.http.*;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;
import org.apache.tomcat.core.*;
import org.apache.tomcat.helper.RequestUtil;

/**
 * A context administration class. Contexts can be
 * viewed, added, and removed from the context manager.
 *
 */
public class TomcatAdmin extends TagSupport {
    private ContextManager cm;
    String ctxPath;
    String docBase;
    String ctxPathParam;
    String docBaseParam;
    String action;
    String host;
    PageContext pageContext;
    
    public TomcatAdmin() {}

    public int doStartTag() throws JspException {
	try {
	    HttpServletRequest req=(HttpServletRequest)pageContext.
		getRequest();
	    init(req);
	    pageContext.setAttribute("cm", cm);
	    Context ctx=null;
	    if( ctxPath==null && ctxPathParam!=null ) {
		ctxPath=req.getParameter( ctxPathParam );
	    }
	    if( docBase==null &&  docBaseParam!=null) {
		docBase=req.getParameter( docBaseParam );
	    }
	    
	    if( ctxPath != null ) {
		System.out.println("Finding " + ctxPath );
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
	    if("removeContext".equals( action ) )
		removeContext( cm , ctx);
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
    
    public void setPageContext(PageContext pctx ) {
	this.pageContext=pctx;
    }

    public void setParent( Tag parent ) {
	super.setParent( parent);
    }

    public void release() {
    }

    private void init(HttpServletRequest request) {
	Request realRequest = (Request)
	    request.getAttribute( Request.ATTRIB_REAL_REQUEST);
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

    public void setAction( String action ) {
	this.action=action;
    }

    public void setDocBase( String docBase ) {
	this.docBase=docBase;
    }

    private void removeContext( ContextManager cm, Context ctx)
	throws TomcatException
    {
	System.out.println("Removing " + ctx );
	cm.removeContext( ctx );
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
	context.setPath(path);
	context.setDocBase(docBase);

	cm.addContext(context);
	context.init();
    }
}
