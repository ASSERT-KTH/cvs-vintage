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
