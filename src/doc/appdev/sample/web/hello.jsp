<html>
<head>
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
<title>Sample Application JSP Page</title>
</head>
<body bgcolor=white>

<table border="0">
<tr>
<td align=center>
<img src="images/tomcat.gif">
</td>
<td>
<h1>Sample Application JSP Page</h1>
This is the output of a JSP page that is part of the Hello, World
application.  It displays several useful values from the request
we are currently processing.
</td>
</tr>
</table>

<table border="0" border="100%">
<tr>
  <th align="right">Context Path:</th>
  <td align="left"><%= request.getContextPath() %></td>
</tr>
<tr>
  <th align="right">Path Information:</th>
  <td align="left"><%= request.getPathInfo() %></td>
</tr>
<tr>
  <th align="right">Query String:</th>
  <td align="left"><%= request.getQueryString() %></td>
</tr>
<tr>
  <th align="right">Request Method:</th>
  <td align="left"><%= request.getMethod() %></td>
</tr>
<tr>
  <th align="right">Servlet Path:</th>
  <td align="left"><%= request.getServletPath() %></td>
</tr>
</table>
</body>
</html>
