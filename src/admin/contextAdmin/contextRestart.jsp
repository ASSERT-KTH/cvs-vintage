<html>
<%-- can't have a session (yet) if restarting this context --%>
<%@ page session="false" %> 
<%@ taglib uri="http://jakarta.apache.org/taglibs/tomcat_admin-1.0" 
           prefix="adm" %>

<!--
  Copyright (c) 1999 The Apache Software Foundation.  All rights 
  reserved.
-->

<head>

    <title>Context restart</title>
</head>

<body bgcolor="white">

<h3>Restarting <%= ( (request.getParameter("restartHost")==null) || 
                     ("".equals( request.getParameter("restartHost") ) ) ) ? 
                      "localhost" : request.getParameter("restartHost") %>:<%= 
	            ("".equals(request.getParameter("restartContextName"))) ?
                      "ROOT" : request.getParameter("restartContextName") %></h3>

<adm:admin ctxHostParam="restartHost"
           ctxPathParam="restartContextName"
           docBaseParam="restartContextDocBase"
           action="restartContext" />

<a href="contextList.jsp">Return to Context List</a>

</body>
</html>
