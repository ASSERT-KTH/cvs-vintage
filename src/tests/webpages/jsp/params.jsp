<html>
<!--
  Copyright (c) 1999 The Apache Software Foundation.  All rights 
  reserved.
-->

<body bgcolor="white">
<h1> Jsp params: </h1>
<font size="4">

<% 
  javax.servlet.ServletConfig sc=getServletConfig();
  java.util.Enumeration enum=sc.getInitParameterNames();
  while( enum.hasMoreElements() ) {
     String name=(String)enum.nextElement();
%>
     Parameter <%= name %> = <%= sc.getInitParameter( name ) %>
<br>
<%
  }
%>
</font>
</body>
</html>
