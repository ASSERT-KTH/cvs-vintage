<!--
  Copyright 2001-2004 The Apache Software Foundation
 
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
 
      http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->
<h1>Tomcat Self-test</h1> 

<%@ taglib uri="http://jakarta.apache.org/taglibs/tomcat_admin-1.0" 
           prefix="adm" %>
<%@ taglib uri="http://jakarta.apache.org/taglibs/ant-1.0" 
           prefix="ant" %>

This page will show the result of executing the sanity test suite. 
You can see the context log <a href="/jsp-tests/context_log.txt">here</a>

<ant:gtest />

<adm:admin ctxPath="/jsp-tests" 
	   action="setLogger" 
	   value="webapps/jsp-tests/context_log.txt" />

<pre>
<ant:ant>
  <ant:target name="main" />
  <ant:property name="ant.file" 
		location="/WEB-INF/scripts/watchdog-jsp.xml" 
		webApp="/admin" />
  <ant:property name="wgdir" 
		location="/Golden" 
		webApp="/jsp-tests" />
  <ant:property name="debug"  param="debug" />
  <ant:property name="outputType" value="html"  />
  <ant:property name="port" param="port" />
  <ant:property name="http.protocol" param="server.proto" />
  <ant:property name="host" param="host" />
</ant:ant>
</pre>

<% // Test completed, display the results ( outType=none means
   // Gtest doesn't generate any output ( but we have to wait untill
   // it's done ), use 'html' for "interactive" results
%>

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

<b>Message: </b>
<pre>
  <%= failures.getMessage() %>
</pre>

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

</adm:iterate>

