<h1>Tomcat Self-test</h1> 

<%@ taglib uri="http://jakarta.apache.org/taglibs/tomcat_admin-1.0" 
           prefix="adm" %>
<%@ taglib uri="http://jakarta.apache.org/taglibs/ant-1.0" 
           prefix="ant" %>

This page will show the result of executing the sanity test suite. 
You can see the context log <a href="/servlet-tests/context_log.txt">here</a>

<adm:admin ctxPath="/servlet-tests" 
	   action="setLogger" 
	   value="webapps/servlet-tests/context_log.txt" />

<ant:gtest />

<pre>
<ant:ant>
  <ant:target name="main" />
  <ant:property name="ant.file" 
		location="/WEB-INF/scripts/watchdog-servlet.xml" 
		webApp="/admin" />
  <ant:property name="debug"  param="debug" />
  <ant:property name="outputType" value="html"  />
  <ant:property name="port" param="port" />
  <ant:property name="http.protocol" param="server.proto" />
  <ant:property name="host" param="host" />
</ant:ant>
</pre>
