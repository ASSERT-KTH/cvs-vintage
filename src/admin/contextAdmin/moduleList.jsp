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

  <h3>Interceptors</h3>
     
    <table>
     <adm:iterate name="riA" 
                  array="<%= cm.getContainer().getInterceptors() %>" 
                  type="org.apache.tomcat.core.BaseInterceptor" >
      <tr>
	  <td><%= riA.getClass().getName() %></td>
      </tr>
     </adm:iterate>
    </table>

</body>
</html>
