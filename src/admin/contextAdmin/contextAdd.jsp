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

<h3>Adding <%= request.getParameter("addContextPath") %> </h3>

<adm:admin ctxPathParam="addContextPath"
           docBaseParam="addContextDocBase"
           action="addContext" />

</body>
</html>
