<html>
<%@ page import="java.util.Enumeration, org.apache.tomcat.core.Context" %>
<%@ taglib uri="http://jakarta.apache.org/taglibs/tomcat_admin-1.0" 
           prefix="adm" %>
<!--
  Copyright 1999-2004 The Apache Software Foundation
 
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

<head>
    <title>Restart Contexts</title>
</head>

<body bgcolor="white">


<h2>Prepare to restart all contexts</h2>
<adm:admin />
<%
  Enumeration en=cm.getContexts();
  while( en.hasMoreElements() ) {
    ctx=(Context)en.nextElement(); %>
    <jsp:include page="contextRestart.jsp" flush="true">
      <jsp:param name="restartHost" value="<%= 
	ctx.getHost() == null ? \"\" : ctx.getHost() %>" />
      <jsp:param name="restartContextName" value="<%= ctx.getPath() %>" />
      <jsp:param name="restartContextDocBase" value="<%= ctx.getDocBase() %>" />
      <jsp:param name="action" value="restartContext" />
   </jsp:include>
<% } 

 System.out.println("TomcatWebAdmin : All contexts restarted ");
%>

<h1>Contexts restarted </h1>

<a href="contextList.jsp">Return to Context List</a>

</body>
</html>
