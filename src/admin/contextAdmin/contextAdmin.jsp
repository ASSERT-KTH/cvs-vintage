<html>
<!--
  Copyright (c) 1999 The Apache Software Foundation.  All rights 
  reserved.
-->

<head>
    <title>Context Admin</title>
</head>

<body bgcolor="white">
<font size=5>

<%@ page import="ContextAdmin" %>
<%@ page import="java.util.Enumeration" %>
<%@ include file="contextAdmin.html" %>

<hr>

<jsp:useBean id="contextAdmin" scope="page" class="ContextAdmin" />
<jsp:setProperty name="contextAdmin" property="*" />

<%
    String param = request.getParameter("submit");

    if (param != null) {

      contextAdmin.init(request);

      if (param.equals("View All Contexts")) {
          Enumeration enum = contextAdmin.getContextNames();
          while (enum.hasMoreElements()) {
              String name = (String)enum.nextElement();
%>
              <ul>
                  <li><font color="#CC0000"><h2>CONTEXT NAME: <%= name %>
                  </h2></font>
<%
              String[] contextInfoArray = contextAdmin.getContextInfo(name);
              for (int i=0; i<contextInfoArray.length; i++) {
%>
                  <dd><dt> <%= contextInfoArray[i] %>
<%
              }
%>
              </ul>
<%
          }
      }
      else if (param.equals("Add Context")) {
%>
          <%= contextAdmin.addContext() %>
<%
          }
      else if (param.equals("Remove Context")) {
%>
          <%= contextAdmin.removeContext() %>
<%
          }
    }
    else out.println("ERROR: Null Request Parameter Value");

%>

<hr>

</body>
</font>
</html>
