/*
 *  Copyright 2001-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package tadm;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.tomcat.util.test.GTest;
import org.apache.tomcat.util.test.HttpClient;

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
