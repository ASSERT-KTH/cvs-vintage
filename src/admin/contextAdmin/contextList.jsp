<html>
<%@ page errorPage="adminError.jsp" %>
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

    <title>Context list</title>
</head>

<body bgcolor="white">

<adm:admin ctxHostParam="ContextHost"
           ctxPathParam="ContextPath"
           docBaseParam="ContextDocBase"
           action="action" />

<h3>Web applications</h3>

  <table border="0">
   <tr>
     <form method=POST action=contextsRestart.jsp>
     <td><INPUT TYPE=submit name="submit" value="Restart All contexts"></td>
     </form>
   </tr>
  </table>

  <table border="0" cellpad="2" >
  <tr bgcolor="#e1e1e1">
    <th>Host</td>
    <th>path</th>
    <th>docBase </th>
  </tr>
  
  <adm:iterate name="context" enumeration="<%= cm.getContexts() %>" 
               type="org.apache.tomcat.core.Context" >
      <tr>
      <td><%= (context.getHost() == null) ? "localhost" : context.getHost() %></td> 
      <td> <a href="ctxDetail.jsp?ctx=<%= context.getPath() %>&host=<%= (context.getHost() == null) ? "" : context.getHost() %>">
		<%= ("".equals( context.getPath() )) ? "ROOT" :  context.getPath() %>
           </a></td>
      <td> <%= context.getDocBase() %> </td>
      <td>
	<form method="POST" action="contextRemove.jsp" >
	     <INPUT TYPE=hidden name="removeContextName" 
		    value="<%= context.getPath() %>">
	     <INPUT TYPE=submit name="submit" value="Remove">
	     <INPUT TYPE=hidden name="removeHost" 
	          value="<%= (context.getHost() == null) ? "" : context.getHost() %>">
	   </form>
      </td>
      <td>
         <form method="POST" action="contextRestart.jsp">
           <INPUT TYPE=hidden name="restartHost" 
                value="<%= (context.getHost() == null) ? "" : context.getHost() %>">
          <INPUT TYPE=hidden name="restartContextName"
               value="<%= context.getPath() %>">
          <INPUT TYPE=hidden name="restartContextDocBase"
               value="<%= context.getDocBase() %>">
          <INPUT TYPE=submit name=submit value="Restart">
         </form>
       </td>
      </tr>
  </adm:iterate>
   <tr>
       <form method="POST" action="contextAdd.jsp">
       <td><INPUT type=text name="addContextHost" size=20></td>
       <td><INPUT type=text name="addContextPath" size=20></td>
       <td><INPUT type=text name="addContextDocBase" size=40>
           <INPUT type=hidden name="action" value="addContext"></td>
       <td>&nbsp;</td>
       <td><INPUT type=submit name="submit" value="Add Context"></td>
       </form>
    </tr>
       
  </table>

</body>
</html>
