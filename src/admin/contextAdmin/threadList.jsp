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

  <h3>Threads</h3>
     
    <table>
      <adm:iterate name="th" 
                  array="<%= tadm.ThreadAdmin.findThreads() %>" 
                  type="java.lang.Thread" >
      <tr>
	  <td><%= th.getName() %></td>
	  <td><%= th.getThreadGroup().getName() %></td>
      </tr>
     </adm:iterate>
    </table>

</body>
</html>
