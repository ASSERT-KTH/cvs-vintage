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
public class ModuleAdmin extends TagSupport {
    private ContextManager cm;

    String var;
    String moduleName;
    
    public ModuleAdmin() {}

    /** Will set the "module" attribute, with the instance of the
     *  named interceptor. If "ctx" attribute is set, look in that
     *  context
     */
    public int doStartTag() throws JspException {
	try {
	    cm=(ContextManager)pageContext.getAttribute("cm");
	    if( cm==null )
		throw new JspException( "Can't find context manager" );

	    Context ctx=(Context)pageContext.getAttribute("ctx");

	    Container ct=(ctx==null)? cm.getContainer():ctx.getContainer();
	    BaseInterceptor bi[]=ct.getInterceptors();
	    BaseInterceptor found=null;
	    for( int i=0; i<bi.length; i++ ) {
		String cn=bi[i].getClass().getName();
		if( cn.equals( moduleName )) {
		    found=bi[i];
		    pageContext.setAttribute( var,bi[i],
					      PageContext.PAGE_SCOPE );
		    break;
		}
	    }
	    if( found==null )
		throw new JspException("Can't find module " + moduleName +
				       " in " + ctx );
	} catch (Exception ex ) {
	    ex.printStackTrace();
	}
	return EVAL_BODY_INCLUDE;
    }

    // -------------------- Properties --------------------
    
    public void setVar(String m) {
	var=m;
    }

    public void setType( String m ) {
	moduleName=m;
    }

    // --------------------
    private static int debug=0;
    
    private void log(String s ) {
	System.out.println(s );
    }
}
