<html>
<%@ taglib uri="http://jakarta.apache.org/taglibs/tomcat_admin-1.0" 
           prefix="adm" %>

<!--
  Copyright (c) 1999 The Apache Software Foundation.  All rights 
  reserved.
-->

<head>

    <title>Context list</title>
</head>

<body bgcolor="white">

<adm:admin/>

<h3>Web applications</h3>

  <table border="0" cellpad="2" >
  <tr bgcolor="#e1e1e1">
    <th>path</th>
    <th>docBase </th>
  </tr>
  
  <adm:iterate name="context" enumeration="<%= cm.getContexts() %>" 
               type="org.apache.tomcat.core.Context" >
      <tr>
      <td> <a href="ctxDetail.jsp?ctx=<%= context.getPath() %>"> 
		<%= ("".equals( context.getPath() )) ? "ROOT" :  context.getPath() %>
           </a></td>
      <td> <%= context.getDocBase() %> </td>
      <td>
	<a href="contextRemove.jsp?removeContextName=<%= context.getPath() %>">
            remove
	</a>
      </td>
      <td>
	<form method="POST" action="contextRemove.jsp" >
	     <INPUT TYPE=hidden name="removeContextName" 
		    value="<%= context.getPath() %>">
	     <INPUT TYPE=submit name="submit" value="Remove">
	   </form>
      </td>
      </tr>
  </adm:iterate>

  </table>

</body>
</html>
