<html>
<%@ page import="java.util.Enumeration, org.apache.tomcat.core.Context" %>
<%@ taglib uri="http://jakarta.apache.org/taglibs/tomcat_admin-1.0" 
           prefix="adm" %>
<!--
  Copyright (c) 1999 The Apache Software Foundation.  All rights 
  reserved.
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
