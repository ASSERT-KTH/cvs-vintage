<html>
<%@ taglib uri="http://jakarta.apache.org/taglibs/tomcat_admin-1.0" 
           prefix="adm" %>
<%@ page buffer="none" %>
<!--
  Copyright (c) 1999 The Apache Software Foundation.  All rights 
  reserved.
-->

<head>

    <title>Context list</title>
</head>

<body bgcolor="white">

<adm:admin/>

<h2>Prepare to restart</h2>

<%
try {
  cm.stop(); // all contexts stopped
  cm.shutdown(); // all contexts removed
  cm.init();
  cm.start();
} catch(Exception ex) {
  ex.printStackTrace();
}
 System.out.println("Done restarting ");
%>

<h1>Server restarted </h1>

</body>
</html>
