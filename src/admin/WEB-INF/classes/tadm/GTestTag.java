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
 * This tag will run a GTest-based test suite.
 * 
 */
public class GTestTag extends TagSupport {
    PageContext pageContext;
    String revision;
    
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

	    pageContext.setAttribute("gtestTestRevision",
				     revision);
	    pageContext.setAttribute("gtestTestResults",
				     GTest.getTestResults());
	    pageContext.setAttribute("gtestTestFailures",
				     GTest.getTestFailures());
	    pageContext.setAttribute("gtestTestSuccess",
				     GTest.getTestSuccess());
	    pageContext.setAttribute("gtestTestProperties",
				     GTest.getTestProperties());
	    pageContext.setAttribute("gtestHttpClients",
				     HttpClient.getHttpClients());

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
    String debug;
    String outputType="html";

    /** Set the name of the test.xml, relative to the base dir.
     *  For example, /WEB-INF/test-tomcat.xml
     */
    public void setTestFile( String s ) {
	testFileName=s;
    }

    /** Set the target - a subset of tests to be run
     */
    public void setTarget( String s ) {
	//	System.out.println("Setting target " + s );
	target=s;
    }

    /** The application containing the test file
     *  ( if not set assume the test file is local to /admin app.
     */
    public void setTestApp( String s ) {
	testApp=s;
    }

    public void setDebug( String s ) {
	debug=s;
    }

    public void setOutputType( String s ) {
	outputType=s;
    }
    
    // -------------------- Implementation methods --------------------
    
    private void runTest( String base) throws IOException {
	PrintWriter out=pageContext.getResponse().getWriter();
	try {
	    out.flush();
	    // 	  out.println("Running test " + base + " " + testFileName + " "
	    // 			       + target + "</br>" );
	    File testFile=new File( base + testFileName);


	    // reset test repositories
	    GTest.getTestResults().setSize(0);
	    GTest.getTestFailures().setSize(0);
	    GTest.getTestSuccess().setSize(0);
	    GTest.getTestProperties().clear();
	    HttpClient.getHttpClients().clear();
	    
	    // 	    // old task
	    // 	    org.apache.tomcat.task.GTest.setDefaultWriter( out );
	    // 	    org.apache.tomcat.task.GTest.setHtmlMode( true );
	    // new one
	    GTest.setDefaultWriter(out);
	    GTest.setDefaultOutput(outputType); // external formatting
	    if(debug!=null)
		GTest.setDefaultDebug(Integer.valueOf( debug ).intValue());
	    
	    Project project=new Project();
	    
	    AntServletLogger log=new AntServletLogger();
	    log.setWriter( out );
	    project.addBuildListener( log );
	    
	    project.init();
	    project.setUserProperty( "ant.file", testFile.toString());
	    // XXX
	    project.setUserProperty( "gdir", base + "/Golden");
	    project.setUserProperty( "wgdir", base + "/Golden");
	    
	    ProjectHelper.configureProject( project, testFile );

	    // try to get revision if present
	    revision = project.getProperty( "revision" );
	    if (revision  == null)
		revision  = "Revision: Unknown";
	    else {
		// clean up a little in case CVS $Revision: 1.7 $ used
		revision  = revision .replace('$',' ');
		revision .trim();
	    }
	    
	    Vector targets=new Vector();
	    if( target==null ) target="client";
	    
	    targets.addElement( target );
	    project.executeTargets( targets );
	    
	} catch( Exception ex ) {
	    ex.printStackTrace(out);
	    if( ex instanceof BuildException ) {
		Throwable ex1=((BuildException)ex).getException();
		out.println("Root cause: " );
		if( ex1!=null)
		    ex1.printStackTrace(out);
	    }
	}
    }
}
