<html><title>Tomcat Self-Test</title>
<body bgcolor="#FFFFFF">
<h1>Tomcat Self-test</h1> 

<%@ taglib uri="http://jakarta.apache.org/taglibs/tomcat_admin-1.0" 
           prefix="adm" %>

This page will show the result of executing the sanity test suite. 
You can see the context log <a href="/test/context_log.txt">here</a>

<form method="GET" action="test.jsp" >
Target:
<select name="target" > 
  <option>new-style</option>
  <option>file-tomcat</option>
  <option>dispatch-tomcat</option>
  <option>get-tomcat</option>
  <option>requestMap</option>
  <option>post</option>
  <option>jsp-tomcat</option>
  <option>wrong_request</option>
  <option>unavailable</option>
  <option>restricted</option>
  <option selected>client</option>
</select>
<br>

Debug: <input type="checkbox" name="debug" value="10"><br>
Port: <input type="input" name="port" value="<%= request.getServerPort() %>">
<br>
Host: <input type="input" name="host" value="<%= request.getServerName() %>">
<br>
Expected protocol: <input type="input" name="server.proto" 
			  value="<%= request.getProtocol() %>">
 ( use when testing Apache - tomcat3.x returns HTTP/1.0 ) <br>
<input type="submit">
</form>

<% // This is an ugly hack to make the logs easily accessible.
   // Keep in mind this is just a way to jump-start testing, not a 
   // production-quality test runner.
%>
<!-- trozo  -->
<% out.flush(); 
   if( request.getParameter("target") == null ) return;
%>
<!-- trozo  -->
<adm:admin ctxPath="/test" 
	   action="setLogger" 
	   value="webapps/test/context_log.txt" />
<!-- trozo 1 -->
<adm:gtest testFile="WEB-INF/test-tomcat.xml" 
	   testApp="/test" 
	   target='<%= request.getParameter("target") %>' 
           debug='<%= request.getParameter("debug") %>' 
           outputType='none' />
<!-- trozo 1 -->
<% // Test completed, display the results ( outType=none means
   // Gtest doesn't generate any output ( but we have to wait untill
   // it's done ), use 'html' for "interactive" results
%>

<h1>Test <%= gtestTestRevision %></h1>

<% // -------------------- Failures -------------------- %>
<h1>FAILED Tests</h1>

<adm:iterate name="failures" enumeration="<%= gtestTestFailures.elements() %>" 
               type="org.apache.tomcat.util.test.GTest" >
<% // Need more tags - if, etc 
%>
<a href='<%= failures.getHttpClient().getURI() %>'> 
<font color='red'> FAIL </font></a> ( <%= failures.getDescription() %> )
    <%= failures.getHttpClient().getRequestLine() %>
<br>
TEST: <%= failures.getMatcher().getTestDescription() %>
<br>
<b>Request: </b>
<pre>
  <%= failures.getHttpClient().getFullRequest() %>
</pre>
<b>Comments: </b>
  <%= failures.getComment() %>
<br>
<b>Message: </b>
<pre>
  <%= failures.getMatcher().getMessage() %>
</pre>

<% // use a tag %>
<% if( request.getParameter("debug" ) != null ) { %>
  <b>Response status: </b> 
  <%= failures.getHttpClient().getResponse().getResponseLine() %>
  <br>
  <b>Response headers: </b>
   (I'm not sure how to do embeded iterations, need JSP expert )
  <br>
  
  <b>Response body: </b>
  <pre>
  <%= failures.getHttpClient().getResponse().getResponseBody() %>
  </pre>
<% } %>  


</adm:iterate>

<% // -------------------- Success story -------------------- %>

<h1>PASSED Tests</h1>

<adm:iterate name="success" enumeration="<%= gtestTestSuccess.elements() %>" 
               type="org.apache.tomcat.util.test.GTest" >

<a href='<%= success.getHttpClient().getURI() %>'> 
OK</a> ( <%= success.getDescription() %> ) 
    <%= success.getHttpClient().getRequestLine() %>
<br>
TEST: <%= success.getMatcher().getTestDescription() %>
<br>
</adm:iterate>
</body>
</html>
