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
 * Child tag to add ant targets to Ant taglib.
 * 
 */
public class AntTarget extends TagSupport {
    
    public AntTarget() {}

    public int doStartTag() throws JspException {
	Tag parent=getParent();
	if( parent == null  )
	    throw new JspException( "AntTarget used as top-level tag,"+
				    "must be inside <ant>");
	if( ! (parent instanceof AntTag ))
	    throw new JspException( "AntTarget must be child of <ant>");

	AntTag antTag=(AntTag)parent;
	if( name == null ) {
	    String names[]=pageContext.getRequest().
		getParameterValues( param );
	    for( int i=0; i<names.length; i++ ) 
		antTag.addTarget( names[i]);
	} else {
	    antTag.addTarget( name );
	}
	return SKIP_BODY;
    }

    //-------------------- Properties --------------------
    String name;
    String param;

    public void setName( String s ) {
	name=s;
    }
    public void setParam( String s ) {
	param=s;
    }
}
