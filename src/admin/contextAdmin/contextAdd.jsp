<html>
<%@ taglib uri="http://jakarta.apache.org/taglibs/tomcat_admin-1.0" 
           prefix="adm" %>

<!--
  Copyright (c) 1999 The Apache Software Foundation.  All rights 
  reserved.
-->

<head>

    <title>Context Add</title>
</head>

<body bgcolor="white">

<h3>Adding <%= ("".equals(request.getParameter("addContextHost")) ? 
	"localhost" : request.getParameter("addContextHost") ) 
	%>:<%= request.getParameter("addContextPath") %> </h3>

<adm:admin ctxPathParam="addContextPath"
	   ctxHostParam="addContextHost"
           docBaseParam="addContextDocBase"
           action="addContext" />
<a href="contextList.jsp">Return to Context List</a>
</body>
</html>
