<html>
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
