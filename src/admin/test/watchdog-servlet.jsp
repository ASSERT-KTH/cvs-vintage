<h1>Tomcat Self-test</h1> 

<%@ taglib uri="http://jakarta.apache.org/taglibs/tomcat_admin-1.0" 
           prefix="adm" %>

This page will show the result of executing the sanity test suite. 
You can see the context log <a href="/servlet-tests/context_log.txt">here</a>

<adm:admin ctxPath="/servlet-tests" 
	   action="setLogger" 
	   value="webapps/servlet-tests/context_log.txt" />


<adm:gtest testFile="WEB-INF/scripts/watchdog-servlet.xml" 
	   testApp="/admin" 
	   target='main'  />
