<html>
<%@ taglib uri="http://jakarta.apache.org/taglibs/tomcat_admin-1.0" 
           prefix="adm" %>

<!--
  Copyright (c) 1999 The Apache Software Foundation.  All rights 
  reserved.
-->

<head>

    <title>Context remove</title>
</head>

<body bgcolor="white">

<h3>Removing <%= request.getParameter("removeContextName") %> </h3>
<!-- <%= request.getParameter("removeContextName") %> -->
<adm:admin ctxPathParam="removeContextName"
           ctxHostParam="removeHost"
           action="removeContext" />

<a href="contextList.jsp">Return to Context List</a>
</body>
</html>
