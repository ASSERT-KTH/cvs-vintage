<html>
<%@ taglib uri="http://jakarta.apache.org/taglibs/tomcat_admin-1.0" 
           prefix="adm" %>

<head>

    <title>Context Detail</title>
</head>

<body bgcolor="white">

<adm:admin ctxPathParam="ctx" ctxHostParam="host" />

<%
  if( ctx==null ) {
%>
  <h1>Error, no context for <%= request.getParameter( "ctx" ) %> </h1>
<% } else { %>

  <a href="sessionState.jsp?ctx=<%= request.getParameter( "ctx" ) %>&host=<%=
           request.getParmeter("host") == null ? "" : 
	     request.getParameter("host")">Session info</a>
  <table border="0" cellpad="2" >
  <tr bgcolor="#e1e1e1">

    <tr bgcolor="#e1e1e1">
      <th>Path</th><td> <%= ctx.getPath() %> </td>
    </tr>
    <tr>
      <th>DocBase</th>
      <td> <%= ctx.getDocBase() %> </td>
    </tr>
    <tr bgcolor="#e1e1e1">
      <th>Reloadable</th>
      <td> <%= ctx.getReloadable() %></td>
    </tr>
    <tr>
      <th>AbsolutePath</th><td> <%= ctx.getAbsolutePath() %> </td>
    </tr>
   <tr bgcolor="#e1e1e1">
      <th>Description</th><td> <%= ctx.getDescription() %> </td>
    </tr>
    <tr>
      <th>Distributable</th><td> <%= ctx.isDistributable() %> </td>
    </tr>
    <tr bgcolor="#e1e1e1">
      <th>Trusted</th><td> <%= ctx.isTrusted() %> </td>
    </tr>
    <tr>
      <th>Session Timeout</th><td> <%= ctx.getSessionTimeOut() %> </td>
    </tr>
    <tr bgcolor="#e1e1e1">
      <th>Reloadable</th><td> <%= ctx.getReloadable() %> </td>
    </tr>
    <tr>
      <th>Index files</th>
      <td> 
	<adm:iterate name="wfile" array="<%= ctx.getWelcomeFiles() %>" 
                     type="java.lang.String" >
	  <%= wfile %>&nbsp;
        </adm:iterate>
      </td>
   </tr>
    <tr>
      <th>Virtual Hosts</th>
      <td> <%= ( null == ctx.getHost())? "DEFAULT" : ctx.getHost() %>&nbsp; 
	<adm:iterate name="vhost" enumeration="<%= ctx.getHostAliases() %>" 
                     type="java.lang.String" >
	  <%= vhost %>&nbsp;
        </adm:iterate>
      </td>
   </tr>
   </table>

   <h3>Taglibs</h3>
   <table>
	<adm:iterate name="tlib" enumeration="<%= ctx.getTaglibs() %>" 
                     type="java.lang.String" >
          <tr>
	    <td bgcolor="#e1e1e1"><%= tlib %></td>
	    <td><%= ctx.getTaglibLocation( tlib ) %> </td>
	  </tr>
        </adm:iterate>
   </table>

   <h3>Init params</h3>

   <table>
     <adm:iterate name="initP" enumeration="<%= ctx.getInitParameterNames() %>" 
					     type="java.lang.String" >
	<tr>
	  <td bgcolor="#e1e1e1"><%= initP %></td>
	    <td><%= ctx.getInitParameter( initP ) %> </td>
	  </tr>
     </adm:iterate>

<% } %>

</body>
</html>
