package tadm;
import java.util.Vector;
import java.util.Enumeration;
import java.io.*;
import java.net.URL;
import javax.servlet.http.*;
import javax.servlet.*;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;

import org.apache.tools.ant.*;
import org.apache.tomcat.util.test.*;

/**
 * Child tag to pass ant properties to Ant tag
 * 
 */
public class AntProperty extends TagSupport {
    
    public AntProperty() {}

    public int doStartTag() throws JspException {
	Tag parent=getParent();
	if( parent == null  )
	    throw new JspException( "AntProperty used as top-level tag,"+
				    "must be inside <ant>");
	if( ! (parent instanceof AntTag))
	    throw new JspException( "AntProperty must be child of <ant>");

	AntTag antTag=(AntTag)parent;

	if( value==null && location!= null ) {
	    // the property will be set with the absolute path
	    // of the "location", relative to this or another
	    // (accessible) web application
	    HttpServletRequest req=(HttpServletRequest)pageContext.
		getRequest();
	    ServletContext thisCtx=pageContext.getServletConfig().
		getServletContext();
	    ServletContext targetCtx=(webapp==null) ? thisCtx:
		thisCtx.getContext( webapp );
	    value=targetCtx.getRealPath(location);
	}

	
	if( value == null ) {
	    // if param is used, try it first ( param!= name )
	    if( param != null ) {
		value=pageContext.getRequest().getParameter( param );
	    } else {
		value=pageContext.getRequest().getParameter( name );
	    }
	}

	if( value != null )
	    antTag.setProperty( name, value );

   // reset value to default null
   value = null;

	return SKIP_BODY;
    }

    //-------------------- Properties --------------------
    String name;
    String value;
    String param;

    public void setName( String s ) {
	name=s;
    }

    public void setParam( String s ) {
	param=s;
    }

    public void setValue( String s ) {
	value=s;
    }

    // -------------------- Special properties --------------------
    String location;
    String webapp;

    public void setLocation( String s ) {
	location=s;
    }

    /** Set the property with the "base" of the web application
     */
    public void setWebApp( String s ) {
	webapp=s;
    }


    // -------------------- From ant --------------------
    
}
