<html>
<%@ taglib uri="http://jakarta.apache.org/taglibs/tomcat_admin-1.0" 
           prefix="adm" %>
<head>
    <title>Session info</title>
</head>

<body bgcolor="white">

<adm:admin ctxPathParam="ctx" ctxHostParams="host" />
<adm:moduleAdmin var="store" 
		 type="org.apache.tomcat.modules.session.SimpleSessionStore" />

  <h3>Session store info</h3>

  <ul>
    <li>Context: <%= ctx %>
    <li>Default timeout: <%= ctx.getSessionTimeOut() %>
    <li>Active sessions: <%= store.getSessionCount(ctx) %>
    <li>Recycled sessions: <%= store.getRecycledCount(ctx) %>
  <ul>

    <table border>
      <tr>
	  <th>Id</th>
	  <th>Attrs</th>
	  <th>St</th>
	  <th>Creation</th>
	  <th>Access</th>
	  <th>Exp</th>
	  <th>New</th>
      </tr>
     <adm:iterate name="sS" 
                  enumeration="<%= store.getSessions(ctx) %>" 
                  type="org.apache.tomcat.core.ServerSession" >
      <tr>
	  <td><%= sS.getId().toString() %></td>
	  <td><%= sS.getAttributeCount() %></td>
	  <td><a href="sessionExpire.jsp?ctx=<%= request.getParameter("ctx") %>&id=<%= sS.getId().toString() %>" >
              <%= sS.getState() %></td>
	      </a>
	  <td><%= sS.getTimeStamp().getCreationTime() %></td>
	  <td><%= sS.getTimeStamp().getLastAccessedTime() %></td>
	  <td><%= sS.getTimeStamp().getMaxInactiveInterval() %></td>
	  <td><%= sS.getTimeStamp().isNew() %></td>
      </tr>
     </adm:iterate>
    </table>

</body>
</html>
