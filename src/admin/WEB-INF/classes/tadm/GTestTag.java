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

/**
 * This tag will run a GTest-based test suite.
 * 
 */
public class GTestTag extends TagSupport {
    PageContext pageContext;
    
    public GTestTag() {}

    public int doStartTag() throws JspException {
	try {
	    HttpServletRequest req=(HttpServletRequest)pageContext.
		getRequest();

	    ServletContext thisCtx=pageContext.getServletConfig().
		getServletContext();

	    // the admin can get other contexts, we are trusted
	    ServletContext targetCtx=(testApp==null) ? thisCtx:
		thisCtx.getContext( testApp );
							    
	    String base=targetCtx.getRealPath("/");
	    
	    runTest( base );
	    //pageContext.setAttribute("cm", cm);

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

    //-------------------- Properties --------------------
    String testFileName;
    String target;
    String testApp;
    
    /** Set the name of the test.xml, relative to the base dir.
     *  For example, /WEB-INF/test-tomcat.xml
     */
    public void setTestFile( String s ) {
	testFileName=s;
    }

    /** Set the target - a subset of tests to be run
     */
    public void setTarget( String s ) {
	target=s;
    }

    /** The application containing the test file
     *  ( if not set assume the test file is local to /admin app.
     */
    public void setTestApp( String s ) {
	testApp=s;
    }
    
    // -------------------- Implementation methods --------------------
    
    private void runTest( String base) {
	try {
	    File testFile=new File( base + testFileName);
	    
	    Project project=new Project();
	    
	    AntServletLogger log=new AntServletLogger();
	    log.setWriter( pageContext.getResponse().getWriter());
	    project.addBuildListener( log );
	    
	    project.init();
	    project.setUserProperty( "ant.file", testFile.toString());
	    // XXX
	    project.setUserProperty( "gdir", base + "/Golden");
	    
	    ProjectHelper.configureProject( project, testFile );
	    
	    Vector targets=new Vector();
	    if( target==null ) target="client";
	    
	    targets.addElement( target );
	    project.executeTargets( targets );
	    
	} catch( Exception ex ) {
	    ex.printStackTrace();
	}
    }
}
