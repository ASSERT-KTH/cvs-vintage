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
      </tr>
  </adm:iterate>

  </table>

  <h3>Request Interceptors</h3>
     
    <table>
     <adm:iterate name="riA" array="<%= cm.getRequestInterceptors() %>" 
               type="org.apache.tomcat.core.RequestInterceptor" >
      <tr>
	  <td><%= riA.getClass().getName() %>
	  </td>
	</tr>
      <td>
      </tr>
     </adm:iterate>
    </table>

  <h3>Context Interceptors</h3>

    <table>
     <adm:iterate name="riA" array="<%= cm.getContextInterceptors() %>" 
               type="org.apache.tomcat.core.ContextInterceptor" >
      <tr>
	  <td><%= riA.getClass().getName() %>
	  </td>
	</tr>
      <td>
      </tr>
     </adm:iterate>
    </table>
       
</body>
</html>
