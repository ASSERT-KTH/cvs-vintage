package tadm;
import java.util.*;
import java.io.*;
import java.net.URL;
import javax.servlet.http.*;
import javax.servlet.*;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;

import org.apache.tools.ant.*;
import org.apache.tomcat.util.test.*;

/**
 * This tag will make available various properties needed to access
 * GTest static variables.
 */
public class GTestTag extends TagSupport {
    
    public GTestTag() {}

    public int doStartTag() throws JspException {
	try {
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

	    	    
	    // reset test repositories
	    GTest.resetGTest();
	    GTest.setDefaultWriter( pageContext.getResponse().getWriter() );

	} catch (Exception ex ) {
	    ex.printStackTrace();
	}
	return SKIP_BODY;
    }
}
