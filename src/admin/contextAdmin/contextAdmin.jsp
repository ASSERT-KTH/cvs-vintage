<html>
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

      if (param.equals("Add Context")) {
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
