<html><title>Tomcat Self-Test</title>
<body bgcolor="#FFFFFF">
<h1>Tomcat Self-test</h1> 

<%@ taglib uri="http://jakarta.apache.org/taglibs/tomcat_admin-1.0" 
           prefix="adm" %>
<%@ taglib uri="http://jakarta.apache.org/taglibs/ant-1.0" 
           prefix="ant" %>

This page will show the result of executing the sanity test suite. 

<%@ include file="sanity-form.jsp" %>

<% // This is an ugly hack to make the logs easily accessible.
   // Keep in mind this is just a way to jump-start testing, not a 
   // production-quality test runner.
%>
<% out.flush(); 
   if( request.getParameter("target") == null ) return;
%>

<ant:gtest />

<ant:ant>
  <ant:target param="target" />
  
  <ant:property name="ant.file" 
		location="/WEB-INF/test-tomcat.xml" 
		webApp="/test" />
  <ant:property name="gdir" 
		location="/Golden" 
		webApp="/test" />
  <ant:property name="debug"  param="debug" />
  <ant:property name="outputType" value="none"  />
  <ant:property name="port" param="port" />
  <ant:property name="http.protocol" param="server.proto" />
  <ant:property name="host" param="host" />
</ant:ant>

<% // Test completed, display the results ( outType=none means
   // Gtest doesn't generate any output ( but we have to wait untill
   // it's done ), use 'html' for "interactive" results
%>

<h1>Test <%= antProperties.getProperty("revision") %></h1>

<% // -------------------- Failures -------------------- %>
<h1>FAILED Tests</h1>

<adm:iterate name="failures" enumeration="<%= gtestTestFailures.elements() %>" 
               type="org.apache.tomcat.util.test.Matcher" >

<% // Need more tags - if, etc 
%>
<a href='<%= failures.getHttpRequest().getURI() %>'> 
<font color='red'> FAIL </font></a> ( <%=
                  failures.getHttpClient().getComment() %> )
    <%= failures.getHttpRequest().getRequestLine() %>
<br>
TEST: <%= failures.getTestDescription() %>
<br>
<b>Request: </b>
<pre>
  <%= failures.getHttpRequest().getFullRequest() %>
</pre>
<b>Comments: </b>
  <%= failures.getHttpClient().getComment() %>
<br>
<b>Message: </b>
<pre>
  <%= failures.getMessage() %>
</pre>

<% // use a tag %>
<% if( request.getParameter("debug" ) != null ) { %>
  <b>Response status: </b> 
  <%= failures.getHttpResponse().getResponseLine() %>
  <br>
  <b>Response headers: </b>
   (I'm not sure how to do embeded iterations, need JSP expert )
  <br>
  
  <b>Response body: </b>
  <pre>
  <%= failures.getHttpResponse().getResponseBody() %>
  </pre>
<% } %>  


</adm:iterate>

<% // -------------------- Success story -------------------- %>

<h1>PASSED Tests</h1>

<adm:iterate name="success" enumeration="<%= gtestTestSuccess.elements() %>" 
               type="org.apache.tomcat.util.test.HttpClient" >

<a href='<%= success.getFirstRequest().getURI() %>'> 
OK</a> ( <%= success.getComment() %> ) 
    <%= success.getFirstRequest().getRequestLine() %>
<br>
</adm:iterate>
</body>
</html>
