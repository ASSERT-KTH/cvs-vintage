<h1>Tomcat Self-test</h1> 

<%@ taglib uri="http://jakarta.apache.org/taglibs/tomcat_admin-1.0" 
           prefix="adm" %>

This page will show the result of executing the sanity test suite. 
You can see the context log <a href="/test/context_log.txt">here</a>

<% // This is an ugly hack to make the logs easily accessible.
   // Keep in mind this is just a way to jump-start testing, not a 
   // production-quality test runner.
%>

<adm:admin ctxPath="/test" 
	   action="setLogger" 
	   value="webapps/test/context_log.txt" />

<adm:gtest testFile="WEB-INF/test-tomcat.xml" 
	   testApp="/test" 
	   target="file" />



</pre>