<html><title>Tomcat Self-Test</title>
<body bgcolor="#FFFFFF">
<h1>Tomcat Self-test</h1> 
<%@ page import="java.util.*" %>
<%@ taglib uri="http://jakarta.apache.org/taglibs/tomcat_admin-1.0" 
           prefix="adm" %>
<%@ taglib uri="http://jakarta.apache.org/taglibs/ant-1.0" 
           prefix="ant" %>

This page will show the result of executing the sanity test suite. 

<%! long t ; %>

<%@ include file="sanity-form.jsp" %>

<% // This is an ugly hack to make the logs easily accessible.
   // Keep in mind this is just a way to jump-start testing, not a 
   // production-quality test runner.
%>
<% out.flush(); 
   if( request.getParameter("target") == null ) return;

   String port = request.getParameter("port");
   String colonPort=null;
   String notStandAlone=null;
   try {
       colonPort = ":" + port;
       if ( Integer.parseInt(port) == 80 ) {
           colonPort = "";
           notStandAlone="Not standalone";
       }
   } catch (Exception e) {
      out.println("ERROR: Invalid port number!");
      return;
   }
   String dirStatus="301";
   String redirStatus="301";
   if (!staticServer.equals("Tomcat")) {
      dirStatus="200";
      if (!staticServer.equals("Apache"))
         redirStatus="302";
      else
         redirStatus="301";
   }
%>

<ant:gtest />
<% t = System.currentTimeMillis(); %>
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
  <ant:property name="colonPort" value="<%= colonPort %>" />
  <ant:property name="http.protocol" param="server.proto" />
  <ant:property name="host" param="host" />
  <ant:property name="not.standalone" value="<%= notStandAlone %>" />
  <ant:property name="dir.status" value="<%= dirStatus %>" />
  <ant:property name="redir.status" value="<%= redirStatus %>" />
<% if ("Tomcat".equals(webServer)) { %>
  <ant:property name="tomcat.server" value="Tomcat" />
<% } else if ("Apache".equals(webServer)) { %>
  <ant:property name="apache.server" value="Apache" />
<% } else if ("IIS".equals(webServer)) { %>
  <ant:property name="iis.server" value="IIS" />
<% } else if ("Netscape".equals(webServer)) { %>
  <ant:property name="netscape.server" value="Netscape" />
<% } %>
<% if ("Tomcat".equals(staticServer)) { %>
  <ant:property name="tomcat.static" value="Tomcat" />
<% } else if ("Apache".equals(staticServer)) { %>
  <ant:property name="apache.static" value="Apache" />
<% } else if ("IIS".equals(staticServer)) { %>
  <ant:property name="iis.static" value="IIS" />
<% } else if ("Netscape".equals(staticServer)) { %>
  <ant:property name="netscape.static" value="Netscape" />
<% } %>
</ant:ant>
<% // Test completed, display the results ( outType=none means
   // Gtest doesn't generate any output ( but we have to wait untill
   // it's done ), use 'html' for "interactive" results
%>

<h1>Test <%= antProperties.getProperty("revision") %></h1>

Executed in <%= System.currentTimeMillis() - t %> Milliseconds.<br>

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
